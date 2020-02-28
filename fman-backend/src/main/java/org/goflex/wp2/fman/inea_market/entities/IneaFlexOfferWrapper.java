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


/*
 * Created by Ivan Bizaca on 13/07/2017.
 */

/* FLEX OFFER MESSAGE FROM FOA TO FMAN */


import com.fasterxml.jackson.annotation.JsonFormat;
import org.goflex.wp2.core.entities.FlexOfferState;
import org.goflex.wp2.core.entities.Assignment;
import org.goflex.wp2.core.entities.FlexOfferType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;
import java.util.Date;


@XmlAccessorType(XmlAccessType.FIELD)
public class IneaFlexOfferWrapper implements Serializable, Cloneable{

    private long id;
    private FlexOfferState state;
    private int offeredById;
    //private Date acceptanceBeforeTime;
    private int numSecondsPerInterval=900;
    private Date startAfterTime;
    private Date startBeforeTime;
    private Date assignmentBeforeTime;
    private Date creationTime;
    private FlexOfferSliceInea[] flexOfferProfileConstraints;
    private FlexOfferScheduleInea defaultSchedule;
    // private TariffConstraintProfileInea flexOfferTariffConstraint = null; 2018-09-21 Gregor: This is not needed
    private Assignment assignment = Assignment.obligatory;

    public  static final String  dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public FlexOfferState getState() {
        return state;
    }

    public void setState(FlexOfferState state) {
        this.state = state;
    }

    public int getOfferedById() {
        return offeredById;
    }

    public void setOfferedById(int offeredById) {
        this.offeredById = offeredById;
    }


    @XmlAttribute
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateFormat)
    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

        public int getNumSecondsPerInterval() {
        return numSecondsPerInterval;
    }

    public void setNumSecondsPerInterval(int numSecondsPerInterval) {
        this.numSecondsPerInterval = numSecondsPerInterval;
    }

    @XmlAttribute
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateFormat)
    public Date getStartAfterTime() {
        return startAfterTime;
    }

    public void setStartAfterTime(Date startAfterTime) {
        this.startAfterTime = startAfterTime;
    }

    @XmlAttribute
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateFormat)
    public Date getStartBeforeTime() {
        return startBeforeTime;
    }

    public void setStartBeforeTime(Date startBeforeTime) {
        this.startBeforeTime = startBeforeTime;
    }

    public FlexOfferSliceInea[] getFlexOfferProfileConstraints() {
        return flexOfferProfileConstraints;
    }

    public void setFlexOfferProfileConstraints(FlexOfferSliceInea[] flexOfferProfileConstraints) {
        this.flexOfferProfileConstraints = flexOfferProfileConstraints;
    }

//
//    public TariffConstraintProfileInea getFlexOfferTariffConstraint() {
//        return flexOfferTariffConstraint;
//    }
//
//    public void setFlexOfferTariffConstraint(TariffConstraintProfileInea flexOfferTariffConstraint) {
//        this.flexOfferTariffConstraint = flexOfferTariffConstraint;
//    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    @XmlAttribute
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateFormat)
    public Date getAssignmentBeforeTime() {
        return assignmentBeforeTime;
    }

    public void setAssignmentBeforeTime(Date assignmentBeforeTime) {
        this.assignmentBeforeTime = assignmentBeforeTime;
    }


    public FlexOfferScheduleInea getDefaultSchedule() {
        return defaultSchedule;
    }

    public void setDefaultSchedule(FlexOfferScheduleInea defaultSchedule) {
        this.defaultSchedule = defaultSchedule;
    }

}
