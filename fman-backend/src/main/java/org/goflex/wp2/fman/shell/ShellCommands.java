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


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.goflex.wp2.core.entities.*;
import org.goflex.wp2.fman.flexoffer.FlexOfferService;
import org.goflex.wp2.fman.inea_market.IneaTradingConfig;
import org.goflex.wp2.fman.inea_market.IneaTradingService;
import org.goflex.wp2.fman.inea_market.IneaTransactionHistoryT;
import org.goflex.wp2.fman.measurements.MeasurementService;
import org.goflex.wp2.fman.measurements.MeasurementT;
import org.goflex.wp2.fman.user.UserRole;
import org.goflex.wp2.fman.user.UserService;
import org.goflex.wp2.fman.user.UserT;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ShellComponent
public class ShellCommands {
    @Autowired
    private FlexOfferService foSvc;

    @Autowired
    private UserService userSvc;

    @Autowired
    private IneaTradingService tradingSvc;

    @Autowired
    private MeasurementService measService;



    @ShellMethod("Set the logging level for 'org.goflex.wp2' package only")
    public String loglevel(@ShellOption() String loglevel) {

        String packageName = "org.goflex.wp2";
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        if (loglevel.equalsIgnoreCase("ALL")) {
            loggerContext.getLogger(packageName).setLevel(Level.ALL);
            return "Logging level successfully set to " + loglevel;
        } else if (loglevel.equalsIgnoreCase("TRACE")) {
            loggerContext.getLogger(packageName).setLevel(Level.TRACE);
            return "Logging level successfully set to " + loglevel;
        } else if (loglevel.equalsIgnoreCase("DEBUG")) {
            loggerContext.getLogger(packageName).setLevel(Level.DEBUG);
            return "Logging level successfully set to " + loglevel;
        } else if (loglevel.equalsIgnoreCase("INFO")) {
            loggerContext.getLogger(packageName).setLevel(Level.INFO);
            return "Logging level successfully set to " + loglevel;
        } else if (loglevel.equalsIgnoreCase("WARN")) {
            loggerContext.getLogger(packageName).setLevel(Level.WARN);
            return "Logging level successfully set to " + loglevel;
        } else if (loglevel.equalsIgnoreCase("ERROR")) {
            loggerContext.getLogger(packageName).setLevel(Level.ERROR);
            return "Logging level successfully set to " + loglevel;
        } else if (loglevel.equalsIgnoreCase("OFF")) {
            loggerContext.getLogger(packageName).setLevel(Level.OFF);
            return "Logging level successfully set to " + loglevel;
        } else {
            return "Error, not a known loglevel: " + loglevel;
        }
    }


