package org.goflex.wp2.fman.measurements.entities;

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


import org.goflex.wp2.fman.measurements.MeasurementT;

import java.util.Date;

public class MeasurementPayload {
    private String userName;
    private Long measurementId;
    private Date timeStamp;
    private Double cumulativeEnergy;

    public MeasurementPayload(){}

    public MeasurementPayload(String userName, Long measurementId, Date timeStamp, Double cumulativeEnergy) {
        this.userName = userName;
        this.measurementId = measurementId;
        this.timeStamp = timeStamp;
        this.cumulativeEnergy = cumulativeEnergy;
    }

    static public MeasurementPayload mapper(MeasurementT m) {
        return new MeasurementPayload(m.getUserName(), m.getId(), m.getTimeStamp(), m.getCumulativeEnergy());
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getMeasurementId() {
        return measurementId;
    }

    public void setMeasurementId(Long measurementId) {
        this.measurementId = measurementId;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Double getCumulativeEnergy() {
        return cumulativeEnergy;
    }

    public void setCumulativeEnergy(Double cumulativeEnergy) {
        this.cumulativeEnergy = cumulativeEnergy;
    }
}
