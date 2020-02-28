package org.goflex.wp2.fman.flexoffer.events;

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


import org.goflex.wp2.core.entities.FlexOfferSchedule;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class ScheduleUpdatedEvent extends ApplicationEvent {
    private UUID flexOfferId;
    private FlexOfferSchedule schedule;

    public ScheduleUpdatedEvent(Object source, UUID flexOfferId, FlexOfferSchedule schedule) {
        super(source);
        this.flexOfferId = flexOfferId;
        this.schedule = schedule;
    }

    public UUID getFlexOfferId() {
        return flexOfferId;
    }

    public void setFlexOfferId(UUID flexOfferId) {
        this.flexOfferId = flexOfferId;
    }

    public FlexOfferSchedule getSchedule() {
        return schedule;
    }

    public void setSchedule(FlexOfferSchedule schedule) {
        this.schedule = schedule;
    }
}