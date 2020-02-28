package org.goflex.wp2.fman.shell;

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


import com.google.common.collect.ImmutableMap;
import org.goflex.wp2.core.entities.*;
import org.goflex.wp2.fman.flexoffer.FlexOfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


public class DemoFlexOffers {

    /* Test methods for populating Aggregator with meaninful data */
    public List<FlexOffer> demo(int numFos) {
        List<FlexOffer> fl = this.demoConsumptionFOs(numFos);
        fl.addAll(this.demoProductionFOs(numFos));
        return fl;
    }

    public List<FlexOffer> demo_consumption(int numFos) {
        List<FlexOffer> fl = this.demoConsumptionFOs(numFos);
        return fl;
    }

    public List<FlexOffer> demo_production(int numFos) {
        List<FlexOffer> fl = this.demoProductionFOs(numFos);
        return fl;
    }

    public List<FlexOffer> demoConsumptionFOs(int numFos) {
        return demoConsumptionFOs(numFos, 20, 25, 10);
    }

    public List<FlexOffer> demoProductionFOs(int numFos) {
        return demoProductionFOs(numFos, 20, 25, 10);
    }

    public List<FlexOffer> demoConsumptionFOs(int numFos, double TWfactor, double TFTmax, double enFactor) {
        Calendar cal = Calendar.getInstance();
        Date dateFrom = cal.getTime();
        cal.add(Calendar.MINUTE, (int) (1 * TWfactor));
        Date dateTo = cal.getTime();

        return this.demoFOs(numFos, dateFrom, dateTo, 10, TFTmax, 0, 1 * enFactor, 0.4 * enFactor, false);
    }

    public List<FlexOffer>  demoProductionFOs(int numFos, double TWfactor, double TFTmax, double enFactor) {
        Calendar cal = Calendar.getInstance();
        Date dateFrom = cal.getTime();
        cal.add(Calendar.MINUTE, (int) (1 * TWfactor));
        Date dateTo = cal.getTime();

        return this.demoFOs(numFos, dateFrom, dateTo, 10, TFTmax, -1 * enFactor, 0, -0.4 * enFactor, false);
    }


    public List<FlexOffer> demoFOs(int numFos, Date dateFrom, Date dateTo, int maxSlices, double TFTmax, double Emin, double Emax, double EFmax, boolean schedule) {
        Calendar cal = Calendar.getInstance();

        List<FlexOffer> fl = new ArrayList<>();

        for (int i = 0; i < numFos; i++) {
            cal.setTime(dateFrom);
            long mFrom = cal.getTimeInMillis();
            cal.setTime(dateTo);
            long mTo = cal.getTimeInMillis();
            long mEST = (long) (mFrom + Math.random() * (mTo - mFrom));
            cal.setTimeInMillis(mEST);

            FlexOffer f = new FlexOffer();
            // f.setId(idl++);
            f.setLabel("Test Flex-Offer");
            f.setOfferedById("admin");
            f.setStartAfterTime(cal.getTime());
            cal.add(Calendar.MINUTE, (int) (15 * Math.random() * TFTmax));
            f.setStartBeforeTime(cal.getTime());
            cal.setTimeInMillis(mEST);
            cal.add(Calendar.MINUTE, -15);
            f.setAssignmentBeforeTime(cal.getTime());
            cal.add(Calendar.MINUTE, -15);
            f.setAcceptanceBeforeTime(cal.getTime());
            cal.add(Calendar.MINUTE, -30);
            f.setCreationTime(cal.getTime());

            List<FlexOfferSlice> sl = new ArrayList<FlexOfferSlice>();

            double totalMinE=0, totalMaxE=0;
            for (int k = 0; k < maxSlices; k++) {
                double minE, maxE;

                if (EFmax > 0) {
                    minE = Emin + Math.random() * (Emax - Emin);
                    maxE = minE + Math.random() * EFmax;
                } else {
                    maxE = Emin + Math.random() * (Emax - Emin);
                    minE = maxE + Math.random() * EFmax;
                }

                totalMinE += minE;
                totalMaxE += maxE;
                sl.add(new FlexOfferSlice(1, 1, new FlexOfferConstraint[] { new FlexOfferConstraint(minE, maxE) }, 0, 0));
            }

            f.setFlexOfferProfileConstraints(sl.toArray(new FlexOfferSlice[] {}));

            if (schedule) {
                f.setFlexOfferSchedule(new FlexOfferSchedule(f));
            }

            /* Generate a random location */
            GeoLocation loc = new GeoLocation(9.9036 + Math.random() * 1e-4, 57.0541 + Math.random() * 1e-4);
            f.setLocationId(ImmutableMap.of("userLocation", loc));
            fl.add(f);
        }
        return fl;
    }
}

