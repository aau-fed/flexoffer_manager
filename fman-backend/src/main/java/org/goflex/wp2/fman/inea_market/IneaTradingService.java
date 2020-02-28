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


import org.goflex.wp2.core.entities.*;
import org.goflex.wp2.core.wrappers.OperationInformation;
import org.goflex.wp2.fman.aggregatorportfolio.AggregatorPortfolio;
import org.goflex.wp2.fman.aggregatorportfolio.AggregatorPortfolioService;
import org.goflex.wp2.fman.common.configuration.PersistentConfigurationService;
import org.goflex.wp2.fman.inea_market.events.IneaMarketEvent;
import org.goflex.wp2.fman.inea_market.marketcontract.IneaMarketContract;
import org.goflex.wp2.fman.measurements.MeasurementService;
import org.goflex.wp2.fman.optimizer.OptimizationObjective;
import org.goflex.wp2.fman.optimizer.OptimizationService;
import org.goflex.wp2.fman.optimizer.Optimizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.LongStream;

@Service
public class IneaTradingService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    static final String C_CONTRACT_IMBALANCE_FEE = "ineamarket.contract.imbalanceprice";
    static final String C_CONTRACT_IMBALANCE_CUTOFF = "ineamarket.contract.imbalancecutoff";
    static final String C_TRADING_ENABLED = "ineamarket.enabled";
    static final String C_TRADING_LASTERROR = "ineamarket.lasterror";
    static final String C_BID_PROPORTIONAL_MARGIN = "ineamarket.bid.proportionalMargin";
    static final String C_BID_PERKWH_MARGIN = "ineamarket.bid.perkWhMargin";
    static final String C_TRADING_FREQUENCY = "ineamarket.trading.frequencey";
    static final String C_TRADING_HORIZON = "ineamarket.trading.horizon";
    static final String C_BID_UPDATEPOLICY_ABSOLUTEENERGYCHANGE = "ineamarket.bid.updatepolicy.absEnergyChange";

    @Value("${fman.inea.offeredById}")
    private String INEA_OFFERERBY_ID;

    /* Defines whether the INEA service is enabled */
    private boolean enabled = true;

    // Date format for formatting FO labels
    private DateFormat _dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private OptimizationService optSvc;

    @Autowired
    private IneaTransactionHistoryRepository histRepo;

    @Autowired
    private AggregatorPortfolioService portSvc;

    @Autowired
    private PersistentConfigurationService confSvc;

    @Autowired
    private MeasurementService measurementService;

    @Autowired
    private IneaMarketAPIService marketSvc;

    public IneaTradingService () {
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


    public boolean isTradingEnabled() {
        return this.confSvc.getValue(C_TRADING_ENABLED)
                           .map(s -> Boolean.parseBoolean(s))
                           .orElse(false);
    }

    public void setTradingEnabled(boolean tradingEnabled) {
        this.confSvc.setValue(C_TRADING_ENABLED, String.valueOf(tradingEnabled));

        this.setEnabled(tradingEnabled);
    }

    public IneaTradingConfig getConfig() {
        IneaTradingConfig cfg = new IneaTradingConfig();

        cfg.setTradingEnabled(this.isTradingEnabled());

        cfg.setProportionalBidMargin(this.confSvc.getValue(C_BID_PROPORTIONAL_MARGIN)
                                                 .map(s -> Double.valueOf(s))
                                                 .orElse(1.05));
        cfg.setBidMarginPerDeltaKwh(this.confSvc.getValue(C_BID_PERKWH_MARGIN)
                                                .map(s -> Double.valueOf(s))
                                                .orElse(0.05));
        cfg.setTradingFrequencyInMultiplesOf15min(this.confSvc.getValue(C_TRADING_FREQUENCY)
                                                .map(s -> Integer.valueOf(s))
                                                .orElse(1));
        cfg.setTradingHorizonInMultiplesOf15min(this.confSvc.getValue(C_TRADING_HORIZON)
                                                .map(s -> Integer.valueOf(s))
                                                .orElse(1));
        cfg.setBidUpdatePolicyOnAbsoluteEnergyChange(this.confSvc.getValue(C_BID_UPDATEPOLICY_ABSOLUTEENERGYCHANGE)
                                                                 .map(s -> Double.valueOf(s))
                                                                 .orElse((new IneaTradingConfig()).getBidUpdatePolicyOnAbsoluteEnergyChange()));

        cfg.setContractImbalanceFee(this.confSvc.getValue(C_CONTRACT_IMBALANCE_FEE)
                                                .map(s -> Double.valueOf(s))
                                                .orElse((new IneaTradingConfig()).getContractImbalanceFee()));

        cfg.setContractCutOffAmount(this.confSvc.getValue(C_CONTRACT_IMBALANCE_CUTOFF)
                                                .map(s -> Double.valueOf(s))
                                                .orElse((new IneaTradingConfig()).getContractCutOffAmount()));
        return cfg;
    }

    public void setConfig(IneaTradingConfig cfg) {
        this.confSvc.setValue(C_TRADING_FREQUENCY, String.valueOf(cfg.getTradingFrequencyInMultiplesOf15min()));
        this.confSvc.setValue(C_TRADING_HORIZON, String.valueOf(cfg.getTradingHorizonInMultiplesOf15min()));
        this.confSvc.setValue(C_BID_PROPORTIONAL_MARGIN, String.valueOf(cfg.getProportionalBidMargin()));
        this.confSvc.setValue(C_BID_PERKWH_MARGIN, String.valueOf(cfg.getBidMarginPerDeltaKwh()));
        this.confSvc.setValue(C_BID_UPDATEPOLICY_ABSOLUTEENERGYCHANGE, String.valueOf(cfg.getBidUpdatePolicyOnAbsoluteEnergyChange()));
        this.confSvc.setValue(C_CONTRACT_IMBALANCE_FEE, String.valueOf(cfg.getContractImbalanceFee()));
        this.confSvc.setValue(C_CONTRACT_IMBALANCE_CUTOFF, String.valueOf(cfg.getContractCutOffAmount()));

        this.setTradingEnabled(cfg.getTradingEnabled());
    }

    public String getLastTradingError() {
        return this.confSvc.getValue(C_TRADING_LASTERROR)
                           .orElse("");
    }

    public void setLastTradingError(String error) {
        this.confSvc.setValue(C_TRADING_LASTERROR, error);
    }


    @PostConstruct
    public void init() {
        this.setEnabled(this.isTradingEnabled());
    }


    public IneaMarketContract getMarketContract() {
        IneaMarketContract c = new IneaMarketContract();
        c.setImbalanceFee(confSvc.getValue(C_CONTRACT_IMBALANCE_FEE)
                                 .map(s -> Double.valueOf(s))
                                 .orElse(1e6));
        c.setImbalanceCutOffMargin(confSvc.getValue(C_CONTRACT_IMBALANCE_CUTOFF)
                .map(s -> Double.valueOf(s))
                .orElse(1.0));
        return c;
    }

    /**
     * The trading idea is as follow:
     *  if crossing the boundary at the time interval t0, we send a new trading offer to INEA's FMAR for the period t1-t2 (e.g., 15 mins only).
     *  Withing the interval t0-t1 - we continuously send updates for the offer
     *
     */
    private Optional<IneaTransactionHistoryT> _getLastTransaction() {
        List<IneaTransactionHistoryT> hist = this.histRepo.findTop1ByOrderByTransactionTimeDesc();

        if (hist.size() == 1) {
            return Optional.of(hist.get(0));
        } else {
            return Optional.empty();
        }
    }

    /**
     *
     * @return last transaction whose schedulingTime is not null
     */
    private Optional<IneaTransactionHistoryT> _getLastScheduledTransaction() {
        List<IneaTransactionHistoryT> hist = this.histRepo.findTop1ByOrderBySchedulingTimeDesc();

        if (hist.size() == 1) {
            return Optional.of(hist.get(0));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Gets active trading transaction
     * @return
     */
    public Optional<IneaTransactionHistoryT> getActiveTransaction(Date asOfTime) {
        long currentInterval = FlexOffer.toFlexOfferTime(asOfTime);

        // Get last trading offer, if any
        Optional<IneaTransactionHistoryT> lastTransacton = this._getLastTransaction();

        /* Initiate a new offer / or update the previous one */
        if (lastTransacton.isPresent() && lastTransacton.get().getTradingFlexOffer() != null) {
            /* Check if the trans is for the current time interval */
            IneaTransactionHistoryT trans = lastTransacton.get();
            if (trans.getTradingFlexOffer().getCreationInterval() >= currentInterval &&
                trans.getTradingFlexOffer().getCreationInterval() < currentInterval + this.getConfig().getTradingFrequencyInMultiplesOf15min()) {
                return lastTransacton;
            }
        }

        return Optional.empty();
    }

    /**
     * Gets executing transaction
     * @return
     */
    public Optional<IneaTransactionHistoryT> getExecutingTransaction(Date asOfTime) {
        long currentInterval = FlexOffer.toFlexOfferTime(asOfTime);

        // Get last trading offer, if any
        Optional<IneaTransactionHistoryT> lastTransacton = this._getLastScheduledTransaction();

        /* Initiate a new offer / or update the previous one */
        if (lastTransacton.isPresent() && lastTransacton.get().getTradingFlexOffer() != null) {
            /* Check if the trans is for the current time interval */
            IneaTransactionHistoryT trans = lastTransacton.get();
            /* Get schedule */
            FlexOfferSchedule schedule = trans.getTradingFlexOffer().getFlexOfferSchedule();

            if (schedule != null && schedule.getStartInterval() >= currentInterval && schedule.getEndInterval() <= currentInterval) {
                return lastTransacton;
            }
        }

        return Optional.empty();
    }

    @Scheduled(cron = "${fman.inea.tradingTime}")
    public void runTradeIteration() throws Exception {

        // Do nothing, if trading is disabled
        if (!this.isTradingEnabled()) return;

        logger.info("Running periodic trading iteration.");

        Date now = new Date();

        // If no previous offer, or time boundary crossed, generate a new offer
        try {

            // Make or update the offer
            this.makeOrUpdateTradingOffer(now, this.getActiveTransaction(now), false);

        } catch (Exception ex) {
            this.setLastTradingError(ex.getMessage());
        }

    }

    public void manuallySendBidIn() throws Exception {
        logger.info("Manually sending a trading bid. Please do not repeat this operation too often, as this will flood FMAR with messages.");

        Date now = new Date();

        // If no previous offer, or time boundary crossed, generate a new offer
        try {

            // Make or update the offer
            this.makeOrUpdateTradingOffer(now, this.getActiveTransaction(now), true);

        } catch (Exception ex) {
            this.logger.error("Error in the trading routines", ex);
            this.setLastTradingError(ex.getMessage());
        }
    }


    private void makeOrUpdateTradingOffer(Date asOfTime, Optional<IneaTransactionHistoryT> prevTransaction, boolean forceSendBidIn) throws Exception {
        IneaTradingConfig config = this.getConfig();
        long currentInterval = FlexOffer.toFlexOfferTime(asOfTime);
        long tradingIntervalStart = currentInterval + 1;
        long tradingIntervalEnd = currentInterval + config.getTradingHorizonInMultiplesOf15min();

        FlexOffer tradingOffer = null;
        try {
            tradingOffer = this._computeTradingFlexOffer(currentInterval,
                    tradingIntervalStart,
                    tradingIntervalEnd,
                    config);
        } catch (Exception ex) {
            logger.error("No trading bid (Flex-Offer) was generated. Trading is not possible. Error:" + ex.getMessage());
            return;
        }
        // OK, the offer was accepted. Let's store it


        if (!prevTransaction.isPresent()) {

            logger.info("No previous bid present. Sending in a fresh new bid to FMAR.");

            // Trade this as a new offer
            IneaTransactionHistoryT t = new IneaTransactionHistoryT();
            t.setTransactionTime(asOfTime);
            t.setTradingFlexOffer(tradingOffer);
            t.setActiveContract(this.getMarketContract());
            t.setTradingIntervalFrom(tradingIntervalStart);
            t.setTradingIntervalTo(tradingIntervalEnd);

            Optional<Long> ineaId = this.marketSvc.sendNewFlexOffer(tradingOffer);

            t.setIneaId(ineaId.orElseGet(null));
            t.setState(this.marketSvc.isINEAresponseOK(ineaId) ? IneaTransactionHistoryT.TransactionState.tActive : IneaTransactionHistoryT.TransactionState.tCancelled);
            t.setStateMessage(this.marketSvc.decodeINEAresponse(ineaId));

            this.histRepo.saveAndFlush(t);


        } else {


            if (!forceSendBidIn) {

                // Check, if the offer has already been scheduled. If so, stop the update
                if (IneaMarketCommitment.isCommitmentTransaction(prevTransaction.get())) {
                    logger.info("The previous bid has a schedule assigned, forming a commitment. No bid update is triggered.");
                    return;
                }

                // Check, if the offer has changed
                if (!this._hasOfferChanged(prevTransaction.get().getTradingFlexOffer(), tradingOffer, config)) {
                    logger.info("The previous bid still intact with the current loads available. No bid update is triggered.");
                    return;
                }
            }

            logger.info("A previous bid exists. Sending a bid update.");

            // Store a new transaction
            IneaTransactionHistoryT t = new IneaTransactionHistoryT();
            t.setTransactionTime(asOfTime);
            t.setTradingFlexOffer(tradingOffer);
            t.setActiveContract(this.getMarketContract());
            t.setTradingIntervalFrom(tradingIntervalStart);
            t.setTradingIntervalTo(tradingIntervalEnd);

            Optional<Long> ineaId = this.marketSvc.updateExistingFlexOffer(prevTransaction.get().getIneaId(), tradingOffer);

            t.setIneaId(ineaId.orElseGet(null));
            t.setState(this.marketSvc.isINEAresponseOK(ineaId) ? IneaTransactionHistoryT.TransactionState.tActive : IneaTransactionHistoryT.TransactionState.tCancelled);
            t.setStateMessage(this.marketSvc.decodeINEAresponse(ineaId));

            this.histRepo.saveAndFlush(t);


            // Mark the other transaction as inactive
            prevTransaction.get().setState(IneaTransactionHistoryT.TransactionState.tCancelled);
            this.histRepo.saveAndFlush(prevTransaction.get());
        }
    }

    /* This is to be used for testing of functionality */
    public void addTestTransaction(IneaTransactionHistoryT t) {
        Date asOfTime = new Date();

        t.setTransactionTime(asOfTime);

        this.histRepo.saveAndFlush(t);
    }

    private boolean _hasOfferChanged(FlexOffer oldFo, FlexOffer newFo, IneaTradingConfig config) {

        if (oldFo.getFlexOfferProfileConstraints().length != newFo.getFlexOfferProfileConstraints().length)
            return true;


        /* Compute the different between old and new offers */
        double absDiff = 0;

        for (int i = 0; i< oldFo.getFlexOfferProfileConstraints().length; i++) {
            FlexOfferSlice s1 = oldFo.getFlexOfferProfileConstraint(i);
            FlexOfferSlice s2 = newFo.getFlexOfferProfileConstraint(i);

            absDiff += Math.abs(s1.getEnergyLower(0) - s2.getEnergyLower(0));
            absDiff += Math.abs(s1.getEnergyUpper(0) - s2.getEnergyUpper(0));

            if (absDiff >= config.getBidUpdatePolicyOnAbsoluteEnergyChange())
                return true;

        }

        return false;
    }


    public FlexOffer computeTradingOffer() throws Exception {
        long currentInterval = FlexOffer.toFlexOfferTime(new Date());
        return this._computeTradingFlexOffer(currentInterval, currentInterval + 1, currentInterval + 2, this.getConfig());
    }


    private FlexOffer _computeTradingFlexOffer(
            long currentInterval,
            long tradingIntervalStart,
            long tradingIntervalStop,
            IneaTradingConfig config) throws Exception {


        /* Get the current portfolio, contract, and config */
        AggregatorPortfolio port = this.portSvc.getActivePortfolio();
        IneaMarketContract contract = this.getMarketContract();

        if (port.getFlexOffers().size() == 0) {
            throw new Exception("No FlexOffers available for generating a meaningful market offer.");
        }

        // First, optimize the portfolio for the lowest cost
        OptimizationObjective obj = OptimizationObjective.objLowestCost;
        Optimizer opt = new Optimizer(port, obj);
        opt.optimizePortfolio(false);
        TimeSeries def_schedule = port.getFoCollection().getScheduledEnergy();
        double def_cost = port.computeTotalPortfolioCost();

        // Next, optimize the portfolio for the max energy in the trading interval
        obj = OptimizationObjective.objEnergyMax;
        obj.setTimeIntervalFrom(tradingIntervalStart);
        obj.setTimeIntervalTo(tradingIntervalStop);
        opt.setObj(obj);
        opt.optimizePortfolio(false);
        double upreg_cost = port.computeTotalPortfolioCost();
        TimeSeries upreg_schedule = port.getFoCollection().getScheduledEnergy();
        TimeSeries upreg_DeltaSchedule = upreg_schedule.minus(def_schedule);
        double upreg_deltaEnergy = LongStream.rangeClosed(tradingIntervalStart, tradingIntervalStop)
                .mapToDouble(tid -> upreg_DeltaSchedule.getOptValue(tid).orElse(0.0))
                .sum();



        // Next, optimize the portfolio for the min energy in the trading interval
        obj = OptimizationObjective.objEnergyMin;
        obj.setTimeIntervalFrom(tradingIntervalStart);
        obj.setTimeIntervalTo(tradingIntervalStop);
        opt.setObj(obj);
        opt.optimizePortfolio(false);
        double downreg_cost = port.computeTotalPortfolioCost();
        TimeSeries downreg_schedule = port.getFoCollection().getScheduledEnergy();
        TimeSeries downreg_DeltaSchedule = def_schedule.minus(downreg_schedule);
        double downreg_deltaEnergy = LongStream.rangeClosed(tradingIntervalStart, tradingIntervalStop)
                                            .mapToDouble(tid -> downreg_DeltaSchedule.getOptValue(tid).orElse(0.0))
                                            .sum();


        // Generate the bid flex-offer
        FlexOffer bidFo = new FlexOffer();
        bidFo.setLabel(String.format("Trading FlexOffer: %s - %s",
                this._dateFormat.format(FlexOffer.toAbsoluteTime(tradingIntervalStart)),
                this._dateFormat.format(FlexOffer.toAbsoluteTime(tradingIntervalStop))));
        bidFo.setOfferedById(this.INEA_OFFERERBY_ID);
        bidFo.setCreationTime(FlexOffer.toAbsoluteTime(currentInterval));
        bidFo.setAcceptanceBeforeInterval(tradingIntervalStart);
        bidFo.setAssignmentBeforeInterval(tradingIntervalStart);
        bidFo.setStartAfterInterval(tradingIntervalStart);
        bidFo.setStartBeforeInterval(tradingIntervalStart);

        FlexOfferSlice[] slices = new FlexOfferSlice[(int) (tradingIntervalStop - tradingIntervalStart + 1)];
        bidFo.setFlexOfferProfileConstraints(slices);
        bidFo.setDefaultSchedule(new FlexOfferSchedule());
        bidFo.getDefaultSchedule().setStartInterval(tradingIntervalStart);
        bidFo.getDefaultSchedule().setScheduleSlices(new FlexOfferScheduleSlice[slices.length]);
        bidFo.setFlexOfferSchedule(null); /* No schedule is assigned */

        for(int i=0; i < slices.length; i++) {
            // Calculate Tid
            long tid = tradingIntervalStart + i;

            FlexOfferSlice s = new FlexOfferSlice();
            slices[i] = s;
            s.setMinDuration(1);
            s.setMaxDuration(1);

            // Set FO lower and upper bounds
            FlexOfferConstraint c = new FlexOfferConstraint(// Lower-bound
                                                            downreg_schedule.getOptValue(tid).orElse(
                                                            def_schedule.getOptValue(tid).orElse(0.0)),

                                                            // Upper-bound
                                                            upreg_schedule.getOptValue(tid).orElse(
                                                                    def_schedule.getOptValue(tid).orElse(0.0)));

            if (c.getLower() >= c.getUpper()) {
                throw new Exception("No flexibility is available in the market bid offer.");
            }

            s.setEnergyConstraintList(new FlexOfferConstraint[] { c });

            double minTariff = -1.0 * (config.getBidMarginPerDeltaKwh() + config.getProportionalBidMargin() * (downreg_deltaEnergy > 0 ? Math.max(0, (downreg_cost - def_cost) / downreg_deltaEnergy) : 0.0));
            double maxTariff = config.getBidMarginPerDeltaKwh() + config.getProportionalBidMargin() * (upreg_deltaEnergy > 0 ? Math.max(0, (upreg_cost - def_cost) / upreg_deltaEnergy) : 0.0);

            s.setTariffConstraint(new FlexOfferTariffConstraint(minTariff, maxTariff));

            // Set the default schedule
            bidFo.getDefaultSchedule().getScheduleSlices()[i] = new FlexOfferScheduleSlice(
                    Math.max(c.getLower(), Math.min(c.getUpper(), def_schedule.getOptValue(tid).orElse(0.0))),
                    0.0 /* INEA does not use that */);
        }

        return bidFo;
    }


    // Operation data and heart-beat handling
    public int getOperationState() {
        // Gregor explanation :
        // 0 - offline, 1 - online, 2 - FO sent, 3 - assigned, 4 - in adaptation
        // operationState 1: Online but no FO submitted.
        // operationState 2: FO submitted and waiting for the schedule.
        // operationState 3: Schedule received and waiting for execution.
        // operationState 4: Schedule is being executed.
        int state = this.enabled ? 1 : 0;

        Date now = new Date();

        Optional<IneaTransactionHistoryT> activeTr = this.getActiveTransaction(now);
        Optional<IneaTransactionHistoryT> executingTr = this.getExecutingTransaction(now);

        if (activeTr.isPresent() && !executingTr.isPresent()) {
            state = 2;

            if (IneaMarketCommitment.isCommitmentTransaction(activeTr.get())) {
                state = 3;
            }
        }

        if (executingTr.isPresent()) {
            state = 4;
        }

        return state;
    }

    public OperationInformation getOperationalInfo() {

        Date nowTime = new Date();

        OperationInformation op = new OperationInformation();
        op.setOperationState(this.getOperationState());

        op.setPriority(1);
        op.setIntervalLength(FlexOffer.numSecondsPerInterval);

        // Operation power
        try {
            double power = this.measurementService.getCurrentOperatingPower();
            // Note, on INEA's power/ energy is reversed
            power *= -1.0;
            ConsumptionTuple operationPower = new ConsumptionTuple(new Date(), power);
            op.setOperationPower(new ConsumptionTuple[]{operationPower});
        } catch(Exception e) {
            op.setOperationPower(new ConsumptionTuple[]{});
        }

        // Accounting power
        try {

            // Changed the length of operation power array on INEA request
            TimeSeries ts = this.measurementService.getAggregatedMeasurementSeries(
                    FlexOffer.toFlexOfferTime(nowTime) - this.marketSvc.getIneaOperationDataLength(),
                    FlexOffer.toFlexOfferTime(nowTime), true);

            // Note, on INEA's energy is reversed
            ts = ts.mul(-1.0);

            // Now, we convert energy (kWh/15min) to power (kWh / h)
            ts = ts.mul(4.0);

            // temporarily sending operational power as accounting power on inea request
            op.setAccountingPower(ConsumptionTuple.timeseries2consumptiontuples(ts, false));

        } catch (Exception e) {
            op.setAccountingPower(new ConsumptionTuple[]{});
        }

        // Generate prognosis series
        try {
            AggregatorPortfolio port = this.portSvc.getActivePortfolio();
            TimeSeries ts = port.getFoCollection().getScheduledEnergy();

            // Crop a time series
            ts = ts.subSeries(FlexOffer.toFlexOfferTime(nowTime), ts.getEndInterval());

            if (ts != null) {
                ts= new TimeSeries(FlexOffer.toFlexOfferTime(nowTime), new double[] {});
            }

            // Note, on INEA's energy is reversed
            ts = ts.mul(-1.0);

            op.setOperationPrognosis(ConsumptionTuple.timeseries2consumptiontuples(ts, true));
        } catch (Exception e) {
            op.setOperationPrognosis(new ConsumptionTuple[]{});
        }

        return op;
    }

    @Scheduled(fixedDelayString = "${fman.inea.heartbeat.frequency}")
    public void sendHeartBeatAndCheckSchedules() {
        if (this.enabled) {
            OperationInformation opInfo = this.getOperationalInfo();
            this.marketSvc.sendHeartBeat(opInfo);
        }
    }

    @EventListener
    private void handleMarketEvent(IneaMarketEvent evt) {
        if (evt.getType() == IneaMarketEvent.IneaEventType.evtScheduleAvailable) {

            /* Persist the schedule */
            List<IneaTransactionHistoryT> tl = this.histRepo.findTop1ByIneaIdOrderByTransactionTimeDesc(evt.getIneaId());

            if (tl.isEmpty()) {
                logger.error("Schedule received from the market, but no associated trading offer was found in the database.");
            } else {
                IneaTransactionHistoryT t = tl.get(0);

                t.getTradingFlexOffer().setFlexOfferSchedule(evt.getSchedule());
                t.setStateMessage("Flex-Offer was scheduled");
                t.setSchedulingTime(new Date());

                this.histRepo.saveAndFlush(t);

            }

        }
    }

}
