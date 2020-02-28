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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class TempDataStorage {

    @Bean(name = "scheduleDetail")
    public ConcurrentHashMap<UUID, FlexOfferSchedule> scheduleDetailTable() {
        ConcurrentHashMap<UUID, FlexOfferSchedule> scheduleDetail = new ConcurrentHashMap<>();
        return scheduleDetail;
    }

    @Bean(name = "activeSchedules")
    public ConcurrentHashMap<Long, List<UUID>> activeSchedules() {
        ConcurrentHashMap<Long, List<UUID>> activeSchedules = new ConcurrentHashMap<>();
        return activeSchedules;
    }
}
