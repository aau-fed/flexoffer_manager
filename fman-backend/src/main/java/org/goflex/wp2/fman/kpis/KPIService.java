package org.goflex.wp2.fman.kpis;

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
import org.goflex.wp2.core.entities.TimeSeries;
import org.goflex.wp2.core.entities.TimeSeriesType;
import org.goflex.wp2.fman.aggregatorportfolio.AggregatorPortfolio;
import org.goflex.wp2.fman.aggregatorportfolio.AggregatorPortfolioService;
import org.goflex.wp2.fman.user.UserService;
import org.goflex.wp2.fman.user.UserT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 *  This service computes all required GOFLEX KPIs
 *
 * Laurynas
 */
@Service
public class KPIService {
    private static final Logger logger = LoggerFactory.getLogger(KPIService.class);

    @Autowired
    private AggregatorPortfolioService portSvc;

    @Autowired
    UserService userService;

    private double zeroOnNan(double val) {
        if (Double.isNaN(val)) return 0;
        else return val;
    }

    private KPIEnergyAdaptability _getLoadAdaptabilityKpi(AggregatorPortfolio portfolio, Date dateFrom, Date dateTo ) throws Exception {

        KPIEnergyAdaptability k = new KPIEnergyAdaptability();

        TimeSeries maxEnergy = portfolio.getFoCollection().getEnergySeriesByType(TimeSeriesType.tstMaxEnergy);
        TimeSeries minEnergy = portfolio.getFoCollection().getEnergySeriesByType(TimeSeriesType.tstMinEnergy);
        TimeSeries baselineEnergy = portfolio.getFoCollection().getEnergySeriesByType(TimeSeriesType.tstBaselineEnergy);
        TimeSeries scheduledEnergy = portfolio.getFoCollection().getEnergySeriesByType(TimeSeriesType.tstScheduledEnergy);

        // If a KPI for a period requested, make sure we exactly cover this period
        if (dateFrom != null && dateTo != null) {
            long tiFrom = FlexOffer.toFlexOfferTime(dateFrom);
            int tiTo = (int)(FlexOffer.toFlexOfferTime(dateTo) - FlexOffer.toFlexOfferTime(dateFrom)) + 1;
            TimeSeries refTs = new TimeSeries(tiFrom, tiTo, 0.0);

            maxEnergy = maxEnergy.extend(refTs);
            minEnergy = minEnergy.extend(refTs);
            baselineEnergy = baselineEnergy.extend(refTs);
            scheduledEnergy = scheduledEnergy.extend(refTs);
        }


        TimeSeries maxminDelta = maxEnergy.minus(minEnergy);

        TimeSeries maxConsumption = maxEnergy.map(v -> v > 0 ? v : 0);
        TimeSeries minConsumption = minEnergy.map(v -> v > 0 ? v : 0);

        TimeSeries maxProduction = minEnergy.map(v -> v > 0 ? 0 : -v);
        TimeSeries minProduction = maxEnergy.map(v -> v > 0 ? 0 : -v);

        k.setConsumptionVariability(this.zeroOnNan(maxConsumption.minus(minConsumption)
                .avg() * 4 /* 4 quarters per hour */));

        k.setConsumptionPeak(this.zeroOnNan(maxConsumption.avg() * 4 /* 4 quarters per hour */));

        k.setConsumptionAdaptabilityLevel(this.zeroOnNan(k.getConsumptionVariability() / k.getConsumptionPeak()));
        k.setConsumptionFlexibilityLevel(maxConsumption.minus(minConsumption).map(v -> v > 1e-6 ? 1.0 : 0.0).avg());


        k.setProductionVariability(this.zeroOnNan(maxProduction.minus(minProduction)
                .avg() * 4 /* 4 quarters per hour */));

        k.setProductionPeak(this.zeroOnNan(maxProduction.avg() * 4 /* 4 quarters per hour */));
        k.setProductionAdaptabilityLevel(this.zeroOnNan(k.getProductionVariability() / k.getProductionPeak()));
        k.setProductionFlexibilityLevel(maxProduction.minus(minProduction).map(v -> v > 1e-6 ? 1.0 : 0.0).avg());

        k.setAvgBaselineEnergy(this.zeroOnNan(baselineEnergy.avg() * 4));
        k.setAvgScheduledEnergy(this.zeroOnNan(scheduledEnergy.avg() * 4));
        k.setFlexibilityActivationLevel(scheduledEnergy.minus(baselineEnergy).map(v -> Math.abs(v) > 1e-6 ? 1.0 : 0.0).avg());

        return k;
    }

    public KPIEnergyAdaptability getLoadAdaptabilityKpi() {


        /* We don't need the recent portfolio for this. */
        try {
            AggregatorPortfolio port = portSvc.getLastOrActivePortfolio();
            return this._getLoadAdaptabilityKpi(port, null, null);
        } catch (Exception e) {
            logger.error("Error computing load adaptability level KPI", e);
            e.printStackTrace();
        }

        return null;
    }

    public KPIAggregatorBenefit getAggregatorBenefit() {
        KPIAggregatorBenefit k = new KPIAggregatorBenefit();
        /* We don't need the recent portfolio for this. */
        AggregatorPortfolio port = portSvc.getLastPortfolio();

        if (port != null) {
            k.setExpectedAggregatorGains(port.computeMarketGains());
            k.setExpectedAggregatorCosts(port.computePortfolioCosts());
        }

        return k;
    }

    public KPISummary generateKPISummary(Date timeFrom, Date timeTo, String userName) {
        KPISummary kpis = new KPISummary();
        kpis.setPeriodFrom(timeFrom);
        kpis.setPeriodTo(timeTo);
        kpis.setSummaryOfUserName(userName);

        try {
            Long userId = null;

            if (userName != null) {
                UserT user = this.userService.getUserByUserName(userName);
                if (user == null) throw new Exception("No user can be found.");
                userId = user.getUserId();
            }

            AggregatorPortfolio port = portSvc.getPortfolioInPeriod(timeFrom, timeTo, userId);
            kpis.setNumberOfFOs(port.getFlexOffers().size());
            kpis.setNumberOfMarketCommitments(port.getMarketCommitments().size());

            // Compute aggregator benefit
            KPIAggregatorBenefit k = new KPIAggregatorBenefit();
            k.setExpectedAggregatorGains(port.computeMarketGains());
            k.setExpectedAggregatorCosts(port.computePortfolioCosts());
            kpis.setAggregatorBenefitKPIs(k);

            // Compute energy adaptability KPIs
            KPIEnergyAdaptability ad = this._getLoadAdaptabilityKpi(port, timeFrom, timeTo);
            kpis.setAdaptabilityKPIs(ad);
        } catch (Exception ex){
            logger.error("Error retrieving historical portfolio", ex);
        }

        return kpis;
    }
}
