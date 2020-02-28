package org.goflex.wp2.fman.billing;

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
import org.goflex.wp2.fman.common.exception.CustomException;
import org.goflex.wp2.fman.flexoffer.FlexOfferService;
import org.goflex.wp2.fman.measurements.MeasurementService;
import org.goflex.wp2.fman.user.UserService;
import org.goflex.wp2.fman.user.UserT;
import org.goflex.wp2.fman.user.usercontract.UserContactService;
import org.goflex.wp2.fman.user.usercontract.UserContract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@Service
public class BillingService {
    private static final Logger logger = LoggerFactory.getLogger(BillingService.class);

    @Autowired
    private UserService usrService;

    @Autowired
    private UserContactService contrService;

    @Autowired
    private FlexOfferService foService;

    @Autowired
    private MeasurementService measService;

    public UserBill generateUserBillForMonth(String userName, int year, int month) throws Exception {
        UserT user = this.usrService.getUserByUserName(userName);
        if(user == null){
            throw new CustomException("User not found", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        UserContract userContract = this.contrService.getContract(user.getUserId());
        if (userContract == null) {
            userContract = UserContract.DEFAULT;
            logger.warn("Prosumer bill was generated using the default contract.");
        }

        if(userName == null){
            throw new CustomException("User cannot be null", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // Retrieve all FOs
        Date startDate = new GregorianCalendar(year, month, 1, 0,0,0).getTime();
        Date endDate = new GregorianCalendar(year, month+1, 1, 0, 0, 0).getTime();
        List<FlexOffer> fos = this.foService.getFOsByUsernameAndMonth(userName, startDate, endDate);

        // Construct the FO collection
        FlexOfferCollection foCollection = new FlexOfferCollection(fos);

        // Retrieve all measurments in the period, -1 +1 TID
        TimeSeries measurements = this.measService.getUserMeasurementSeries(userName,
                                                                        foCollection.getMinTid(),
                                                                        foCollection.getMaxTid(),
                                                              true);

        return UserBillFactory.generateBill(userName, userContract, fos, measurements);
    }

    public MarketBill generateMarketBillForMonth(int year, int month) throws Exception {
        return null;
    }
}
