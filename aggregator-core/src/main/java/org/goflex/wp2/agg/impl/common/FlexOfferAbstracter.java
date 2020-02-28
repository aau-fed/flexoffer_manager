package org.goflex.wp2.agg.impl.common;

/*-
 * #%L
 * ARROWHEAD::WP5::Aggregator Core
 * %%
 * Copyright (C) 2016 The ARROWHEAD Consortium
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

/**
 * A class that generalizes a single flex-offer for grouping and weight bounding  
 * 
 * @author Laurynas Siksnys (siksnys@cs.aau.dk), AAU
 *
 */
public class FlexOfferAbstracter {
	public FlexOfferWeightDelegate weightDlg = null;
	public FlexOfferDimValueDelegate [] dimValDlg= null;
	
	public FlexOfferAbstracter(FlexOfferWeightDelegate weightDlg, FlexOfferDimValueDelegate ... dimValDlg)
	{
		this.weightDlg = weightDlg;
		this.dimValDlg = dimValDlg;
	}

	public int getDimCount()
	{
		return dimValDlg.length;
	}
	
	public FGridVector getFlexOfferVector(FlexOffer f) {
		FGridVector v =  new FGridVector(this.getDimCount());
		for(int i=0; i< v.getDimCount(); i++)
			v.getValues()[i] = this.dimValDlg[i].getValue(f);
		return v;
	}
}
