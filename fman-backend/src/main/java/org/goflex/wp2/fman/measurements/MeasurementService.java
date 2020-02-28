package org.goflex.wp2.fman.measurements;

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


import org.goflex.wp2.core.entities.TimeSeries;
import org.goflex.wp2.fman.measurements.events.MeasurementsUpdatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MeasurementService {
    @Autowired
    MeasurementRepository repo;

    @Autowired
    ApplicationEventPublisher publisher;

    public Page<MeasurementT> findAll(Specification<MeasurementT> spec, Pageable pageable) {
        return repo.findAll(spec, pageable);
    }


    public void postMeasurements(List<MeasurementT> measurements) {
        this.repo.saveAll(measurements);
        this.repo.flush();

        /* Notify about the availability of new measurements*/
        Map<String, List<MeasurementT>> userMeasMap =
                measurements
                .stream()
                .collect(Collectors.groupingBy(m -> m.getUserName()));

        userMeasMap.forEach((userName, userMeas) -> {
            this.publisher.publishEvent(new MeasurementsUpdatedEvent(this, userName, userMeas));
        });
    }

    private Optional<TimeSeries> _interpolateData(List<MeasurementRepository.TimeIntervalAndValue> mt) {
        if (mt.size() == 0 ) {
            return Optional.empty();
        }

        long timeEarliest = mt.get(0).getTimeInterval();
        long timeLatest = mt.get(mt.size() - 1).getTimeInterval();

        double [] values = new double[(int) (timeLatest - timeEarliest + 1)];

        values[0] = mt.get(0).getValue();

        for (int i = 1; i < mt.size(); i++) {
            long timeBefore = mt.get(i - 1).getTimeInterval();
            long timeNow = mt.get(i).getTimeInterval();
            double cummulEnergyBefore = mt.get(i - 1).getValue();
            double cummulEnergyNow = mt.get(i).getValue();

            // Linearly-interpolate cummulative energy values
            for (long ti = timeBefore; ti <= timeNow; ti++) {
                values[(int) (ti - timeEarliest)] = cummulEnergyBefore + (cummulEnergyNow - cummulEnergyBefore) *
                                                                        ((double) (ti - timeBefore) / (timeNow - timeBefore));
            }
        }

        TimeSeries ts = new TimeSeries(timeEarliest, values);

        return Optional.of(ts);
    }

    /**
     *  This returns a timeseries of measurements for a given user and time window. Missing values are interpolated linearly.
     *
     * @param userName
     * @param startInterval
     * @param stopInterval
     * @param normalized If false, then return a cummulative energy time series, otherwise - normalized to the initial value of energy
     * @return
     */
    public TimeSeries getUserMeasurementSeries(String userName, long startInterval, long stopInterval, boolean normalized) {

        List<MeasurementRepository.TimeIntervalAndValue> mt = this.repo.aggregateForTimeIntervals(userName, startInterval, stopInterval);

        TimeSeries ts = this._interpolateData(mt)
                            .orElse(new TimeSeries(startInterval, new double[] {}));

        if (normalized) {
            Double commuEnergy = this.repo.getCommulativeEnergyUntil(userName, startInterval-1);
            ts = ts.deAccumulate(commuEnergy != null ? commuEnergy : 0.0);
            //ts = ts.getData().length > 0 ? ts.deAccumulate(commuEnergy != null ? commuEnergy : 0.0) : ts;
        }

        return ts;

    }

    /**
     * This returns a timeseries of measurements aggregated for all users and time window.
     * Missing values are interpolated linearly.
     *
     * @param startInterval
     * @param stopInterval
     * @param normalized
     * @return
     */

    public TimeSeries getAggregatedMeasurementSeries(long startInterval, long stopInterval, boolean normalized) {
        List<MeasurementRepository.TimeIntervalAndValue> mt = this.repo.aggregateForTimeIntervals(startInterval, stopInterval);

        TimeSeries ts = this._interpolateData(mt)
                .orElse(new TimeSeries(startInterval, new double[] {}));

        if (normalized) {
            Double commuEnergy = this.repo.getCommulativeEnergyUntil(startInterval-1);
            ts = ts.deAccumulate(commuEnergy != null ? commuEnergy : 0.0);
        }

        return ts;
    }

    /**
     * Estimates the current operating power, based on the measurements received
     * @return
     */
    public double getCurrentOperatingPower() {
        Date nowTime = new Date();
        Date lookbackTime = new Date(System.currentTimeMillis() - 300 * 1000);
        Double power = this.repo.aggregateCurrentPower(nowTime, lookbackTime);

        return power != null ? Math.round(power * 10000D) / 10000D : 0.0;
    }
}
