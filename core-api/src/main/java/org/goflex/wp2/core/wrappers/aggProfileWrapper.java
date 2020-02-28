package org.goflex.wp2.core.wrappers;

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


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.goflex.wp2.core.entities.FlexOfferConstraint;
import org.goflex.wp2.core.entities.FlexOfferTariffConstraint;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;

/**
 * Created by bijay on 8/28/17.
 */

@XmlAccessorType(XmlAccessType.FIELD)
public class aggProfileWrapper implements Serializable {

    private int durationSeconds;
    private  int costPerEnergyUnitLimit;
    private FlexOfferConstraint energyConstraint;


    private FlexOfferTariffConstraint tariffConstraint;

    @JsonCreator
    public aggProfileWrapper(@JsonProperty(value="durationSeconds") int durationSeconds, @JsonProperty(value="costPerEnergyUnitLimit") int costPerEnergyUnitLimit,
                             @JsonProperty(value="energyProfile") FlexOfferConstraint energyProfile,@JsonProperty(value= "tariffConstraint", required = false) FlexOfferTariffConstraint tariffConstraint){
        this.durationSeconds= durationSeconds;
        this.costPerEnergyUnitLimit = costPerEnergyUnitLimit;
        this.energyConstraint = energyProfile;
        this.tariffConstraint = tariffConstraint;
    }


    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public int getCostPerEnergyUnitLimit() {
        return costPerEnergyUnitLimit;
    }

    public void setCostPerEnergyUnitLimit(int costPerEnergyUnitLimit) {
        this.costPerEnergyUnitLimit = costPerEnergyUnitLimit;
    }

    public FlexOfferConstraint getEnergyConstraint() {
        return energyConstraint;
    }

    public void setEnergyConstraint(FlexOfferConstraint energyConstraint) {
        this.energyConstraint = energyConstraint;
    }

    @JsonProperty
    public FlexOfferTariffConstraint getTariffConstraint() {
        return tariffConstraint;
    }

    @JsonIgnore
    public void setTariffConstraint(FlexOfferTariffConstraint tariffConstraint) {
        this.tariffConstraint = tariffConstraint;
    }
}
