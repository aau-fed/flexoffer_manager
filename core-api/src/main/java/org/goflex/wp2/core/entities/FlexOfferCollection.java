package org.goflex.wp2.core.entities;

/*-
 * #%L
 * GOFLEX::WP2::Core Data Structures
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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * This class represents a collection of FlexOffers, supporting all kinds of operations on the collection
 */
public class FlexOfferCollection {
    final static Logger logger = LoggerFactory.getLogger(FlexOfferCollection.class);

    /* FlexOffer or AggregatedFlexOffer and user contract pairs */
    private List<FlexOffer> fos = new ArrayList();

    public FlexOfferCollection(Collection<? extends FlexOffer> fos) throws Exception {
        this.fos = new ArrayList<>();

        /* Flex-offer expences */
        for(FlexOffer f : fos) {

            // Check the schedules
            if (f.getDefaultSchedule()==null){
                f.setDefaultSchedule(new FlexOfferSchedule(f));
            }

            if (f.getFlexOfferSchedule() == null) {
                f.setFlexOfferSchedule(f.getDefaultSchedule().clone());
            }

            // Make sure the schedules are truely initialized
            if (f.getFlexOfferSchedule() == null) {
                f.setFlexOfferSchedule(new FlexOfferSchedule(f));
            }

            // Check the slice durations
            boolean has_invalid_profile = false;
            boolean has_incompact_durs = false;


            for (int j=0; j< f.getFlexOfferProfileConstraints().length; j++) {
                FlexOfferSlice s = f.getFlexOfferProfileConstraint(j);

                if (s.getMinDuration() < 1) {
                    logger.info("A FlexOffer slice min duration cannot be shorter than 1 (interval)");
                    has_invalid_profile = true;
                }

                if (s.getMaxDuration() < 1) {
                    logger.info("A FlexOffer slice max duration cannot be shorter than 1 (interval)");
                    has_invalid_profile = true;
                }

                if (s.getMinDuration() > s.getMaxDuration()) {
                    logger.info("A FlexOffer slice max duration cannot be shorter than min duration");
                    has_invalid_profile = true;
                }

                // Assert slice durations
                if (s.getMaxDuration() != s.getMinDuration()) {
                    logger.error("FlexOffers with the slices of flexible duration cannot be handled!");
                    has_invalid_profile = true;
                }

                if (s.getMaxDuration() != 1 || s.getMinDuration() != 1) {
                    has_incompact_durs = true;
                    break;
                }
            }

            // We skip an invalid offer
            if (has_invalid_profile) continue;

            if (has_incompact_durs) {
                // Rework the profile to be compatible
                List<FlexOfferSlice> new_prof = new ArrayList<FlexOfferSlice>();
                List<FlexOfferScheduleSlice> new_sch = new ArrayList<FlexOfferScheduleSlice>();
                List<FlexOfferScheduleSlice> new_defsch = new ArrayList<FlexOfferScheduleSlice>();

                for (int l=0; l < f.getFlexOfferProfileConstraints().length; l++){
                    for(int m = 0; m < f.getFlexOfferProfileConstraint(l).getMaxDuration(); m++) {
                        long dur = f.getFlexOfferProfileConstraint(l).getMaxDuration();
                        FlexOfferSlice fs = f.getFlexOfferProfileConstraints()[l];

                        double lowerTariff = 0;
                        double upperTariff = 0;

                        if (fs.getTariffConstraint() != null) {
                            lowerTariff = fs.getTariffConstraint().getMinTariff();
                            upperTariff = fs.getTariffConstraint().getMaxTariff();
                        }


                        FlexOfferSlice s = new FlexOfferSlice(1, 1, new FlexOfferConstraint [] {
                                new FlexOfferConstraint(fs.getEnergyLower(0) / dur, fs.getEnergyUpper(0) / dur)
                        }, lowerTariff,  upperTariff) ;

                        new_prof.add(s);
                        new_defsch.add(new FlexOfferScheduleSlice(f.getDefaultSchedule().getScheduleSlice(l).getEnergyAmount() / dur,
                                f.getDefaultSchedule().getScheduleSlice(l).getTariff()));
                        new_sch.add(new FlexOfferScheduleSlice(f.getFlexOfferSchedule().getScheduleSlice(l).getEnergyAmount() / dur,
                                f.getFlexOfferSchedule().getScheduleSlice(l).getTariff()));
                    }
                }
                f.setFlexOfferProfileConstraints(new_prof.toArray(new FlexOfferSlice [] {}));
                f.getDefaultSchedule().setScheduleSlices(new_defsch.toArray(new FlexOfferScheduleSlice[0]));
                f.getFlexOfferSchedule().setScheduleSlices(new_sch.toArray(new FlexOfferScheduleSlice[0]));

                logger.warn("Flex-offer has been resampled to have the slices of the duration of 1");
            }

            this.getFos().add(f);
        }
    }

    public List<FlexOffer> getFos() { return fos;  }

    public void setFos(List<FlexOffer> fos) { this.fos = fos; }

    /* Get a time series of baseline energy */
    public TimeSeries getBaselineEnergy() throws Exception {
        return this.getEnergySeriesByType(TimeSeriesType.tstBaselineEnergy);
    }

    /* Get a time series of scheduled energy */
    public TimeSeries getScheduledEnergy() throws Exception {
        return this.getEnergySeriesByType(TimeSeriesType.tstScheduledEnergy);
    }

