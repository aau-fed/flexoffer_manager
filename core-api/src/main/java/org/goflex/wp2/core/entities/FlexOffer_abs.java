package org.goflex.wp2.core.entities;

/*-
 * #%L
 * ARROWHEAD::WP5::Core Data Structures
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


import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class describes a flex-offer in the discrete time.      
 */
/**
 * @author Laurynas
 *
 */
@XmlRootElement(name="flexOffer")
@XmlAccessorType(XmlAccessType.FIELD)
public class FlexOffer_abs implements Serializable, Cloneable {
    private static final Logger logger = LoggerFactory.getLogger(FlexOffer_abs.class);

	/* Specified the relation between the intervals and minutes */
	public static final int numSecondsPerInterval = 15*60;	/* 1 flex-offer interval corresponds to 15*60 seconds/15 minutes */

	public  static final String  dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

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


	
	private static final long serialVersionUID = -6141402215673014415L;
		
	/**
	 *  ID of flex-offer. 
	 *  
	 *  A clarification of this ID is:
	 *  The flex-offer owner/sender sets this ID to a value it prefers. When a flex-offer is sent to 
	 *  a receiver, the receiver creates a local flex-offer copy, returning an ID of its local copy.
	 *  
	 *  If the sender wants to update/delete the flex-offer on the receiver, it must use the receiver's local ID.
	 *  It the receiver wants to update/delete the flex-offer on the sender, it must use the sender's local ID.  
	 * 
	 *  @author Laurynas
	 */
	@XmlAttribute
	private UUID id = null;


	@XmlAttribute
	private FlexOfferState state = FlexOfferState.Initial;
	
	@XmlAttribute
	private String stateReason=null;
	
	//@XmlTransient
	private long creationInterval;


	@XmlAttribute
	private String offeredById = ""; // Is not associated with any legal entity


	@XmlAttribute
	private String locationId = "";

	// negotiation time constrains
	@XmlAttribute
	private long acceptBeforeInterval;

	// start time constrains
	//@XmlTransient
	private long assignmentBeforeInterval; // Not set (no constraint)
	
	// Assign this interval number before the scheduled start time of the flex-offer 
	//@XmlTransient
	@JsonIgnore
	private long assignmentBeforeDurationIntervals = 0; // Not set (no constraint)

	@XmlTransient
	private long startAfterInterval;

	@XmlTransient
	private long startBeforeInterval;

	@XmlAttribute
	private FlexOfferType flexOfferType = FlexOfferType.imbalance;

	@XmlAttribute
	private Assignment assignment = Assignment.obligatory;
	
	/**
	 * Derived sum of duration from all consecutive energy intervals. It is
	 * based on discrete time intervals.
	 */
	private transient int duration = Integer.MIN_VALUE;
	
	// energy intervals
	@XmlElement
	private FlexOfferSlice[] flexOfferProfileConstraints = new FlexOfferSlice[0];


	/* 2017-06-29 Added the support the flex-offer enhancement V1 */
	@XmlElement(nillable = true)
	private TariffConstraintProfile flexOfferTariffConstraint = null;

	/* 2015-08-05 Added the support for the default schedule, aka "baseline" */
	@XmlElement(nillable = true)
	private FlexOfferSchedule defaultSchedule = null;

	@XmlElement(nillable = true)
	private FlexOfferSchedule flexOfferSchedule = null;

	// if null then no total constraint
	@JsonIgnore
	private FlexOfferConstraint totalEnergyConstraint = null;

	@JsonIgnore
	private FlexOfferConstraint totalCostConstraint = null ;


	/**
	 * Initializes an empty discrete flex-offer. 
	 */
	public FlexOffer_abs() {}

