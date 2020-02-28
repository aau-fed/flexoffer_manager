/*
 * Created by bijay
 * GOFLEX :: WP2 :: foa-core
 * Copyright (c) 2018 The GoFlex Consortium
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining  a copy of this software and associated documentation
 *  files (the "Software") to deal in the Software without restriction,
 *  including without limitation the rights to use, copy, modify, merge,
 *  publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions: The above copyright notice and
 *  this permission notice shall be included in all copies or substantial
 *  portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON
 *  INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 *
 *  Last Modified 4/9/18 2:01 PM
 */

package org.goflex.wp2.fman.user.usercontract;

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



import org.goflex.wp2.core.entities.FlexOfferSlice;
import org.goflex.wp2.fman.common.exception.CustomException;
import org.goflex.wp2.fman.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * This is a passive component for exchanging Flex-Offer data with the aggregator
 */
@Service
public class UserContactService {

    @Autowired
    private UserContractRepository contractRepository;
    @Autowired
    private UserService userService;

    public UserContract getContract(long userId){
        return contractRepository.findByUserId(userId);
	}

	public UserContract saveContract(UserContract contract){
	    return contractRepository.saveAndFlush(contract);
    }

    public List<UserContract> getContractsByUserIds(List<Long> ids) {
        return contractRepository.findAllById(ids);
    }

    public UserContract update(UserContract userContract){

        if (userContract.getUserId() < 0) {
            throw new CustomException("UserID cannot be null", HttpStatus.BAD_REQUEST);
        }
        UserContract contract =  contractRepository.findByUserId(userContract.getUserId());
        if (contract == null) {
            throw new CustomException("Username not found.", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        UserContract oldContract = contractRepository.findByUserId(userContract.getUserId());
        oldContract.setEnergyFlexReward(userContract.getEnergyFlexReward());
        oldContract.setFixedReward(userContract.getFixedReward());
        oldContract.setSchedulingEnergyReward(userContract.getSchedulingEnergyReward());
        oldContract.setSchedulingFixedReward(userContract.getSchedulingFixedReward());
        oldContract.setTimeFlexReward(userContract.getTimeFlexReward());
        oldContract.setSchedulingStartTimeReward(userContract.getSchedulingStartTimeReward());
        oldContract.setImbalanceLimitPerTimeInterval(userContract.getImbalanceLimitPerTimeInterval());
        oldContract.setValid(userContract.isValid());
        oldContract.setLastUpdated(new Date());
        return contractRepository.save(oldContract);

    }


}