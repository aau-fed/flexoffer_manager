package org.goflex.wp2.fman.inea_market.entities;

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


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.goflex.wp2.core.entities.FlexOfferTariffConstraint;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

/**
 * A single slice (interval) in the flexoffer profile. 
 * @author Laurynas
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FlexOfferScheduleInea implements Serializable {
	private static final long serialVersionUID = -1249370306408064574L;

	private static final String  dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	@XmlElement
	private Date startTime;

	private FlexOfferScheduleSliceInea [] scheduleSlices;

	public FlexOfferScheduleInea(Date startTime, FlexOfferScheduleSliceInea[] scheduleSlices) {
		this.startTime = startTime;
		this.scheduleSlices = scheduleSlices;
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateFormat)
	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public FlexOfferScheduleSliceInea[] getScheduleSlices() {
		return scheduleSlices;
	}

	public void setScheduleSlices(FlexOfferScheduleSliceInea[] scheduleSlices) {
		this.scheduleSlices = scheduleSlices;
	}

	@Override
	public String toString() {
		return "FlexOfferScheduleInea{" +
				"startTime=" + startTime +
				", scheduleSlices=" + Arrays.toString(scheduleSlices) +
				'}';
	}
}
