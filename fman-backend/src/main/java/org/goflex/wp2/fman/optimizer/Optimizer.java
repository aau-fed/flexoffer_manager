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
import org.goflex.wp2.core.entities.FlexOfferSchedule;
import org.goflex.wp2.core.entities.TimeSeries;
import org.goflex.wp2.fman.aggregatorportfolio.AggregatorPortfolio;

import java.util.HashMap;

public class Optimizer {
	private AggregatorPortfolio fp;
    private OptimizationObjective obj;

    private TimeSeries commitmentSeries = null;

	public Optimizer(AggregatorPortfolio fp, OptimizationObjective obj) {
		this.fp = fp;
		this.obj = obj;
		this.commitmentSeries = null;
	}

	public AggregatorPortfolio getFlexOfferPortfolio() {
		return this.fp;
	}

	/* Optimizes a portfolio. Returns true, if a better solution was found. Else - false */
	public boolean optimizePortfolio(boolean restoreSchedulesIfNotImproved) throws Exception {
		// Initialization
		switch (this.obj) {
			case objFollowFMAR:
				this.commitmentSeries = this.fp.getCommitmentEnergy(Double.MAX_VALUE);
			default:
		}

		// Make a snapshot of current schedule and cost
		double previousCost = this.getFitnessValue();
		double acceptEpsilon = 1e-3;

		HashMap<FlexOffer, FlexOfferSchedule> previousSchedules = new HashMap<>();

		this.fp.getFoCollection().getFos().stream()
				.forEach(f -> {
					previousSchedules.put(f, f.getFlexOfferSchedule().clone());
				});

		if (obj == OptimizationObjective.objLowestCost) {
			this.fp.getFoCollection().setSchedulesToBaseline();

			/* No market commitments, no optimization is needed*/
			if (this.fp.getMarketCommitments() == null || this.fp.getMarketCommitments().isEmpty()) {
				return this.getFitnessValue() < previousCost - acceptEpsilon;
			}
		}

		switch (this.obj) {
			case objFollowFMAR:
			case objEnergyBalance:
			case objLowestCost:
			case objEnergyMax:
			case objEnergyMin:

				// STEP 1: Solve time scheduling problem (black-box) first
				// Initialize the time flexibility scheduling
				TimeFlexOptimizer tsOpt = new TimeFlexOptimizer(this);
				tsOpt.optimizeTimeFlex();

				/* STEP 2: Solve the swarm ops problem */
				AmountFlexOptimizer apOpt = new AmountFlexOptimizer(this);
				apOpt.optimizeAmounts();

				// TODO: Use LP based optimizer in the future
                //AmountFlexLPOptimizer apLpOpt = new AmountFlexLPOptimizer(this.getFlexOfferPortfolio());

				break;
			default: {}
		}

		// Revert back, if the current cost did not improve
		double newCost = this.getFitnessValue();
		if (restoreSchedulesIfNotImproved && previousCost <= newCost) {
			this.fp.getFoCollection().getFos().stream()
					.forEach(f -> f.setFlexOfferSchedule(previousSchedules.get(f)));
		}

		return newCost < previousCost - acceptEpsilon;
	}

	/* Get a fitness value of the portfolio for different objective targets */
	protected double getFitnessValue() {
		switch(this.obj) {
			case objLowestCost:
				return this.fp.computeTotalPortfolioCost();
			case objEnergyBalance:
				return this.fp.getFoCollection().computeEnergySquaredImbalance();
			case objEnergyMin:
				return this.fp.getFoCollection().computeTotalEnergy(this.obj.getTimeIntervalFrom(),
												  this.obj.getTimeIntervalTo());
			case objEnergyMax:
				return -this.fp.getFoCollection().computeTotalEnergy(this.obj.getTimeIntervalFrom(),
												   this.obj.getTimeIntervalTo());
			case objFollowFMAR:
				return this.fp.getFoCollection().computeEnergySquaredImbalanceAgainstTimeSeries(this.commitmentSeries, Double.MAX_VALUE);
			default:
				return 0;
		}
	}

    public AggregatorPortfolio getFp() {
        return fp;
    }

    public void setFp(AggregatorPortfolio fp) {
        this.fp = fp;
    }

    public OptimizationObjective getObj() {
        return obj;
    }

    public void setObj(OptimizationObjective obj) {
        this.obj = obj;
    }

}
