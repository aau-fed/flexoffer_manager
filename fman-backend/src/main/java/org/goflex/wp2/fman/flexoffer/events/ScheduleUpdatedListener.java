
/*
 * Created by bijay
 * GOFLEX :: WP2 :: foa-core
 * Copyright (c) 2018.
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining  a copy of this software and associated documentation
 *  files (the "Software") to deal in the Software without restriction,
 *  including without limitation the rights to use, copy, modify, merge,
 *  publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions: The above copyright notice and
 *  this permission notice shall be included in all copies or substantial
 *  portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON
 *  INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 *
 *  Last Modified 2/2/18 2:27 PM
 */

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


import org.goflex.wp2.core.adapters.FODateUtils;
import org.goflex.wp2.core.entities.FlexOfferSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ScheduleUpdatedListener implements ApplicationListener<ScheduleUpdatedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleUpdatedListener.class);

    @Autowired
    private Environment env;

    @Resource(name = "scheduleDetail")
    private ConcurrentHashMap<UUID, FlexOfferSchedule> scheduleDetail;

    @Resource(name = "activeSchedules")
    private ConcurrentHashMap<Long, List<UUID>> activeSchedules;

    private ConcurrentHashMap<UUID, FlexOfferSchedule> schedulesCopy = new ConcurrentHashMap<>();

    @Override
    public void onApplicationEvent(ScheduleUpdatedEvent event) {

        // Early backoff, if no ID for FO is assigned
        if (event.getFlexOfferId() == null) return;

        try {
            if (!schedulesCopy.containsKey(event.getFlexOfferId())) {

                logger.debug("New schedule received for FlexOffer with Id: " + event.getFlexOfferId());
                schedulesCopy.put(event.getFlexOfferId(), event.getSchedule());
                scheduleDetail.put(event.getFlexOfferId(), event.getSchedule());

                Long timeStamp = FODateUtils.toFlexOfferTime(event.getSchedule().getStartTime());
                List<UUID> foIds = new ArrayList(Arrays.asList(event.getFlexOfferId()));
                foIds.add(event.getFlexOfferId());
                activeSchedules.put(timeStamp, foIds);

            } else if (!event.getSchedule().equals(schedulesCopy.get(event.getFlexOfferId()))) {
                // todo: if only startAfterTime is different then treat schedule as unchanged
                //  FMAN might update startAfterTime to current time if it becomes invalid
                //  replace the if (true) condition to reflect that
                //  also there seems to be a bug where FMAN keeps alternating startAfterTime
                //  according to laurynas this happens in demand supply balancing mode
                if (true) {
                    logger.debug("Updated schedule received for FlexOffer with Id: " + event.getFlexOfferId());
                    schedulesCopy.put(event.getFlexOfferId(), event.getSchedule());
                    scheduleDetail.put(event.getFlexOfferId(), event.getSchedule());

                    Long oldTimestamp = FODateUtils.toFlexOfferTime(schedulesCopy.get(event.getFlexOfferId()).getStartTime());
                    if (activeSchedules.containsKey(oldTimestamp)
                            && activeSchedules.get(oldTimestamp).contains(event.getFlexOfferId())) {
                        activeSchedules.get(oldTimestamp).remove(event.getFlexOfferId());
                    }

                    Long newTimeStamp = FODateUtils.toFlexOfferTime(event.getSchedule().getStartTime());
                    if (activeSchedules.containsKey(newTimeStamp)) {
                        activeSchedules.get(newTimeStamp).add(event.getFlexOfferId());
                    } else {
                        activeSchedules.put(newTimeStamp, new ArrayList<>(Arrays.asList(event.getFlexOfferId())));
                    }
                }
            } else {
                logger.debug("Same schedule already exists for FlexOffer with Id: " + event.getFlexOfferId());
                logger.trace("Existing schedule!");
                logger.trace(event.getSchedule().toString());
                logger.trace("Received schedule!");
                logger.trace(schedulesCopy.get(event.getFlexOfferId()).toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
