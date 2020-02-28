package org.goflex.wp2.core.adapters;

/*-
 * #%L
 * GOFLEX::WP2::Core Data Structures
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



import java.util.Date;

public class FODateUtils {

	/* Specified the relation between the intervals and minutes */
	public static final int numSecondsPerInterval = 15*60;	/* 1 flex-offer interval corresponds to 15*60 seconds/15 minutes */


	/* Convert discrete FlexOffer time to absolute time */
	public static final Date toAbsoluteTime(long foTime)
	{
		long timeInMillisSinceEpoch = foTime * (numSecondsPerInterval * 1000);
		return new Date(timeInMillisSinceEpoch);
	}

	/* Convert abosolute time to discrete FlexOffer time */
	public static final long toFlexOfferTime(Date time)
	{
		long timeInMillisSinceEpoch = time.getTime();
		return timeInMillisSinceEpoch / (numSecondsPerInterval * 1000);
	}


}