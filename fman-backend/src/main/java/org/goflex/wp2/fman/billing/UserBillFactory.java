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
import org.goflex.wp2.core.entities.FlexOfferSlice;
import org.goflex.wp2.core.entities.TimeSeries;
import org.goflex.wp2.fman.user.usercontract.UserContract;
import org.springframework.stereotype.Service;

import java.util.Collection;


/**
 * A class for generating Aggregator's bills 
 * 
 * @author Laurynas
 *
 */
@Service
public class UserBillFactory {
		
	/**
	 * Checks if a flexoffer is exectured correctly  
	 */
	public static boolean isExecutedCorrectly(FlexOffer fo, UserContract contract, TimeSeries userMeasurements) {
		// No schedule is given, so FO is correctly executed
		if (fo.getFlexOfferSchedule() == null || fo.getFlexOfferSchedule().getScheduleSlices().length == 0) {
			return true;
		} else {
			return fo.getFlexOfferSchedule().computeImbalance(userMeasurements, 0.0) /
					fo.getFlexOfferSchedule().getScheduleSlices().length <= contract.getImbalanceLimitPerTimeInterval();
		}
	}

	
	/* This calculates the expected value of a given a user contract and a flexoffer */
	public static double getFlexOfferExpectedValue(UserContract contract, FlexOffer f) {
		double value = 0;
		
		/* Add time flexibility part */
		value += contract.getTimeFlexReward() * (f.getStartBeforeInterval() - f.getStartAfterInterval());
		/* Add energy flexibility part */
		for(FlexOfferSlice s : f.getFlexOfferProfileConstraints()) {
			value += contract.getEnergyFlexReward() * (s.getEnergyUpper(0) - s.getEnergyLower(0));
		}
		
		value += getFlexOfferSchedulingCost(contract, f);			
		
		return value;
	}
	
	public static double getFlexOfferSchedulingCost(UserContract contract, FlexOffer f) {
		double value = 0;
		
		/* Add scheduling part */
		if ((f.getDefaultSchedule() != null) && (f.getFlexOfferSchedule() != null) &&
			(f.getFlexOfferSchedule().getScheduleSlices().length == f.getFlexOfferProfileConstraints().length) &&
			(f.getFlexOfferSchedule().getScheduleSlices().length == f.getDefaultSchedule().getScheduleSlices().length) &&
			(!f.getFlexOfferSchedule().equals(f.getDefaultSchedule()))) {
			
			// Check if there're baseline deviations
			boolean deviations = f.getDefaultSchedule().getStartInterval() != f.getFlexOfferSchedule().getStartInterval();
			for(int sid = 0; sid < f.getFlexOfferProfileConstraints().length; sid++) {
				if (Math.abs(f.getDefaultSchedule().getScheduleSlice(sid).getEnergyAmount() -
							 f.getFlexOfferSchedule().getScheduleSlice(sid).getEnergyAmount()) > 1e-3) {
					deviations = true;
					break;
				}
			}		
			
			if (deviations) {			
				/* Incremenent custom schedule activation count */
				value += contract.getSchedulingFixedReward();
				
				value += contract.getSchedulingStartTimeReward() * Math.abs(f.getFlexOfferSchedule().getStartInterval() - 
																		    f.getDefaultSchedule().getStartInterval());
								
				/* ... for energy deviations */
				for(int i=0; i<f.getFlexOfferSchedule().getScheduleSlices().length; i++) {
					value += contract.getSchedulingEnergyReward() * Math.abs(f.getFlexOfferSchedule().getScheduleSlice(i).getEnergyAmount() -
																			 f.getDefaultSchedule().getScheduleSlice(i).getEnergyAmount());
				}				
			}
		}
		return value;
	}
 
