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


import org.goflex.wp2.core.entities.FlexOffer;
import org.goflex.wp2.core.entities.FlexOfferCollection;
import org.goflex.wp2.core.entities.TimeSeries;
import org.goflex.wp2.core.entities.TimeSeriesType;
import org.goflex.wp2.fman.measurements.MeasurementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class IneaMarketCommitmentService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IneaTransactionHistoryRepository histRepo;

    @Autowired
    private MeasurementService measService;

    public List<IneaMarketCommitment> getCommitmentTransactions(long tradingIntervalFrom, long tradingIntervalTo) {
        // Fetch factual aggregated measurements in this period
        TimeSeries measurements = this.measService.getAggregatedMeasurementSeries(tradingIntervalFrom, tradingIntervalTo, true);

        return this.getCommitmentTransactions(tradingIntervalFrom, tradingIntervalTo, measurements);
    }

    public List<IneaMarketCommitment> getCommitmentTransactions(long tradingIntervalFrom, long tradingIntervalTo, TimeSeries measurementSeries) {

        // Fetch all qualifying transactions
        List<IneaMarketCommitment> commitments =
                this.histRepo.findAllByTradingIntervalFromBetweenOrTradingIntervalToBetweenOrderByTransactionTimeDesc(tradingIntervalFrom, tradingIntervalTo, tradingIntervalFrom, tradingIntervalTo)
                        .stream()
                        .filter( t -> (t.getState() == IneaTransactionHistoryT.TransactionState.tActive ||
                                       t.getState() == IneaTransactionHistoryT.TransactionState.tClosed) &&
                                (t.getTradingFlexOffer() != null) && (t.getTradingFlexOffer().getFlexOfferSchedule() != null))
                        .map(t -> {
                            try {
                                return new IneaMarketCommitment(t, measurementSeries);
                            } catch (Exception e) {
                                return null;
                            }
                        })
                        .filter(t -> t != null)
                        .collect(Collectors.toList());

        return commitments;
    }

    /**
     * Gets a timeseries of the market commitment energy
     *
     * @param intervalFrom
     * @return
     */
    public TimeSeries getCommitmentEnergy(long intervalFrom, long intervalTo) {
        // Fetch all qualifying transactions
        List<FlexOffer> trans_fos =
                this.histRepo.findAllByTradingIntervalFromBetweenOrTradingIntervalToBetweenOrderByTransactionTimeDesc(intervalFrom, intervalTo, intervalFrom, intervalTo)
                        .stream()
                        .filter( t -> (t.getState() == IneaTransactionHistoryT.TransactionState.tActive ||
                                       t.getState() == IneaTransactionHistoryT.TransactionState.tClosed) &&
                                (t.getTradingFlexOffer() != null) && (t.getTradingFlexOffer().getFlexOfferSchedule() != null))
                        .map( t -> t.getTradingFlexOffer() )
                        .collect(Collectors.toList());

        try {
            FlexOfferCollection col = new FlexOfferCollection(trans_fos);

            return col.getEnergySeriesByType(TimeSeriesType.tstScheduledEnergy, Double.MAX_VALUE);
        } catch (Exception e) {
            return new TimeSeries();
        }
    }

}
