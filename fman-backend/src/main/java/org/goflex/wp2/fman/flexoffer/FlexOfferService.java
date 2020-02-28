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
 *  Last Modified 2/22/18 9:57 PM
 */

package org.goflex.wp2.fman.flexoffer;

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
import org.goflex.wp2.core.entities.FlexOfferSchedule;
import org.goflex.wp2.core.entities.FlexOfferSlice;
import org.goflex.wp2.core.entities.FlexOfferState;
import org.goflex.wp2.fman.common.exception.CustomException;
import org.goflex.wp2.fman.flexoffer.events.FlexOfferUpdateAction;
import org.goflex.wp2.fman.flexoffer.events.FlexOfferUpdatedEvent;
import org.goflex.wp2.fman.flexoffer.events.ScheduleUpdatedEvent;
import org.goflex.wp2.fman.user.UserService;
import org.goflex.wp2.fman.user.UserT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;


/**
 * This is a passive component for exchanging Flex-Offer data with the aggregator
 */
@Service
public class FlexOfferService {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private FlexOfferRepository foRepo;

    @Autowired
	private UserService userService;

    @Autowired
    ApplicationEventPublisher publisher;

	public FlexOfferT createFlexOffer(FlexOffer flexOffer){

        logger.info("FlexOffer received from the user {}", flexOffer.getOfferedById());

        // Check if FO with such an ID already exists
        if (flexOffer.getId() != null) {
            // Check the REPO for such a GO
            Optional<FlexOfferT> oldFo = this.foRepo.findById(flexOffer.getId().toString());
            if (oldFo.isPresent()) {
                throw new CustomException("FlexOffer with such an ID already exists. Consider updating.", HttpStatus.UNPROCESSABLE_ENTITY);
            }
        } else {
            // Autogenerate flexOffer UUID, if not set
            flexOffer.setId(UUID.randomUUID());
        }

        // Validate the FO
        this._validateFlexOffer(flexOffer);

        // Get the userId, based on user name
        UserT user = this.userService.getUserByUserName(flexOffer.getOfferedById());

        if (user == null) {
            throw new CustomException("No user can be retrieved with the user name " + flexOffer.getOfferedById(), HttpStatus.NOT_FOUND);
        }

        // Do flexOffer validity check, before saving
        if (!flexOffer.isCorrect()) {
            throw new CustomException("FlexOffer attributes are not set correctly.", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // Check the status
        if (flexOffer.getState() != FlexOfferState.Initial) {
            throw new CustomException("FlexOffer cannot be created in the state other than \"initial\".", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // OK, we accept such a FO
        flexOffer.setState(FlexOfferState.Accepted);

        /* Generate FlexOffeT*/
        FlexOfferT t = new FlexOfferT();
        t.setId(flexOffer.getId().toString());
        t.setCreationTime(new Date());
        t.setStatus(flexOffer.getState());
        t.setUser(user);
        t.setFlexoffer(flexOffer);

        t = foRepo.saveAndFlush(t);

		/* Generate an event */
        publisher.publishEvent(new FlexOfferUpdatedEvent(this, t.getFlexoffer(), FlexOfferUpdateAction.foAdded ));

		return t;
	}

    public Optional<FlexOfferT> getFlexOffer(UUID flexOfferId) {
	    return this.foRepo.findById(flexOfferId.toString());
    }

    public void updateFlexOfferUser(Optional<FlexOfferT> oldOptionalFoT, String offeredById) {

	    FlexOfferT oldFoT = oldOptionalFoT.get();

        // Get the userId, based on user name
        String userName = offeredById.split("@")[0];
        UserT user = this.userService.getUserByUserName(userName);

        oldFoT.getFlexoffer().setOfferedById(offeredById);
        oldFoT.setUser(user);
        this.foRepo.save(oldFoT);
    }

    public FlexOfferT updateFlexOffer(FlexOffer oldFo, FlexOffer newFo) {

        if (oldFo.getId() != newFo.getId() ||
                oldFo.getOfferedById() != newFo.getOfferedById())  {
            throw new CustomException("FlexOffer ID and OfferedById cannot be changed.", HttpStatus.FORBIDDEN);
        }

        /* Check if updates are possible */
        if (oldFo.getState() == FlexOfferState.Executed ) {
            throw new CustomException("Cannot update FlexOffer in the state \"Executed\"", HttpStatus.FORBIDDEN);
        }

        // Validate the FO
        this._validateFlexOffer(newFo);

        // Reset the FO state to accepted
        newFo.setState(FlexOfferState.Accepted);

        // Get the userId, based on user name
        UserT user = this.userService.getUserByUserName(newFo.getOfferedById());

        FlexOfferT t = new FlexOfferT();
        t.setId(newFo.getId().toString());
        t.setCreationTime(new Date());
        t.setStatus(newFo.getState());
        t.setUser(user);
        t.setFlexoffer(newFo);

        t = foRepo.saveAndFlush(t);

        /* Generate an event */
        publisher.publishEvent(new FlexOfferUpdatedEvent(this, t.getFlexoffer(), FlexOfferUpdateAction.foUpdated ));

        return t;
    }

    public void deleteFlexOffer(FlexOffer fo) {
        /* Check if updates are possible */
        if (fo.getState() == FlexOfferState.Executed ) {
            throw new CustomException("Cannot delete FlexOffers in the state \"Executed\"", HttpStatus.FORBIDDEN);
        }

        this.foRepo.deleteById(fo.getId().toString());

        /* Generate an event */
        publisher.publishEvent(new FlexOfferUpdatedEvent(this, fo, FlexOfferUpdateAction.foDeleted ));
    }

    public List<FlexOfferT> getAllFlexOffers(){
	    return foRepo.findAll();
    }

    /* This method gets all active / operational flex-offer */
    public List<FlexOfferT> getAllActiveFlexOffers(){    return foRepo.findAllActive();   }

    public Collection<FlexOfferT> getAllByIds(Iterable<String> ids) {
	    return foRepo.findAllById(ids);
    }

    private void _validateFlexOffer(FlexOffer flexoffer) {
        if (!flexoffer.isCorrect()) {
            throw new CustomException("FlexOffer is not valid! Please check its attributes. ", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    //TODO Check if this is needed, it extract all flexoffer from a particular user for a given month and year
    public List<FlexOffer> getFOsByUsernameAndMonth(String userName, Date startDate, Date endDate){
        return foRepo.findByUserIDAndMonth(userService.getUserByUserName(userName).getUserId(), startDate, endDate);
    }

    /* Retrieve all FOs within a selected time period */
    public List<FlexOfferT> getFOsCreatedBetweenDates(Date startDate, Date endDate){
        return foRepo.findAllCreatedWithin(startDate, endDate);
    }

    public List<FlexOfferT> getUserFOsCreatedBetweenDates(long userId, Date startDate, Date endDate){
        return foRepo.findByUserCreatedWithin(userId, startDate, endDate);
    }

    /**
     *  Reduce inactive flexibility of the (parts of) flex-offer profiles that were supposed to be executed, i.e., are positioned earlier than the current time
     *  */
    public FlexOffer reduceInactiveFlexibility(FlexOffer f) {
        long currentTid = FlexOffer.toFlexOfferTime(new Date());

            FlexOfferSchedule sch = f.getFlexOfferSchedule();

            /* Two case, when : 1) schedule is availabe; 2) schedule is not available */
            if (sch != null) {

                /* Start-time bound has been crossed */
                if (currentTid > sch.getStartInterval()) {

                    /* Stat-time treatment - lock-in the start-time */
                    f.setStartAfterInterval(sch.getStartInterval());
                    f.setStartBeforeInterval(sch.getStartInterval());

                    /* Energy amount treatment - lock-in the energy amounts */
                    long sliceTid = sch.getStartInterval();
                    for (int i=0; i < f.getFlexOfferProfileConstraints().length; i++) {
                        FlexOfferSlice s = f.getFlexOfferProfileConstraint(i);

                        if (currentTid > sliceTid) {
                            s.getEnergyConstraint(0).setLower(sch.getScheduleSlice(i).getEnergyAmount());
                            s.getEnergyConstraint(0).setUpper(sch.getScheduleSlice(i).getEnergyAmount());
                        }

                        assert(s.getMinDuration() == s.getMaxDuration());

                        sliceTid += s.getMaxDuration();
                    }
                }
            } else {


                /* Start time treatment */
                if (currentTid > f.getStartAfterInterval()) {

                    /* Fix the EST */
                    f.setStartAfterInterval(Math.min(currentTid, f.getStartBeforeInterval()));
                }
            }

            return f;
    }

    // Handle schedule update event
    @EventListener
    @Transactional
    public void handleAggregatedScheduleUpdated(ScheduleUpdatedEvent evt) {
	    // Find a FO with this ID
        FlexOfferT fo = this.foRepo.getOne(evt.getFlexOfferId().toString());

        if (fo == null || fo.getFlexoffer() == null) {
            this.logger.warn("Schedule arrived for a non-existend flexoffer.");
            return;
        };

        // Update the schedule
        if (evt.getSchedule() == null) {
            fo.getFlexoffer().setFlexOfferSchedule(null);
        } else {
            if (!evt.getSchedule().isCorrect(fo.getFlexoffer())) {
                logger.warn("FlexOffer {} received an incorrect schedule.", fo.getId());
            }

            fo.getFlexoffer().setFlexOfferSchedule(evt.getSchedule());
        }

        this.foRepo.save(fo);
    }


    // Periodic FlexOffer state treatment
    @Scheduled(fixedDelayString = "${flexoffer.update.delay}")
    public void updateFlexOfferState() {
        Date now = new Date();
        long currentInterval = FlexOffer.toFlexOfferTime(now);

        List<FlexOfferT> fos = this.foRepo.findAllActive();

        fos.forEach(f -> {
            boolean isStillActive = true;

            if (f.getFlexoffer() == null) {
                isStillActive = false;
            } else {

                // Check the latest end time
                if (currentInterval > f.getFlexoffer().getEndBeforeInterval()) {     isStillActive = false;    }

                // Check the end of execution
                if (f.getFlexoffer().getFlexOfferSchedule() != null &&
                        currentInterval > f.getFlexoffer().getFlexOfferSchedule().getEndInterval()) {
                    isStillActive = false;
                }
            }

            if (!isStillActive) {
                Optional<FlexOfferT> fto = this.foRepo.findById(f.getId());

                if (!fto.isPresent()) {
                    logger.error("FlexOffer with id {} is not found", f.getId());
                } else {
                    FlexOfferT ft = fto.get();

                    ft.setStatus(FlexOfferState.Executed);

                    if (ft.getFlexoffer() != null) {
                        ft.getFlexoffer().setState(FlexOfferState.Executed);
                    }

                    this.foRepo.save(ft);

                    /* Generate an event */
                    publisher.publishEvent(new FlexOfferUpdatedEvent(this, ft.getFlexoffer(), FlexOfferUpdateAction.foUpdated ));

                }
            }

        });

    }



}