package org.goflex.wp2.fman.measurements.events;

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
import org.goflex.wp2.fman.flexoffer.events.FlexOfferUpdateAction;
import org.goflex.wp2.fman.measurements.MeasurementT;
import org.goflex.wp2.fman.measurements.entities.MeasurementPayload;
import org.springframework.context.ApplicationEvent;

import java.util.Collection;

public class MeasurementsUpdatedEvent extends ApplicationEvent {
    private String userName;
    private Collection<MeasurementT> payload;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public MeasurementsUpdatedEvent(Object source, String userName, Collection<MeasurementT> payload) {
        super(source);

        this.userName = userName;
        this.payload = payload;
    }

    public String getUserName() { return userName;  }

    public void setUserName(String userName) {  this.userName = userName;  }

    public Collection<MeasurementT> getPayload() {
        return payload;
    }

    public void setPayload(Collection<MeasurementT> payload) {
        this.payload = payload;
    }
}