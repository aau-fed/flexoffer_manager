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

import java.util.Date;

public enum OptimizationObjective {
	/**
	 * Aggregator aims for least cost 
	 */
 objLowestCost,
 
 /**
  * Aggregator aims for energy balance
  */
 objEnergyBalance,

 /**
  * Aggregator aims for low energy
  *  */
 objEnergyMin,
 
 /**
  * Aggregator aims for high energy
  *  */
 objEnergyMax,

 /**
  * Follow FMAR
  *  */
 objFollowFMAR,
 
 /**
  * Manual scheduling
  */
 objManual;


 /* This defines the time window in which the objective is effective */
 private long timeIntervalFrom = 0;
 private long timeIntervalTo = 0;


 public long getTimeIntervalFrom() {
  return timeIntervalFrom;
 }

 public void setTimeIntervalFrom(long timeIntervalFrom) {
  this.timeIntervalFrom = timeIntervalFrom;
 }

 public long getTimeIntervalTo() {
  return timeIntervalTo;
 }

 public void setTimeIntervalTo(long timeIntervalTo) {
  this.timeIntervalTo = timeIntervalTo;
 }
}