    @ShellMethod("Set the root logging level")
    public String loglevelroot(@ShellOption() String loglevel) {
        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME))
                .setLevel(ch.qos.logback.classic.Level.toLevel(loglevel));

        return  ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)).getLevel().toString();
    }

    // Authorization methods
    @ShellMethod("Generate a JWT for an arbitrary user")
    public String signin(@ShellOption() String username, @ShellOption String password) {
        return this.userSvc.signin(username, password);
    }

    @ShellMethod("Creates a user account without the GUI")
    public String signup(@ShellOption() String userName, @ShellOption String password,
                         @ShellOption(defaultValue="") String firstName, @ShellOption(defaultValue="") String lastName,
                         @ShellOption(defaultValue="") String email,
                         @ShellOption(defaultValue="ROLE_PROSUMER") String role) {
        UserT user = new UserT();
        user.setUserName(userName);
        user.setPassword(password);
        user.setFirstName(firstName);
        user.setFirstName(lastName);
        user.setEmail(email);
        user.setRole(UserRole.valueOf(role));

        return this.userSvc.signup(user);
    }

    // Flex-Offer generation methods
    @ShellMethod("This adds N consumption and N production FOs")
    public String fadd(@ShellOption() Integer n) {
        DemoFlexOffers d = new DemoFlexOffers();
        for (FlexOffer f : d.demo(n)) {
            this.foSvc.createFlexOffer(f);
        }
        return "Done!";
    }

    @ShellMethod("This adds N consumption time-flexible FOs only")
    public String fadd_tflex(@ShellOption() Integer n) {
        DemoFlexOffers d = new DemoFlexOffers();
        for (FlexOffer f : d.demo_consumption(n)) {
            for (int j = 0; j< f.getFlexOfferProfileConstraints().length; j++ ) {
                f.getFlexOfferProfileConstraint(j).getEnergyConstraintList()[0].setLower(f.getFlexOfferProfileConstraint(j).getEnergyConstraintList()[0].getUpper());
            }

            f.setStartBeforeInterval(f.getStartAfterInterval() + 1);

            this.foSvc.createFlexOffer(f);
        }
        return "Done!";
    }

    public String fadd_consumption(@ShellOption() Integer n) {
        DemoFlexOffers d = new DemoFlexOffers();
        for (FlexOffer f : d.demo_consumption(n)) {
            this.foSvc.createFlexOffer(f);
        }
        return "Done!";
    }

    public String fadd_production(@ShellOption() Integer n) {
        DemoFlexOffers d = new DemoFlexOffers();
        for (FlexOffer f : d.demo_production(n)) {
            this.foSvc.createFlexOffer(f);
        }
        return "Done!";
    }

    @ShellMethod("Clear Flex-Offers from a DB")
    public String fclear() {
        // Clear the FOs
        this.foSvc.getAllFlexOffers()
                .stream()
                .forEach( f-> this.foSvc.deleteFlexOffer(f.getFlexoffer()) );

        return "Done.";
    }


    @ShellMethod("Registers an energy amount measurement for the user with an ID ")
    public String madd(@ShellOption() String userName, @ShellOption() Double cummulativeEnergy) throws Exception {
        Date asOfNow = new Date();

        UserT u = this.userSvc.getUserByUserName(userName);

        if (u == null) {
            throw new Exception("User with ID "+ userName + " is not found!");
        }

        MeasurementT m = new MeasurementT();
        m.setUser(u);
        m.setUserName(userName);
        m.setTimeInterval(FlexOffer.toFlexOfferTime(asOfNow));
        m.setTimeStamp(asOfNow);
        m.setCumulativeEnergy(cummulativeEnergy);

        List<MeasurementT> ml = new ArrayList<MeasurementT>();
        ml.add(m);

        this.measService.postMeasurements(ml);

        return "Done.";
    }

    @ShellMethod("This adds a new market commitments (transactions with schedules)")
    public String cadd() {
        Date asOfNow = new Date();
        IneaTradingConfig conf = this.tradingSvc.getConfig();

        long tradingIntervalFrom = FlexOffer.toFlexOfferTime(asOfNow) + 1;
        long tradingIntervalTo = FlexOffer.toFlexOfferTime(asOfNow) + conf.getTradingHorizonInMultiplesOf15min();

        // this.tradingSvc.getMarketContract().
        IneaTransactionHistoryT t = new IneaTransactionHistoryT();

        t.setState(IneaTransactionHistoryT.TransactionState.tActive);
        t.setStateMessage("This is test transaction added by ADMIN");
        t.setIneaId(-1l);
        t.setActiveContract(this.tradingSvc.getMarketContract());
        t.setTradingIntervalFrom(tradingIntervalFrom);
        t.setTradingIntervalTo(tradingIntervalTo);

        // Generate the test trading bid
        FlexOffer f = new FlexOffer();
        f.setState(FlexOfferState.Initial);
        f.setOfferedById("AGGREGATOR");
        f.setStartAfterInterval(tradingIntervalFrom);
        f.setStartBeforeInterval(tradingIntervalFrom);
        f.setCreationTime(asOfNow);

        FlexOfferSlice[] slices = new FlexOfferSlice[(int) (tradingIntervalTo - tradingIntervalFrom + 1)];
        for (int i = 0; i< slices.length; i++) {
            FlexOfferSlice s = new FlexOfferSlice();

            s.setMinDuration(1);
            s.setMaxDuration(1);

            FlexOfferConstraint ctr = new FlexOfferConstraint();
            ctr.setLower(0);
            ctr.setUpper(10);

            s.setEnergyConstraintList(new FlexOfferConstraint[] { ctr } );
            s.setTariffConstraint(new FlexOfferTariffConstraint(-1.0, 1.0));

            slices[i] = s;
        }

        f.setFlexOfferProfileConstraints(slices);

        f.setDefaultSchedule(new FlexOfferSchedule(f));

        t.setTradingFlexOffer(f);


        FlexOfferSchedule ws = new FlexOfferSchedule(f);
        for (int i = 0; i<ws.getScheduleSlices().length; i++) {
            //ws.getScheduleSlice(i).setEnergyAmount(f.getFlexOfferProfileConstraint(i).getEnergyLower(0));
            ws.getScheduleSlice(i).setEnergyAmount(5.0);
        }

        f.setFlexOfferSchedule(ws);

        this.tradingSvc.addTestTransaction(t);

        return "Done.";
    }


}