	/**
	 * Compare two flex-offers
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FlexOffer_abs other = (FlexOffer_abs) obj;
		if (acceptBeforeInterval != other.acceptBeforeInterval)
			return false;
		if (assignmentBeforeDurationIntervals != other.assignmentBeforeDurationIntervals)
			return false;
		if (assignmentBeforeInterval != other.assignmentBeforeInterval)
			return false;
		if (creationInterval != other.creationInterval)
			return false;
		if (flexOfferSchedule == null) {
			if (other.flexOfferSchedule != null)
				return false;
		} else if (!flexOfferSchedule.equals(other.flexOfferSchedule))
			return false;
		if (defaultSchedule == null) {
			if (other.defaultSchedule != null)
				return false;
		} else if (!defaultSchedule.equals(other.defaultSchedule))
			return false;
		if (totalCostConstraint == null) {
			if (other.totalCostConstraint != null)
				return false;
		} else if (!totalCostConstraint.equals(other.totalCostConstraint))
			return false;
		if (flexOfferTariffConstraint == null) {
			if (other.flexOfferTariffConstraint != null)
				return false;
		} else if (!flexOfferTariffConstraint.equals(other.flexOfferTariffConstraint))
			return false;
		if (id != other.id)
			return false;
		if (offeredById == null) {
			if (other.offeredById != null)
				return false;
		} else if (!offeredById.equals(other.offeredById))
			return false;
		if (!Arrays.equals(flexOfferProfileConstraints, other.flexOfferProfileConstraints))
			return false;
		if (startAfterInterval != other.startAfterInterval)
			return false;
		if (startBeforeInterval != other.startBeforeInterval)
			return false;
		if (state != other.state)
			return false;
		if (stateReason == null) {
			if (other.stateReason != null)
				return false;
		} else if (!stateReason.equals(other.stateReason))
			return false;
		if (totalEnergyConstraint == null) {
			if (other.totalEnergyConstraint != null)
				return false;
		} else if (!totalEnergyConstraint.equals(other.totalEnergyConstraint))
			return false;
		return true;
	}

	/**
	 * clone the existing flex-offer
	 */
	@Override
	public Object clone() {
        try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}



	/**
	 * the absolute time and interval the flex-offer has to be accepted
	 */

	@XmlAttribute
	public long getAcceptanceBeforeInterval() {
		return acceptBeforeInterval;
	}

