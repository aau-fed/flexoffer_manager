package org.goflex.wp2.fman.inea_market;

/*-
 * #%L
 * GOFLEX::WP2::FlexOfferManager Backend
 * %%
 * Copyright (C) 2017 - 2020 The GOFLEX Consortium
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import com.fasterxml.jackson.annotation.JsonProperty;
import org.goflex.wp2.core.entities.FlexOffer;
import org.goflex.wp2.core.entities.FlexOfferSchedule;
import org.goflex.wp2.core.entities.TimeSeries;
import org.goflex.wp2.fman.inea_market.marketcontract.IneaMarketContract;

import java.util.Collection;
import java.util.Optional;

/**
 * Market commitment is a transaction in a special state: 1. it is not cancelled; 2. it has a schedule assigned.
 *
 * This class is a wrapper over such a transaction. This represents the commitments which Aggregator enters to, after a bid is won.
 *
 * @author Laurynas
 *
 */
public class IneaMarketCommitment {
    public static boolean isCommitmentTransaction(IneaTransactionHistoryT transaction) {
        return (transaction.getState() == IneaTransactionHistoryT.TransactionState.tActive ||
                transaction.getState() == IneaTransactionHistoryT.TransactionState.tClosed) &&
                (transaction.getTradingFlexOffer() != null) && (transaction.getTradingFlexOffer().getFlexOfferSchedule() != null);
    }


    private IneaTransactionHistoryT transaction;
    private TimeSeries aggregatedEnergyMeasurements;


    public IneaMarketCommitment(IneaTransactionHistoryT transaction,
                                TimeSeries aggregatedEnergyMeasurements
                                ) throws Exception {

        if (!this.isCommitmentTransaction(transaction))
            throw new Exception("The FMAR transaction does not qualify for a market commitments: it is cancelled or does not have INEA schedule assigned!");

        this.transaction = transaction;
        this.aggregatedEnergyMeasurements = aggregatedEnergyMeasurements != null? aggregatedEnergyMeasurements : new TimeSeries(transaction.getTradingIntervalFrom(), new double[] {});
    }

    public IneaTransactionHistoryT getTransaction() {
        return transaction;
    }


    public FlexOffer getWinningFlexOffer() {
        return this.transaction.getTradingFlexOffer();
    }

    public FlexOfferSchedule getWinningSchedule() {
        return this.transaction.getTradingFlexOffer().getFlexOfferSchedule();
    }

    public FlexOfferSchedule getDefaultSchedule() {
        return this.transaction.getTradingFlexOffer().getDefaultSchedule();
    }

    public IneaMarketContract getContract() {
        return this.transaction.getActiveContract();
    }

    /**
     * Calculates the FO assignment price, as described in the Gregor's document: FMAR_spec_2018_07_06.
     * The Gregor calls this "cost of the individual assigned flex offer".
     *
     * This is available before execution.
     */
    @JsonProperty("expectedMarketGain")
    public double getExceptedMarketGain() {
        FlexOffer f = this.getWinningFlexOffer();

        if (f.getFlexOfferSchedule() == null || f.getDefaultSchedule() == null) {  return 0;  }

        double s = 0;
        for (int i=0; i< f.getFlexOfferProfileConstraints().length; i++) {
            double fo_pr = f.getFlexOfferSchedule().getScheduleSlice(i).getEnergyAmount() >
                           f.getDefaultSchedule().getScheduleSlice(i).getEnergyAmount() ?
                                    f.getFlexOfferProfileConstraint(i).getTariffConstraint().getMaxTariff() :
                                    f.getFlexOfferProfileConstraint(i).getTariffConstraint().getMinTariff() ;

            s +=  fo_pr * (f.getFlexOfferSchedule().getScheduleSlice(i).getEnergyAmount() -
                           f.getDefaultSchedule().getScheduleSlice(i).getEnergyAmount());
        }

        return s;
    }

    /**
     * Calculates the imbalance, as described in the Gregor's document: FMAR_spec_2018_07_06.
     *
     * This is available after execution.
     */
    @JsonProperty("factualImbalance")
    public double getFactualImbalance() {
        FlexOffer f = this.getWinningFlexOffer();
        double imbalance = 0;

        for (int i=0; i< f.getFlexOfferProfileConstraints().length; i++) {
            /* Simple TID mapping is only supported on min/max duration of 1*/
            assert(f.getFlexOfferProfileConstraint(i).getMinDuration() == 1);
            assert(f.getFlexOfferProfileConstraint(i).getMaxDuration() == 1);

            long tid = f.getFlexOfferSchedule().getStartInterval() + i;

            Optional<Double> foenergy = this.aggregatedEnergyMeasurements.getOptValue(tid);

            if (foenergy.isPresent()) {
                imbalance += Math.abs(foenergy.get() - f.getFlexOfferSchedule().getScheduleSlice(i).getEnergyAmount());
            }
        }

        return imbalance;
    }

    @JsonProperty("factualImbalanceFee")
    public double getFactualImbalanceFee() {
        return Math.max(0, this.getFactualImbalance() - this.getContract().getImbalanceCutOffMargin()) *
                this.getContract().getImbalanceFee();
    }

    /* Checks if measurements are available (for the commitment period) */
    @JsonProperty("canBeValidated")
    public boolean canBeValidated() {
        FlexOffer f = this.getWinningFlexOffer();

        for (int i=0; i< f.getFlexOfferProfileConstraints().length; i++) {

            long tid = f.getFlexOfferSchedule().getStartInterval() + i;

            Optional<Double> foenergy = this.aggregatedEnergyMeasurements.getOptValue(tid);

            if (!foenergy.isPresent()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the total commitment gain: expected gain - imbalance fee.
     * When positive - brings benefit to aggregator, negative - brings losses
     */
    @JsonProperty("totalGain")
    public double getTotalGain() {
        return this.getExceptedMarketGain() - this.getFactualImbalanceFee();
    }


    /**
     * More optimized version of getExpectedImbalance - to improve optimization performance
     *
     * @param flexOfferEnergy
     * @return
     */
    public double getExpectedImbalance(Collection<FlexOffer> flexOfferEnergy) {
        FlexOfferSchedule ms = this.getWinningFlexOffer().getFlexOfferSchedule();

        double imbalance = 0;

        for (int k = 0; k < ms.getScheduleSlices().length; k++) {
            /* Simple TID mapping is only supported on the duration of 1*/
            assert(ms.getScheduleSlice(k).getDuration() == 1);

            long tid = ms.getStartInterval() + k;

            double energyAtTid = 0;

            for (FlexOffer f : flexOfferEnergy) {
                FlexOfferSchedule sch = f.getFlexOfferSchedule();

                if (sch == null) continue;

                if (tid < sch.getStartInterval()) {
                    continue;
                }
                if (tid > sch.getStartInterval() + sch.getScheduleSlices().length - 1) {
                    continue;
                }

                energyAtTid += sch.getScheduleSlice((int) (tid - sch.getStartInterval())).getEnergyAmount();
            }

            imbalance += Math.abs(ms.getScheduleSlice(k).getEnergyAmount() - energyAtTid);
        }

        return imbalance;
    }

    public double getExpectedImbalanceFee(Collection<FlexOffer> flexOfferEnergy) {
        return Math.max(0, this.getExpectedImbalance(flexOfferEnergy) - this.getContract().getImbalanceCutOffMargin()) * this.getContract().getImbalanceFee();
    }



}
