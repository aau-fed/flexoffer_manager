package org.goflex.wp2.fman.optimizer;

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
import org.goflex.wp2.fman.aggregatorportfolio.AggregatorPortfolio;
import org.goflex.wp2.fman.aggregatorportfolio.AggregatorPortfolioService;
import org.goflex.wp2.fman.common.configuration.PersistentConfigurationService;
import org.goflex.wp2.fman.optimizer.events.ReoptimizationNeededEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

/* This service handles FO aggregation, disaggregation */
@Configuration
@Service
public class OptimizationService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    static final String C_OPTIMIZATION_OBJ = "optimization.objective";
    static final String C_OPTIMIZATION_TIMEFROM = "optimization.time_from";
    static final String C_OPTIMIZATION_TIMETO = "optimization.time_to";

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private AggregatorPortfolioService portSvc;

    @Autowired
    private PersistentConfigurationService conf;

    public OptimizationObjective getOptimizationObjective() {
        OptimizationObjective obj = this.conf.getValue(C_OPTIMIZATION_OBJ)
                                        .map( s -> OptimizationObjective.valueOf(s))
                                        .orElse(OptimizationObjective.objLowestCost);
        obj.setTimeIntervalFrom(this.conf.getValue(C_OPTIMIZATION_TIMEFROM)
                                 .map(s -> { try {
                                        return Long.parseLong(s);
                                    } catch (Exception e) {
                                        return FlexOffer.toFlexOfferTime(new Date());
                                 }})
                                 .orElse(FlexOffer.toFlexOfferTime(new Date())));
        obj.setTimeIntervalTo(this.conf.getValue(C_OPTIMIZATION_TIMETO)
                               .map(s -> { try {
                                               return Long.parseLong(s);
                                           } catch (Exception e) {
                                               return FlexOffer.toFlexOfferTime(new Date());
                                           }} )
                               .orElse(FlexOffer.toFlexOfferTime(new Date())));

        return obj;
    }

    public void setOptimizationObjective(OptimizationObjective obj) {
        this.conf.setValue(C_OPTIMIZATION_OBJ, obj.toString());
        this.conf.setValue(C_OPTIMIZATION_TIMEFROM, String.valueOf(obj.getTimeIntervalFrom()));
        this.conf.setValue(C_OPTIMIZATION_TIMETO, String.valueOf(obj.getTimeIntervalTo()));
    }


    public OptimizationService() {  }


    @Scheduled(fixedDelayString = "${scheduling.delay:5000}", initialDelayString = "${scheduling.delay:5000}")
    public void runScheduling() {
        logger.info("Running periodic optimization.");
        this._runScheduling();
    }

    @Async
    @EventListener
    public void handleAggregatedScheduleUpdated(ReoptimizationNeededEvent evt)  {
        this._runScheduling();
    }

    // We want to rerun optimization, every time there's a change in the portfolio
    private int lastPortforlioHash = 0;

    private synchronized AggregatorPortfolio _runScheduling(){
        // Get the aggregated portfolio, for more efficient optimization
        AggregatorPortfolio p = null;
        try {
            p = portSvc.getActivePortfolio();
        } catch (Exception e) {
            logger.error("Portfolio error: ", e);
        }

        if (p != null) {
            /* Optimize the portfolio */
            boolean foundBetterSolution = false;
            int newPortfolioHash = p.getFlexOfferIdHash();

            Optimizer optimizer = new Optimizer(p, this.getOptimizationObjective());
            try {
                foundBetterSolution = optimizer.optimizePortfolio(true);
            } catch (Exception e) {
                logger.error("Error running optimization. Setting schedules to baseline: ", e);

                try {
                    p.getFoCollection().setSchedulesToBaseline();
                    foundBetterSolution = true;
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            /* (Disaggregate and) execute the portfolio */
            if (foundBetterSolution || newPortfolioHash != this.lastPortforlioHash) {
                this.lastPortforlioHash = newPortfolioHash;
                portSvc.executeSchedules(p);
            }
        }

        return p;
    }

    public void runDelayedOptimization(){
        this.publisher.publishEvent(new ReoptimizationNeededEvent(this));
    }

    public AggregatorPortfolio runImmediateOptimization() {
        return this._runScheduling();
    }

}
