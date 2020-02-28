package org.goflex.wp2.fman.aggregatorportfolio;

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


import javafx.util.Pair;
import org.goflex.wp2.agg.api.AggregationException;
import org.goflex.wp2.core.entities.AggregatedFlexOffer;
import org.goflex.wp2.core.entities.FlexOffer;
import org.goflex.wp2.core.entities.FlexOfferSchedule;
import org.goflex.wp2.core.entities.TimeSeries;
import org.goflex.wp2.fman.aggregation.AggregationService;
import org.goflex.wp2.fman.common.configuration.PersistentConfigurationService;
import org.goflex.wp2.fman.flexoffer.FlexOfferService;
import org.goflex.wp2.fman.flexoffer.FlexOfferT;
import org.goflex.wp2.fman.flexoffer.events.ScheduleUpdatedEvent;
import org.goflex.wp2.fman.inea_market.IneaMarketCommitment;
import org.goflex.wp2.fman.inea_market.IneaMarketCommitmentService;
import org.goflex.wp2.fman.measurements.MeasurementService;
import org.goflex.wp2.fman.user.usercontract.UserContactService;
import org.goflex.wp2.fman.user.usercontract.UserContract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


/**
 * This service builds instances of the flex-offer portfolio.
 * It can return an original portfolio, or aggregated (this improved the scheduling performance, for many FOs).
 */
