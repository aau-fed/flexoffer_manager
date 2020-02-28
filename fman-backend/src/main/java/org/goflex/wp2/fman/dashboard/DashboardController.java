package org.goflex.wp2.fman.dashboard;

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


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.goflex.wp2.core.entities.FlexOffer;
import org.goflex.wp2.core.entities.TimeSeriesType;
import org.goflex.wp2.fman.aggregatorportfolio.AggregatorPortfolio;
import org.goflex.wp2.fman.aggregatorportfolio.AggregatorPortfolioService;
import org.goflex.wp2.fman.common.currency.CurrencyService;
import org.goflex.wp2.fman.flexoffer.FlexOfferService;
import org.goflex.wp2.fman.inea_market.IneaMarketCommitmentService;
import org.goflex.wp2.fman.inea_market.IneaTradingService;
import org.goflex.wp2.fman.kpis.KPIService;
import org.goflex.wp2.fman.measurements.MeasurementService;
import org.goflex.wp2.fman.optimizer.OptimizationObjective;
import org.goflex.wp2.fman.optimizer.OptimizationService;
import org.goflex.wp2.fman.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.stream.Collectors;

/*
 * Created by Laurynas Siksnys
 */
@RestController
@PreAuthorize("hasRole('ROLE_ADMIN')")              // Only ADMINs can access this
@RequestMapping("/dashboard")
public class DashboardController {
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private OptimizationService optService;

    @Autowired
    private FlexOfferService foService;

    @Autowired
    private UserService usrService;

    @Autowired
    private AggregatorPortfolioService apService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private IneaTradingService tradeService;

    @Autowired
    private IneaMarketCommitmentService tradeCommService;

    @Autowired
    private MeasurementService measurementService;

    @Autowired
    private KPIService kpiService;


    @GetMapping(value="/", produces = "application/json")
    @ApiOperation(value = "${OptimizationController.getDashboardDetails}")
    public DashboardDetails getDashboardDetails() {
        DashboardDetails d = new DashboardDetails();

        Date now = new Date();
        Long timeIntervalFrom = FlexOffer.toFlexOfferTime(now) - 3600*24 / FlexOffer.numSecondsPerInterval;

        try {

            OptimizationObjective obj = this.optService.getOptimizationObjective();

            d.setNumOfActiveUsers((this.foService.getAllActiveFlexOffers()
                                                        .stream()
                                                        .map(f -> f.getUser().getUserId())
                                                        .distinct()
                                                        .collect(Collectors.counting()))
                                              .intValue());
            d.setTotalNoOfUsers((int) this.usrService.getUserCount());

            d.setOptimizationObjective(this.optService.getOptimizationObjective());

            // Set KPIs
            d.setKpiLoadAdaptability(this.kpiService.getLoadAdaptabilityKpi());
            d.setKpiAggregatorBenefit(this.kpiService.getAggregatorBenefit());

            // Generate timeseries data

            AggregatorPortfolio port = this.apService.getLastOrActivePortfolio();

            d.setCurrentPlanningInterval(FlexOffer.toFlexOfferTime(now));
            d.setDefaultSchedule(port.getFoCollection().getEnergySeriesByType(TimeSeriesType.tstBaselineEnergy));
            d.setActiveSchedule(port.getFoCollection().getEnergySeriesByType(TimeSeriesType.tstScheduledEnergy));
            d.setOverallLowSchedule(port.getFoCollection().getEnergySeriesByType(TimeSeriesType.tstMinEnergy));
            d.setOverallHighSchedule(port.getFoCollection().getEnergySeriesByType(TimeSeriesType.tstMaxEnergy));
            d.setCurrentOperatingPower(this.measurementService.getCurrentOperatingPower());
            d.setAggregatedMeasurements(this.measurementService.getAggregatedMeasurementSeries(timeIntervalFrom, Long.MAX_VALUE, true));
            d.setMarketCommitments(this.tradeCommService.getCommitmentEnergy(timeIntervalFrom, Long.MAX_VALUE));
        } catch (Exception e) {
            logger.error("Error generating dashboard details: ", e);
        }
        return d;
    }

    @GetMapping(value="/config", produces = "application/json")
    @ApiOperation(value = "${OptimizationController.getDashboardConfig}")
    public DashboardConfig getDashboardConfig() {
        OptimizationObjective obj = this.optService.getOptimizationObjective();

        DashboardConfig c = new DashboardConfig();
        c.setAggregationEnabled(this.apService.isPortfolioAggregationEnabled());
        c.setTradingEnabled(this.tradeService.isTradingEnabled());
        c.setOptimizationObjective(obj);
        c.setOptimizationFrom(FlexOffer.toAbsoluteTime(obj.getTimeIntervalFrom()));
        c.setOptimizationTo(FlexOffer.toAbsoluteTime(obj.getTimeIntervalTo()));

        /* Currency settings */
        c.setCurrencySymbol(this.currencyService.getCurrencySymbol());
        c.setCurrencyRatioToEur(this.currencyService.getCurrencyRatioToEur());

        return c;
    }

    @PostMapping(value="/config", consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "${OptimizationController.setDashboardConfig}")
    public DashboardConfig setDashboardConfig(@ApiParam("Dashboard details being updated") @RequestBody DashboardConfig cfg) throws Exception {
        DashboardConfig c = this.getDashboardConfig();

        boolean runOpt = false;

        /* Set aggregation settings */
        if (c.getAggregationEnabled() != cfg.getAggregationEnabled()) {
            this.apService.setPortfolioAggregationEnabled(cfg.getAggregationEnabled());

            runOpt = true;
        }

        /* Set trading settings */
        if (c.getTradingEnabled() != cfg.getTradingEnabled()) {
            this.tradeService.setTradingEnabled(cfg.getTradingEnabled());
        }

        /* Set optimization settings */
        if (cfg.getOptimizationObjective() != null && ((c.getOptimizationObjective() != cfg.getOptimizationObjective()) ||
                                                       (c.getOptimizationFrom() != cfg.getOptimizationFrom()) ||
                                                       (c.getOptimizationTo() != cfg.getOptimizationTo()))) {
            OptimizationObjective obj = cfg.getOptimizationObjective();

            if (cfg.getOptimizationFrom() != null) {
                obj.setTimeIntervalFrom(FlexOffer.toFlexOfferTime(cfg.getOptimizationFrom()));
            }

            if (cfg.getOptimizationTo() != null) {
                obj.setTimeIntervalTo(FlexOffer.toFlexOfferTime(cfg.getOptimizationTo()));
            }

            this.optService.setOptimizationObjective(obj);

            runOpt = true;
        }

        /* Currency settings */
        if (cfg.getCurrencySymbol() != null && cfg.getCurrencyRatioToEur() != null) {
            this.currencyService.setCurrency(cfg.getCurrencySymbol(), cfg.getCurrencyRatioToEur());
        }

        if (runOpt) {
            this.optService.runImmediateOptimization();
        }

        return this.getDashboardConfig();
    }

}