	/**
	 * all set interval has been disabled and interval can only be set using set time method
	 */
	 /*public void setAcceptanceBeforeInterval(long acceptanceBeforeInterval) {
		this.acceptBeforeInterval = acceptanceBeforeInterval;
	}
*/
	@XmlAttribute
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateFormat)
	public Date getAcceptBeforeTime() {
		return FlexOffer_abs.toAbsoluteTime(this.acceptBeforeInterval);
	}

	public void setAcceptBeforeTime(Date time) {
		this.acceptBeforeInterval = FlexOffer_abs.toFlexOfferTime(time);
	}

	/**
	 * interval number before the scheduled start time of the flex-offer
	 */
	/**
	 * @return 	assign before duration 
	 */
	public long getAssignmentBeforeDurationIntervals() {
		return assignmentBeforeDurationIntervals;
	}
		
	public void setAssignmentBeforeDurationIntervals(long assignmentBeforeDurationIntervals) {
		this.assignmentBeforeDurationIntervals = assignmentBeforeDurationIntervals;
	}

	@XmlAttribute
	@JsonIgnore
	public long getAssignmentBeforeDurationSeconds() {
		return FlexOffer_abs.numSecondsPerInterval * this.assignmentBeforeDurationIntervals;
	}

	public void setAssignmentBeforeDurationSeconds(int seconds) {
		this.assignmentBeforeDurationIntervals = seconds / FlexOffer_abs.numSecondsPerInterval;
	}

	/**
	 * the absolute time and interval the flex-offer has to be scheduled
	 */
	/**

	/**
	 * @return 	assign before intervals 
	 */
	public long getAssignmentBeforeInterval() {
		return assignmentBeforeInterval;
	}


	public void setAssignmentBeforeInterval(long assignmentBeforeInterval) {
		this.assignmentBeforeInterval = assignmentBeforeInterval;
	}

	@XmlAttribute
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateFormat)
	public Date getAssignmentBeforeTime() {
		return FlexOffer_abs.toAbsoluteTime(this.assignmentBeforeInterval);
	}

	public void setAssignmentBeforeTime(Date time) {
		this.assignmentBeforeInterval = FlexOffer_abs.toFlexOfferTime(time);
	}


	/**
	 * the absolute time and interval the flex-offer is created
	 */

	public long getCreationInterval() {
		return creationInterval;
	}

	/**
	 * all set interval has been disabled and interval can only be set using set time method
	 */
	//public void setCreationInterval(long creationInterval) {
		//this.creationInterval = creationInterval;
	//}

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateFormat)
	public Date getCreationTime() {
		return FlexOffer_abs.toAbsoluteTime(this.creationInterval);
	}

	public void setCreationTime(Date time) {
		this.creationInterval = FlexOffer_abs.toFlexOfferTime(time);
	}

	/**
	 * Calculated Field
	 * the absolute time and interval after which the flex-offer operation should end
	 */
	@JsonIgnore
	public long getEndAfterInterval() {
		return getStartBeforeInterval() + getMaxDurationIntervals();
	}

	@JsonIgnore
	public Date getEndAfterTime() {
		return FlexOffer_abs.toAbsoluteTime(getEndAfterInterval());
	}

	/**
	 * Calculated Field
	 * the absolute time and interval before which the flex-offer operation should end
	 */
	@JsonIgnore
	public long getEndBeforeInterval() {
		return getStartBeforeInterval() + getMaxDurationIntervals();
	}

	@JsonIgnore
	public Date getEndBeforeTime() {
		return FlexOffer_abs.toAbsoluteTime(getEndBeforeInterval());
	}


	/**
	 * @return duration in number of intervals
	 * Laurynas: I still think that getDuration is a better name. Note, that a duration can be higher than an interval count.
	 */
	@JsonIgnore
	public int getMinDurationIntervals() {
		if (duration == Integer.MIN_VALUE) {
			duration = 0;
			for (FlexOfferSlice slice : flexOfferProfileConstraints) {
				duration += slice.getMinDuration();
			}
		}
		return duration;
	}
	/**
	 * @return duration in number of intervals
	 * Laurynas: I still think that getDuration is a better name. Note, that a duration can be higher than an interval count.
	 */
	@JsonIgnore
	public int getMaxDurationIntervals() {
		if (duration == Integer.MIN_VALUE) {
			duration = 0;
			for (FlexOfferSlice slice : flexOfferProfileConstraints) {
				duration += slice.getMaxDuration();
			}
		}
		return duration;
	}


	/**
	 * Get time interval duration in seconds. This is a system-wide constant
	 */
	@XmlAttribute
	public int getNumSecondsPerInterval(){
		return FlexOffer_abs.numSecondsPerInterval;
	}

	/**
	 * the absolute time and interval after which the flex-offer should be started
	 */
	public long getStartAfterInterval() {
		return this.startAfterInterval;
	}

	@XmlAttribute
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateFormat)
	public Date getStartAfterTime() {
		return FlexOffer_abs.toAbsoluteTime(this.startAfterInterval);
	}

	public void setStartAfterTime(Date time) {
		this.startAfterInterval = FlexOffer_abs.toFlexOfferTime(time);
	}

	/**
	 * the absolute time and interval before which the flex-offer should be started
	 */
	public long getStartBeforeInterval() {
		return this.startBeforeInterval;
	}

	@XmlAttribute
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateFormat)
	public Date getStartBeforeTime() {
		return FlexOffer_abs.toAbsoluteTime(this.startBeforeInterval);
	}

	public void setStartBeforeTime(Date time) {
		this.startBeforeInterval = FlexOffer_abs.toFlexOfferTime(time);
	}

	@JsonIgnore
	public int getMinDurationSeconds() {
		return FlexOffer_abs.numSecondsPerInterval * getMinDurationIntervals();
	}

	@JsonIgnore
	public int getMaxDurationSeconds() {
		return FlexOffer_abs.numSecondsPerInterval * getMaxDurationIntervals();
	}


	
	public FlexOfferSchedule getFlexOfferSchedule() {
		return flexOfferSchedule;
	}




	/**
	 *  Results the global ID of the legal entity role that issued this flex-offer 
	 * @return the offeredById
	 */
	public String getOfferedById() {
		return offeredById;
	}
			
	/**
	 * @param i the index of the energy interval
	 * @return the i-th energy interval
	 */
	public FlexOfferSlice getFlexOfferProfile(int i) {
		return flexOfferProfileConstraints[i];
	}

	/**
	 * Returns the energy intervals.
	 * 
	 * @return the energy intervals
	 */
	public FlexOfferSlice[] getFlexOfferProfileConstraints() {
		return flexOfferProfileConstraints;
	}

	public FlexOfferState getState() {
		return state;
	}
	
	public String getStateReason() {
		return stateReason;
	}

	/**
	 * This method computes the totalEnergyConstraint of the FlexOffer.
	 * 
	 * @return EnergyConstraint
	 */
	// If total energy constraints are not specified, we simply compute them if
	// needed
	@JsonIgnore
	public FlexOfferConstraint getSumEnergyConstraints() {
		double totalLower = 0, totalUpper = 0;
		for (FlexOfferSlice i : this.flexOfferProfileConstraints) {
			totalUpper += i.getEnergyUpper(0);
			totalLower += i.getEnergyLower(0);
		}
		return new FlexOfferConstraint(totalLower, totalUpper);
	}

	/**
	 * This method computes the totalTariffConstraint (price) of the FlexOffer profile.
	 * 
	 * @return tariffConstraint of type EnergyConstraint
	 */
	@JsonIgnore
	public FlexOfferConstraint getSumTariffConstraints(){
		double totalLower = 0, totalUpper = 0;
		for (FlexOfferSlice i : this.flexOfferProfileConstraints) {
			if (i.getTariffConstraint() != null)
			{
				totalUpper += i.getTariffConstraint().getMinTariff();
				totalLower += i.getTariffConstraint().getMaxTariff();
			}
		}
		return new FlexOfferConstraint(totalLower, totalUpper);
	}

	/**
	 * Returns the total energy constraint.
	 * 
	 * @return the total energy constraint
	 */
	public FlexOfferConstraint getTotalEnergyConstraint() {
		return totalEnergyConstraint;
	}

	/**
	 * Removes the total energy constraints.
	 */
	public void removeTotalEnergyConstraint() {
		totalEnergyConstraint = null;
	}

	public void setFlexOfferSchedule(FlexOfferSchedule flexOfferSchedule) {
		this.flexOfferSchedule = flexOfferSchedule;
		this.state = FlexOfferState.Assigned;
	}

	public void setInitialFlexOfferSchedule(FlexOfferSchedule flexOfferSchedule) {
		this.flexOfferSchedule = flexOfferSchedule;
	}

	/**
	 * @param totalEnergyConstraint
	 *  the totalEnergyConstraint to set
	 */
	public void setTotalEnergyConstraint(FlexOfferConstraint totalEnergyConstraint) {
		this.totalEnergyConstraint = totalEnergyConstraint;
	}

	/**
	 * @param id
	 *  the flex-offer ID
	 */
	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getId() {
		return id;
	}

	/**
	 * Set the global ID of the legal entity role that issues this flex-offer
	 *
	 * @param offeredById the offeredById to set
	 */
	public void setOfferedById(String offeredById) {
		this.offeredById = offeredById;
	}


	public void setFlexOfferProfileConstraints(FlexOfferSlice[] slices) {
		this.flexOfferProfileConstraints = slices;
	}

	public void setStartAfterInterval(long startAfterInterval) {
		this.startAfterInterval = startAfterInterval;
	}

	public void setStartBeforeInterval(long startBeforeInterval) {
		this.startBeforeInterval = startBeforeInterval;
	}

	public void setState(FlexOfferState state) {
		this.state = state;
	}

	public void setStateReason(String stateReason) {
		this.stateReason = stateReason;
	}



	public FlexOfferSchedule getDefaultSchedule() {
		return defaultSchedule;
	}

	public void setDefaultSchedule(FlexOfferSchedule defaultSchedule) {
		this.defaultSchedule = defaultSchedule;
	}

	/**
	 * Added to address the flex-offer enhancement V1
	 * Setter and getter method for tariffConstraint
	 *
	 */
	public TariffConstraintProfile getFlexOfferTariffConstraint() {
		return flexOfferTariffConstraint;
	}

	public void setFlexOfferTariffConstraint(TariffConstraintProfile tariffProfile) {
		this.flexOfferTariffConstraint = tariffProfile;
	}


	/**
	 * Sets the total cost constraint for the flex-offer
	 *
	 * @param costConstraint the total cost constraint
	 */
	public void setTotalCostConstraint(FlexOfferConstraint costConstraint) {
		this.totalCostConstraint = costConstraint;
	}

	/**
	 * Returns the total cost constraint of this interval.
	 *
	 * @return the total cost constraint of this interval
	 */
	public FlexOfferConstraint getTotalCostConstraint() {
		return totalCostConstraint;
	}

	/**
	 * TODO: Need update to accommodate mixed flex-offers, i.e., FO with both consumption and product profiles
	 */
	@JsonIgnore
	public boolean isConsumption() {
		boolean isCons = false;
		boolean isProd = false;

		for(FlexOfferSlice slice : this.flexOfferProfileConstraints)
		{
			isCons |= slice.isConsumption();
			isProd |= slice.isProduction();
		}

		/* Is a flex-offer is neither consumption or production, it is consumption*/
		if (!isCons && !isProd)
			return true;

		return isCons;
	}

	@JsonIgnore
	public boolean isProduction() {
		boolean isProd = false;

		for(FlexOfferSlice slice : this.flexOfferProfileConstraints)
			isProd |= slice.isProduction();

		return isProd;
	}


	/**
	 * @param timeInterval
	 * @return true if timepoint is within offered flexibility
	 */
	public boolean isWithinFlexibilityRange(long timeInterval) {
		return (timeInterval >= getStartAfterInterval() && timeInterval < getEndBeforeInterval());
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ (int) (acceptBeforeInterval ^ (acceptBeforeInterval >>> 32));
		result = prime
				* result
				+ (int) (assignmentBeforeInterval ^ (assignmentBeforeInterval >>> 32));
		result = prime
				* result
				+ (int) (creationInterval ^ (creationInterval >>> 32));
		result = prime
				* result
				+ (int) (startAfterInterval ^ (startAfterInterval >>> 32));
		result = prime
				* result
				+ (int) (startBeforeInterval ^ (startBeforeInterval >>> 32));
		result = prime * result + Arrays.hashCode(flexOfferProfileConstraints);
		result = prime
				* result
				+ ((flexOfferSchedule == null) ? 0 : flexOfferSchedule.hashCode());
		result = prime
				* result
				+ ((defaultSchedule == null) ? 0 : defaultSchedule.hashCode());
		result = prime
				* result
				+ ((flexOfferTariffConstraint == null) ? 0 : flexOfferTariffConstraint.hashCode());
		result = prime * result + ((id == null)? 0 : id.hashCode());
		result = prime * result
				+ ((offeredById == null) ? 0 : offeredById.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result
				+ ((stateReason == null) ? 0 : stateReason.hashCode());
		return result;
	}



	/**
	 * @TODO Need to implement. Check constraints, etc. 
	 * @return
	 */
    public boolean isCorrect() {
        if (state == null) {
            logger.info("state is null.");
            return false;
        }
        if (offeredById == null || "".equals(offeredById)) {
            logger.info("offeredById is null or empty.");
            return false;
        }
        if (flexOfferProfileConstraints == null) {
            logger.info("profiles is null.");
            return false;
        }
        /*if (defaultSchedule != null && !defaultSchedule.isCorrect(this)) {
            logger.info("defaultSchedule is not null but incorrect");
            return false;
        }
        if (flexOfferSchedule != null && !flexOfferSchedule.isCorrect(this)) {
            logger.info("flexOfferSchedule is not null but incorrect");
            return false;
        }*/

		/*if (flexOfferTariffConstraint != null && !flexOfferTariffConstraint.isCorrect(this)) {
			logger.info("flexOfferSchedule is not null but incorrect");
			return false;
		}*/
		if (creationInterval > startAfterInterval) {
			logger.info("startAfterInterval is before creationInterval");
			return false;
		}
        if (startAfterInterval > startBeforeInterval) {
            logger.info("startAfterInterval is after startBeforeInterval");
            return false;
        }
		if (acceptBeforeInterval > startBeforeInterval) {
			logger.info("acceptanceBeforeInterval is after startBeforeInterval");
			return false;
		}
		//TODO: verify why this is valid
        if (acceptBeforeInterval > this.getEndAfterInterval()) {
            logger.info("acceptanceBeforeInterval is after endAfterInterval");
            return false;
        }
		if (assignmentBeforeInterval > startBeforeInterval) {
			logger.info("assignmentBeforeInterval is after startBeforeInterval");
			return false;
		}
		//TODO: verify why this is valid
        if (assignmentBeforeInterval > this.getEndAfterInterval()) {
            logger.info("assignmentBeforeInterval is after endBeforeInterval");
            return false;
        }
        return true;
    }

	public String toJsonString(){return null;}
	
	public String toXmlString(){return null;}

	public String getLocationId() {
		return locationId;
	}

	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}

	public FlexOfferType getFlexOfferType() {
		return flexOfferType;
	}

	public void setFlexOfferType(FlexOfferType flexOfferType) {
		this.flexOfferType = flexOfferType;
	}

	public Assignment getAssignment() {
		return assignment;
	}

	public void setAssignment(Assignment assignment) {
		this.assignment = assignment;
	}
}