@Service
public class AggregatorPortfolioService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    static final String C_AGGREGATION_ENABLED = "portfolio.aggregationenabled";

    @Autowired
    private FlexOfferService foSvc;

    @Autowired
    private AggregationService aggSvc;

    @Autowired
    private MeasurementService measService;

    @Autowired
    private UserContactService contrSvc;

    @Autowired
    ApplicationEventPublisher publisher;

    @Autowired
    private PersistentConfigurationService conf;

    @Autowired
    private IneaMarketCommitmentService ineaCmtService;

    /* This caches the last optimized portfolio */
    private AggregatorPortfolio lastPortfolio = null;
    private Optional<Date> lastOptimizationTime = Optional.empty();
    private HashMap<UUID, FlexOfferSchedule> lastOptimizedSchedules = new HashMap<>();

    public AggregatorPortfolioService() {  }

    public boolean isPortfolioAggregationEnabled() {
        return this.conf.getValue(C_AGGREGATION_ENABLED)
                .map( s -> Boolean.valueOf(s))
                .orElse(true);
    }

    public void setPortfolioAggregationEnabled(boolean aggregationEnabled) {
        this.conf.setValue(C_AGGREGATION_ENABLED, String.valueOf(aggregationEnabled));
        // Clear out the cache
        this.lastPortfolio = null;
        this.lastOptimizedSchedules.clear();;
    }

    /* Main entrance to getting the active portfolio */
    public AggregatorPortfolio getActivePortfolio() throws Exception {
        // Choice between original and aggregated portfolio
        AggregatorPortfolio port = null;

        if (!this.isPortfolioAggregationEnabled()) {
            port = this._getActivePortfolio();
        } else {
            port = this._getAggregatedActivePortfolio();

            // Recover aggregated FO schedules for performance reasons.
            // Note, they are not presisted into a database.
            port.getFlexOffers().stream().forEach(f -> {
                    if (this.lastOptimizedSchedules.containsKey(f.getId())) {
                        FlexOfferSchedule s = this.lastOptimizedSchedules.get(f.getId());
                        if (s.isCorrect(f)) {
                            f.setFlexOfferSchedule(s);
                        }
                    }
            });
        }

        this.lastPortfolio = port;
        return port;
    }

    public AggregatorPortfolio getLastPortfolio() {
        return this.lastPortfolio;
    }

    /* Get's old optimized or new active portfolio */
    public AggregatorPortfolio getLastOrActivePortfolio() throws Exception {
        if (this.lastPortfolio != null)
            return this.lastPortfolio;
        else
            return this.getActivePortfolio();
    }

    public Optional<Date> getLastOptimizationTime() {
        return this.lastOptimizationTime;
    }

    /* Collect all data to form the portfolio */
    private AggregatorPortfolio _getActivePortfolio() throws Exception {

        // TODO: implement fixed expenses, if needed
        double fixedExpenses = 0.0;

        // Fetch FOTs and reduce the inactive flexibility
        List<FlexOfferT> fots = this.foSvc.getAllActiveFlexOffers()
                                          .stream()
                                          .map(ft -> {
                                              if (ft.getFlexoffer() != null) {
                                                  ft.setFlexoffer(this.foSvc.reduceInactiveFlexibility(ft.getFlexoffer()));
                                              }
                                              return ft;
                                          })
                                          .collect(Collectors.toList());

        // Extract FOs
        List<FlexOffer> fos = fots
                                .stream()
                                .map(ft -> ft.getFlexoffer())
                                .collect(Collectors.toList());

        // Extract unique user Ids, owning those FOs
        List<Long> userIds = fots
                                .stream()
                                .map(ft -> ft.getUser().getUserId()) // This allows accessing in "lazy mode", without fetching the entity
                                .distinct()
                                .collect(Collectors.toList());

        // Fetch all relevant user contracts
        Map<Long, UserContract> userContracts = this.contrSvc.getContractsByUserIds(userIds)
                                                      .stream()
                                                      .collect(Collectors.toMap(c -> c.getUserId(), c-> c));

        // Locate the contract for each FO
        Map<UUID, UserContract> foContracts = fots
                        .stream()
                        .collect(Collectors.toMap(ft -> ft.getFlexoffer().getId(),
                                                  ft -> userContracts.getOrDefault(ft.getUser().getUserId(), UserContract.DEFAULT )));


        Date nowTime = new Date();
        // Find all "time-ahead commitments"
        long intervalFrom = FlexOffer.toFlexOfferTime(nowTime);
        long intervalTo = Long.MAX_VALUE;
        List<IneaMarketCommitment> mk_commit = this.ineaCmtService.getCommitmentTransactions(intervalFrom, intervalTo, null /* No measurements should be used (for performance) */);


        // Build the portfolio
        AggregatorPortfolio port = new AggregatorPortfolio(fixedExpenses,
                                                           fos,
                                                           foContracts,
                                                           mk_commit);

        return port;
    }

    public AggregatorPortfolio getPortfolioInPeriod(Date timeFrom, Date timeTo, Long userId) throws Exception {
        // TODO: implement fixed expenses, if needed
        double fixedExpenses = 0.0;

        // Retrieve FOT's, either all or for a specific user
        List<FlexOfferT> fots = (userId != null ?
                            this.foSvc.getUserFOsCreatedBetweenDates(userId, timeFrom, timeTo) :
                            this.foSvc.getFOsCreatedBetweenDates(timeFrom, timeTo))
                .stream()
                .filter(f -> f.getFlexoffer() != null)
                .collect(Collectors.toList());

        // Generate a list of FOs
        List<FlexOffer> fos  = fots
                .stream()
                .map(ft -> ft.getFlexoffer())
                .collect(Collectors.toList());

        // Extract unique user Ids, owning those FOs
        List<Long> userIds = fots
                .stream()
                .map(ft -> ft.getUser().getUserId()) // This allows accessing in "lazy mode", without fetching the entity
                .distinct()
                .collect(Collectors.toList());

        // Fetch all relevant user contracts
        Map<Long, UserContract> userContracts = this.contrSvc.getContractsByUserIds(userIds)
                .stream()
                .collect(Collectors.toMap(c -> c.getUserId(), c-> c));

        // Locate the contract for each FO
        Map<UUID, UserContract> foContracts = fots
                .stream()
                .collect(Collectors.toMap(ft -> ft.getFlexoffer().getId(),
                        ft -> userContracts.getOrDefault(ft.getUser().getUserId(), UserContract.DEFAULT )));


        long intervalFrom = FlexOffer.toFlexOfferTime(timeFrom);
        long intervalTo = FlexOffer.toFlexOfferTime(timeTo);

        // Retrieve measurements
        TimeSeries measurements = this.measService.getAggregatedMeasurementSeries(intervalFrom, intervalTo, true);

        // Get commitment transactions
        List<IneaMarketCommitment> mk_commit = this.ineaCmtService.getCommitmentTransactions(intervalFrom, intervalTo, measurements);

        return new AggregatorPortfolio(fixedExpenses,
                fos,
                foContracts,
                mk_commit);
    }

    private AggregatorPortfolio _getAggregatedActivePortfolio() throws Exception {
        return this.aggregatePorfolio(this._getActivePortfolio());
    }

    /* Aggregates the portfolio (the flex-offers and contracts inside the portfolio) */
    public AggregatorPortfolio aggregatePorfolio(AggregatorPortfolio initialPortfolio) throws Exception {
        // Step 1: Aggregate all flex-offers

        List<FlexOffer> fos = initialPortfolio.getFlexOffers();

        /* Initial native pre-grouping */
        Map<Pair<Integer, Integer>, List<FlexOffer>> pre_groups =
                fos.stream()
                   .collect(
                        Collectors.groupingBy(f ->
                                new Pair<Integer, Integer>(
                                        // Group by consumption/ production type
                                        (f.isConsumption() && f.isProduction()) ? 0 : (f.isConsumption() ? -1 : 1),
                                        // Group by contract values
                                        initialPortfolio.getFoContMap().get(f.getId()).getContractSignature()
                                )));

        // Apply aggregation on every pre-group
        List<AggregatedFlexOffer> afos = pre_groups
                .entrySet()
                .stream()
                .flatMap(kv -> {
                    try {
                        return this.aggSvc.aggregateFlexOffers(kv.getValue()
                                .stream()
                                .collect(Collectors.toList()))
                                .stream();
                    } catch (AggregationException e) { return new ArrayList<AggregatedFlexOffer>().stream();  }
                })
                .collect(Collectors.toList());


        // Apply aggregation / assignment of the contracts
        Map<UUID, UserContract> afoContracts = afos.stream()
                                                    .collect(Collectors.toMap(
                                                            af-> af.getId(),
                                                            af-> this.aggSvc.aggregateUserContracts(
                                                                    Arrays.stream(af.getSubFlexOffers())
                                                                                    .map(f-> initialPortfolio.getFoContMap()
                                                                                                             .getOrDefault(f.getId(), UserContract.DEFAULT))
                                                                                    .collect(Collectors.toList()))));

        /* Build a new portfolio */
        AggregatorPortfolio ap = new AggregatorPortfolio(initialPortfolio.getFixedExpences(),
                                                         afos,
                                                         afoContracts,
                                                         initialPortfolio.getMarketCommitments());

        return ap;
    }

    /* Execute schedules, having to disaggregate the flex-offers, if needed */
    public void executeSchedules(AggregatorPortfolio aggPortfolio) {
        // Stores, for later quick retrieval
//        this.lastPortfolio = aggPortfolio;
        this.lastOptimizationTime = Optional.of(new Date());
        this.lastOptimizedSchedules.clear();


        aggPortfolio.getFlexOffers()
                    .stream()
                    .forEach(f -> {

                        /* Remember their schedules (in memory) */
                        if (f.getFlexOfferSchedule() != null) {
                            this.lastOptimizedSchedules.put(f.getId(), f.getFlexOfferSchedule().clone());
                        }

                        /* Disaggregate only aggregated FOs */
                        if (f instanceof AggregatedFlexOffer) {
                            // Aggregated FO
                            AggregatedFlexOffer af = (AggregatedFlexOffer) f;

                            try {
                                this.aggSvc.disaggregateAFlexOffer(af);
                            } catch (AggregationException e) {
                                logger.error("Error disaggregating a FlexOffer with Id {}:", af.getId(), e);
                            }

                            /* Publish a system event on disaggregation */
                            af.getSubFoMetas()
                              .stream()
                              .forEach(m -> {
                                  this.publisher.publishEvent(new ScheduleUpdatedEvent(this,
                                                                                        m.getSubFlexOffer().getId(),
                                                                                        m.getSubFlexOffer().getFlexOfferSchedule()));
                              });
                        } else {
                            // Non-aggregated FO
                            this.publisher.publishEvent(new ScheduleUpdatedEvent(this,
                                                                                        f.getId(),
                                                                                        f.getFlexOfferSchedule()));
                        }
                    });

    }

}