    /* Get a time series of a requested energy. NoDATA values are filled with zeros */
    public TimeSeries getEnergySeriesByType(TimeSeriesType type) throws Exception {
        TimeSeries ts = new TimeSeries(new double[] {});

        for(FlexOffer f : this.getFos()) {
            if (f.getFlexOfferSchedule() != null) {
                TimeSeries ft = new TimeSeries(f, type);
                ts._extend(ft)._plus(ft);
            }
        }
        return ts;
    }

    /* Get a time series of scheduled energy. Gaps are filled with NoDataValue */
    public TimeSeries getEnergySeriesByType(TimeSeriesType type, Double noDataValue) throws Exception {
        TimeSeries ts = new TimeSeries(new double[] {});

        for(FlexOffer f : this.getFos()) {
            if (f.getFlexOfferSchedule() != null) {
                TimeSeries ft = new TimeSeries(f, type);
                ts._extend(ft, noDataValue)
                  ._copy(ft.mul(0))
                  ._plus(ft);
            }
        }
        return ts;
    }

    /* Set all schedules equal to baseline */
    public void setSchedulesToBaseline() throws Exception {
        for(FlexOffer f : fos) {
            f.setFlexOfferSchedule(f.getDefaultSchedule().clone());
        }
    }

    public long getMinTid() {
        long value = Integer.MAX_VALUE;
        for(FlexOffer fo : this.fos) {
            if (fo.getFlexOfferSchedule() == null) continue;

            value = Math.min(value, fo.getFlexOfferSchedule().getStartInterval());
        }
        return value;
    }

    public Date getMinTime() {
        return FlexOffer.toAbsoluteTime(this.getMinTid());
    }

    public long getMaxTid() {
        long value = -Integer.MAX_VALUE;
        for(FlexOffer fo : this.fos) {
            if (fo.getFlexOfferSchedule() == null) continue;

            value = Math.max(value, fo.getFlexOfferSchedule().getStartInterval() + fo.getFlexOfferSchedule().getScheduleSlices().length - 1);
        }
        return value;
    }

    public Date getMaxTime() {
        return FlexOffer.toAbsoluteTime(this.getMaxTid());
    }

    /* Compute the schedule demand/supply imbalances - more performant method */

    private void fillTidTableWithScheduleValues(Map<Long, Double> tid_tbl) {
        tid_tbl.clear();
        for (FlexOffer f : this.getFos()) {
            FlexOfferSchedule sch = f.getFlexOfferSchedule();

            if (sch == null) continue;

            for(int j=0; j<sch.getScheduleSlices().length; j++) {
                long tid = sch.getStartInterval() + j;

                if (!tid_tbl.containsKey(tid)) {
                    tid_tbl.put(tid, sch.getScheduleSlice(j).getEnergyAmount());
                } else {
                    tid_tbl.replace(tid, sch.getScheduleSlice(j).getEnergyAmount() + tid_tbl.get(tid));
                }
            }
        }
    }

    public double computeEnergyImbalance() {
        Map<Long, Double> tid_tbl = new HashMap<Long, Double>();

        this.fillTidTableWithScheduleValues(tid_tbl);

        double imbalance = 0;
        for(Double value : tid_tbl.values()) {
            imbalance += Math.abs(value);
        }

        tid_tbl = null; // Dispose

        return imbalance;
    }

    public double computeEnergySquaredImbalance() {
        Map<Long, Double> tid_tbl = new HashMap<Long, Double>();

        this.fillTidTableWithScheduleValues(tid_tbl);

        double imbalance = 0;
        for(Double value : tid_tbl.values()) {
            imbalance += value*value;
        }

        tid_tbl = null; // Dispose

        return imbalance;
    }

    /**
     * Compute imbalance agains a given timeseries (only for specified not-NODATA points)
     * @param ts
     * @param noDataValue
     * @return
     */
    public double computeEnergySquaredImbalanceAgainstTimeSeries(TimeSeries ts, Double noDataValue) {
        Map<Long, Double> tid_tbl = new HashMap<Long, Double>();

        this.fillTidTableWithScheduleValues(tid_tbl);

        double imbalance = 0;

        // Go through the FO intervals
        for(Map.Entry<Long, Double> entry: tid_tbl.entrySet()) {
            Optional<Double> tsVal = ts.getOptValue(entry.getKey());

            if (tsVal.isPresent() && tsVal.get() != noDataValue) {

                double val = entry.getValue() - tsVal.get();

                imbalance += val * val;
            }
        }


        tid_tbl = null; // Dispose

        return imbalance;
    }

    public double computeTotalEnergy() {
        double totalEnergy = 0;
        for (FlexOffer f : this.getFos()) {
            FlexOfferSchedule sch = f.getFlexOfferSchedule();

            if (sch == null) continue;

            for(int j=0; j<sch.getScheduleSlices().length; j++) {
                totalEnergy += sch.getScheduleSlice(j).getEnergyAmount();
            }
        }

        return totalEnergy;
    }

    /**
     * Compute the total energy at a specific time window
     * @param tid_from
     * @param tid_to
     * @return
     */
    public double computeTotalEnergy(long tid_from, long tid_to) {
        double totalEnergy = 0;
        for (FlexOffer f : this.getFos()) {
            FlexOfferSchedule sch = f.getFlexOfferSchedule();

            if (sch == null) continue;

            for(int j=0; j<sch.getScheduleSlices().length; j++) {
                long t = sch.getStartInterval() + j;

                if (tid_from<=t && t <= tid_to) {
                    totalEnergy += sch.getScheduleSlice(j).getEnergyAmount();
                }
            }
        }

        return totalEnergy;
    }

}
