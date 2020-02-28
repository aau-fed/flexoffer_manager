/*
 * Created by bijay
 * GOFLEX :: WP2 :: core-api
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
 *  Last Modified 4/9/18 8:29 PM
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


import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

/**
 * This entity store aggregator contracts with prosumers
 * Created by bijay on 4/9/18.
 */
@Entity
@Table(name ="contract")
public class UserContract {
    public static UserContract DEFAULT = new UserContract();

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long userId;

    /**Price are in Euro (€) */

    /**
     * Aggregator agrees to pay a fixed monthly amount Prev (reward) to DER owner (Prosumer) if Prosumer is flexible,
     * issue flexoffers, and consume/produce energy according to the Aggregator’s schedules. This amount Prev is agreed prior
     * the contracted period and is invariant to actual flexibility offered by Prosumer.
     */
    private double fixedReward;

    /**
     * Aggregator agrees to pay a certain amount to Prosumer for each issued flexoffer, if the corresponding flexoffer schedule is followed.
     * The price for flexoffer is determined as follows using time flexibility and amount flexibility prices Ptf and Paf:
     */
    private double energyFlexReward;
    private double timeFlexReward;

    /**
     * Aggregator agrees to pay to Prosumer a certain amount that depends on (1) if the schedule flexoffer's schedule differs from the base load (default schedule)
     * specified in a Prosumer flexoffer, and (2) how much Aggregator’s schedule differs from the base load (default schedule) - this amount is computed based on two
     * “delta” components: Pdt and Pda, where Pdt is the start-time deviation price, Pda is the amount deviation price.
     */
    private double schedulingFixedReward;
    private double schedulingEnergyReward;
    private double schedulingStartTimeReward;

    /**
     * Allowed limit of imbalance between scheduled and actually consumed/produced energy for the FO to be considered correctly executed.
     * The imbalance is given normalized per 1 time interval (15 mins).
     *
     */
    private double imbalanceLimitPerTimeInterval;


    private Date lastUpdated = new Date();

    // Get the signature (hash code) for the contract values
    @JsonIgnore
    public int getContractSignature() {
        return Objects.hash(fixedReward, energyFlexReward, timeFlexReward, schedulingFixedReward, schedulingEnergyReward, schedulingStartTimeReward, isDefault, isValid);
    }

    private boolean isDefault;

    private boolean isValid = true;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public double getFixedReward() {
        return fixedReward;
    }

    public void setFixedReward(double fixedReward) {
        this.fixedReward = fixedReward;
    }

    public double getEnergyFlexReward() {
        return energyFlexReward;
    }

    public void setEnergyFlexReward(double energyFlexReward) {
        this.energyFlexReward = energyFlexReward;
    }

    public double getTimeFlexReward() {
        return timeFlexReward;
    }

    public void setTimeFlexReward(double timeFlexReward) {
        this.timeFlexReward = timeFlexReward;
    }

    public double getSchedulingEnergyReward() {
        return schedulingEnergyReward;
    }

    public void setSchedulingEnergyReward(double schedulingEnergyReward) {
        this.schedulingEnergyReward = schedulingEnergyReward;
    }

    public double getSchedulingFixedReward() {
        return schedulingFixedReward;
    }

    public void setSchedulingFixedReward(double schedulingFixedTimeReward) {
        this.schedulingFixedReward = schedulingFixedTimeReward;
    }

    public double getSchedulingStartTimeReward() {
        return schedulingStartTimeReward;
    }

    public void setSchedulingStartTimeReward(double schedulingStartTimeReward) {
        this.schedulingStartTimeReward = schedulingStartTimeReward;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }


    public double getImbalanceLimitPerTimeInterval() {
        return imbalanceLimitPerTimeInterval;
    }

    public void setImbalanceLimitPerTimeInterval(double imbalanceLimitPerTimeInterval) {
        this.imbalanceLimitPerTimeInterval = imbalanceLimitPerTimeInterval;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
