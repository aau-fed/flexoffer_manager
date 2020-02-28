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
import org.goflex.wp2.core.entities.FlexOfferSlice;
import org.goflex.wp2.fman.aggregatorportfolio.AggregatorPortfolio;

public class AmountFlexOptimizer {
	private Optimizer opt;
    private AggregatorPortfolio fp;

    public AmountFlexOptimizer(Optimizer opt) {
    	this.opt = opt;
    	this.fp = opt.getFlexOfferPortfolio();
    }

    private double getEnergyValue(int fid, int sliceId) {
    	return this.fp.getFlexOffers().get(fid).getFlexOfferSchedule().getScheduleSlice(sliceId).getEnergyAmount();
    }

	private double getEnergyRangeLength(int fid, int sliceId) {
		return this.fp.getFlexOffers().get(fid).getFlexOfferProfileConstraint(sliceId).getEnergyUpper(0) -
			   this.fp.getFlexOffers().get(fid).getFlexOfferProfileConstraint(sliceId).getEnergyLower(0);
	}

    private void setEnergyValue(int fid, int sliceId, double value) {
    	this.fp.getFlexOffers().get(fid).getFlexOfferSchedule().getScheduleSlice(sliceId).setEnergyAmount(value);
    }

    private double getTotalEnergyValue(int flexOfferId) {
    	FlexOffer f = this.fp.getFlexOffers().get(flexOfferId);
    	double value = 0;

    	for(int i=0; i<f.getFlexOfferProfileConstraints().length; i++) {
    		value += f.getFlexOfferSchedule().getScheduleSlice(i).getEnergyAmount();
    	}

    	return value;
    }


    private void fixEnergyValue(int fid, int sliceId) {
    	FlexOffer f = this.fp.getFlexOffers().get(fid);
    	FlexOfferSlice fc = f.getFlexOfferProfileConstraint(sliceId);

    	// Fix based on total energy constraints
    	if (handleTotalConstraints && (f.getTotalEnergyConstraint() != null)) {
	    	double totalEnerg = this.getTotalEnergyValue(fid);

	    	if (totalEnerg < f.getTotalEnergyConstraint().getLower()) {
	    		this.setEnergyValue(fid, sliceId, this.getEnergyValue(fid, sliceId) + f.getTotalEnergyConstraint().getLower() - totalEnerg);
	    	}

	    	if (totalEnerg > f.getTotalEnergyConstraint().getUpper()) {
	    		this.setEnergyValue(fid, sliceId, this.getEnergyValue(fid, sliceId) + f.getTotalEnergyConstraint().getUpper() - totalEnerg);
	    	}

    	}

    	// Fix based on min/max energy constraints
    	double value = getEnergyValue(fid, sliceId);

    	if (value > fc.getEnergyUpper(0)) {
    		this.setEnergyValue(fid, sliceId, fc.getEnergyUpper(0));
    	}

    	if (value < fc.getEnergyLower(0)) {
    		this.setEnergyValue(fid, sliceId, fc.getEnergyLower(0));
    	}
    }

	private boolean isValidSolution() {
		for(int fid = 0; fid < this.fp.getFlexOffers().size(); fid++) {
			FlexOffer f = this.fp.getFlexOffers().get(fid);

			if (f.getTotalEnergyConstraint() == null) {
				continue;
			}

			double totalValue = this.getTotalEnergyValue(fid);

			if (totalValue < f.getTotalEnergyConstraint().getLower()) {
				return false;
			}

			if (totalValue > f.getTotalEnergyConstraint().getUpper()) {
				return false;
			}
		}
		return true;
	}

    protected void optimizeAmount(int fid, int sliceId) {
    	double step = this.getEnergyRangeLength(fid, sliceId);
    	double value = this.getEnergyValue(fid, sliceId);
    	double fitness = this.opt.getFitnessValue(); // this.fp.computeTotalPortfolioCost();
        boolean forward = true;

    	// Optimize a specific energy value
    	while (step > 1e-4) {

    		while(true) {
	    		double newVal = value + (forward ? 1.0 : -1.0) * step;
	    		this.setEnergyValue(fid, sliceId, newVal);
	    		this.fixEnergyValue(fid, sliceId); // Try to fix the amount value
	    		double newfitness = this.opt.getFitnessValue(); // this.fp.computeTotalPortfolioCost();

	    		if (newfitness < fitness) {
	    			value = this.getEnergyValue(fid, sliceId);
	    			fitness = newfitness;
	    		} else {
	    			forward = !forward;
	    			break;
	    		}
    		}

    		step /= 2;
    	}

		// Try to fix the amount value
		this.fixEnergyValue(fid, sliceId);
    }


    /* Suspend total constraints */
    private boolean handleTotalConstraints = false;

    // Optimize amounts, return true if it is a valid solution
    public boolean optimizeAmounts() {
    	int numIter = 11;

	    for(int i=0; i < numIter; i++) {

	    	this.handleTotalConstraints = i != 0;

	    	for(int fid = 0; fid < this.fp.getFlexOffers().size(); fid++) {
	    		FlexOffer f = this.fp.getFlexOffers().get(fid);

	    		for(int sliceId = 0; sliceId < f.getFlexOfferProfileConstraints().length; sliceId++) {
	    			this.optimizeAmount(fid, sliceId);
	    		}
	    	}

	    	if (this.isValidSolution()){ break; }
    	}

	    return this.isValidSolution();
    }
}
