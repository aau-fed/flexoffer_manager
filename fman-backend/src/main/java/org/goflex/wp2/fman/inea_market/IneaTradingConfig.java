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


/**
 * This class specifies the dynamic configuration of the INEA's FMAR trading process
 */
public class IneaTradingConfig {

    /**
     * This indicates whether the trading on FMAN is enabled
     */
    private Boolean tradingEnabled;

    /**
     * This parameter specifies a proportional margin added on top of the expected portfolio cost when generating
     * a market bid
      */
    private double proportionalBidMargin = 1.05;

    /**
     * This parameter specifies an additional margin to apply per 1kWh of deviation energy (from baseline)
     */
    private double bidMarginPerDeltaKwh = 0.05;

    /**
     * This specifies the trading horizon, i.e., how long is the bid flex-offer (No of 15 min slices)
     */
    private int tradingHorizonInMultiplesOf15min = 1;

    /**
     * This specifies the trading frequency, i.e., how frequently a new offer is traded (in the multiple of 15 min).
     */
    private int tradingFrequencyInMultiplesOf15min = 1;

//    /**
//     * System triggers a bid update when current (potential) bid differs from the previously submitted by more
//     * than "updateBinOnRelativeEnergyChange * 100" percent;
//     *
//     * @return
//     */
//    private double updateBinOnRelativeEnergyChange = 0.05;

    /**
     * System triggers a bid update when current (potential) bid differs from the previously submitted by more
     * than "bidUpdatePolicyOnAbsoluteEnergyChange " kWh of energy;
     *
     * @return
     */
    private double bidUpdatePolicyOnAbsoluteEnergyChange = 500;


    /**
     * Contract details
     */
    private double contractImbalanceFee = 10e6;

    private double contractCutOffAmount = 1; // 1 kWh

    public double getProportionalBidMargin() {
        return proportionalBidMargin;
    }

    public void setProportionalBidMargin(double proportionalBidMargin) {
        this.proportionalBidMargin = proportionalBidMargin;
    }

    public double getBidMarginPerDeltaKwh() {
        return bidMarginPerDeltaKwh;
    }

    public void setBidMarginPerDeltaKwh(double bidMarginPerDeltaKwh) {
        this.bidMarginPerDeltaKwh = bidMarginPerDeltaKwh;
    }

//    public double getUpdateBinOnRelativeEnergyChange() {
//        return updateBinOnRelativeEnergyChange;
//    }
//
//    public void setUpdateBinOnRelativeEnergyChange(double updateBinOnRelativeEnergyChange) {
//        this.updateBinOnRelativeEnergyChange = updateBinOnRelativeEnergyChange;
//    }

    public double getBidUpdatePolicyOnAbsoluteEnergyChange() {
        return bidUpdatePolicyOnAbsoluteEnergyChange;
    }

    public void setBidUpdatePolicyOnAbsoluteEnergyChange(double bidUpdatePolicyOnAbsoluteEnergyChange) {
        this.bidUpdatePolicyOnAbsoluteEnergyChange = bidUpdatePolicyOnAbsoluteEnergyChange;
    }

    public int getTradingHorizonInMultiplesOf15min() {
        return tradingHorizonInMultiplesOf15min;
    }

    public void setTradingHorizonInMultiplesOf15min(int tradingHorizonInMultiplesOf15min) {
        this.tradingHorizonInMultiplesOf15min = tradingHorizonInMultiplesOf15min;
    }

    public int getTradingFrequencyInMultiplesOf15min() {
        return tradingFrequencyInMultiplesOf15min;
    }

    public void setTradingFrequencyInMultiplesOf15min(int tradingFrequencyInMultiplesOf15min) {
        this.tradingFrequencyInMultiplesOf15min = tradingFrequencyInMultiplesOf15min;
    }

    public Boolean getTradingEnabled() {
        return tradingEnabled;
    }

    public void setTradingEnabled(Boolean tradingEnabled) {
        this.tradingEnabled = tradingEnabled;
    }

    public double getContractImbalanceFee() {
        return contractImbalanceFee;
    }

    public void setContractImbalanceFee(double contractImbalanceFee) {
        this.contractImbalanceFee = contractImbalanceFee;
    }

    public double getContractCutOffAmount() {
        return contractCutOffAmount;
    }

    public void setContractCutOffAmount(double contractCutOffAmount) {
        this.contractCutOffAmount = contractCutOffAmount;
    }
}
