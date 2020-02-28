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



import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;

/** Aggregator's bill generated towards FMAR **/
@XmlRootElement
public class MarketBill {
	/* Aggregator's bill records */

	/* Number flexoffers sent to the market */
	private int numOffersIssued = 0;

	/* Number flexoffers sent to the market */
	private int numOffersAcceptedByFMAR = 0;

	/*
	    Total income for all accepted offers
	 */
	private double totalIncome = 0;

    /*
        Total looss for all accepted offers
    */
	private double totalImbalanceLoss = 0;


    public int getNumOffersIssued() {
        return numOffersIssued;
    }

    public void setNumOffersIssued(int numOffersIssued) {
        this.numOffersIssued = numOffersIssued;
    }

    public int getNumOffersAcceptedByFMAR() {
        return numOffersAcceptedByFMAR;
    }

    public void setNumOffersAcceptedByFMAR(int numOffersAcceptedByFMAR) {
        this.numOffersAcceptedByFMAR = numOffersAcceptedByFMAR;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(double totalIncome) {
        this.totalIncome = totalIncome;
    }

    public double getTotalImbalanceLoss() {
        return totalImbalanceLoss;
    }

    public void setTotalImbalanceLoss(double totalImbalanceLoss) {
        this.totalImbalanceLoss = totalImbalanceLoss;
    }
}