	/* Generate bill for the customer, after flexoffer execution */
	public static UserBill generateBill(String userName, UserContract contract, Collection<FlexOffer> flexOffers,
										TimeSeries userMeasurements) {
		UserBill bill = new UserBill();

		bill.setUserName(userName);
		
		// Calculate statistics 
		for(FlexOffer f : flexOffers) {

            /* Increment flexoffers*/
            bill.setNumFlexOffers(bill.getNumFlexOffers() + 1);

            /* Calculate total imbalance */
            if (f.getFlexOfferSchedule() != null) {
                bill.setExecutionImbalance(f.getFlexOfferSchedule().computeImbalance(userMeasurements, 0.0));
            }

			if(!isExecutedCorrectly(f, contract, userMeasurements)) {
				bill.setNumOfExecutedFlexOffers(bill.getNumOfExecutedFlexOffers() + 1);

				// No further reward given
				continue;
			}


				
			/* Add time flexibility part */
			bill.setTotalTimeFlex(bill.getTotalTimeFlex() + (f.getStartBeforeInterval() - f.getStartAfterInterval()));
			
			/* Add energy flexibility part */
			for(FlexOfferSlice s : f.getFlexOfferProfileConstraints()) {
				bill.setTotalEnergyFlex(bill.getTotalEnergyFlex() + (s.getEnergyUpper(0) - s.getEnergyLower(0)));
			}
			
			/* Add scheduling part */
			if ((f.getDefaultSchedule() != null) && (f.getFlexOfferSchedule() != null) &&
				(f.getFlexOfferSchedule().getScheduleSlices().length == f.getFlexOfferProfileConstraints().length) &&
				(f.getFlexOfferSchedule().getScheduleSlices().length == f.getDefaultSchedule().getScheduleSlices().length) &&
				(!f.getFlexOfferSchedule().equals(f.getDefaultSchedule()))) {
				
				// Check if there're baseline deviations
				boolean deviations = f.getDefaultSchedule().getStartInterval() != f.getFlexOfferSchedule().getStartInterval();
				for(int sid = 0; sid < f.getFlexOfferProfileConstraints().length; sid++) {
					if (Math.abs(f.getDefaultSchedule().getScheduleSlice(sid).getEnergyAmount() -
								 f.getFlexOfferSchedule().getScheduleSlice(sid).getEnergyAmount()) > 1e-3) {
						deviations = true;
						break;
					}
				}		
				
				if (deviations) {					
				
					/* Incremenent custom schedule activation count */
					bill.setNumCustomScheduleActivations(bill.getNumCustomScheduleActivations() + 1);
				
					/* Give a reward depending on deviations from default ... */				
					/* ... for start-time deviation */
					bill.setTotalStartTimeDeviations(bill.getTotalStartTimeDeviations() + 
							Math.abs(f.getFlexOfferSchedule().getStartInterval() - f.getDefaultSchedule().getStartInterval()));
									
					/* ... for energy deviations */
					for(int i=0; i<f.getFlexOfferSchedule().getScheduleSlices().length; i++) {
						bill.setTotalEnergyDeviations(bill.getTotalEnergyDeviations() + Math.abs(f.getFlexOfferSchedule().getScheduleSlice(i).getEnergyAmount() -
																	    f.getDefaultSchedule().getScheduleSlice(i).getEnergyAmount()));
					}	
				}	
			}
		}
		
		/* Compute actual reward amounts */		
		bill.setRewardFixed(flexOffers.isEmpty() ? 0 : contract.getFixedReward());
		bill.setRewardTotalTimeFlex(bill.getTotalTimeFlex() * contract.getTimeFlexReward());
		bill.setRewardTotalEnergyFlex(bill.getTotalEnergyFlex() * contract.getEnergyFlexReward());		
		bill.setRewardTotalSchedFixed(bill.getNumCustomScheduleActivations() * contract.getSchedulingFixedReward());
		bill.setRewardTotalSchedEST(bill.getTotalStartTimeDeviations() * contract.getSchedulingStartTimeReward());
		bill.setRewardTotalSchedEnergy(bill.getTotalEnergyDeviations() * contract.getSchedulingEnergyReward());
		bill.setRewardTotal(bill.getRewardFixed() + 
						    bill.getRewardTotalEnergyFlex() + 
						    bill.getRewardTotalSchedEnergy() +
						    bill.getRewardTotalSchedEST() + 
						    bill.getRewardTotalSchedFixed() + 
						    bill.getRewardTotalTimeFlex());
		
		return bill;
	}
}
