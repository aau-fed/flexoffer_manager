package org.goflex.wp2.fman.kpis;

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


public class KPIEnergyAdaptability {

    private double consumptionVariability; /* dkWh/h */
    private double consumptionPeak; /* kWh/h */
    private double consumptionAdaptabilityLevel; /* 0..1. How much a consumption is flexible with respect to a peak load */
    private double consumptionFlexibilityLevel; /* 0..1. What % of time periods consumption is flexible? */

    private double productionVariability;

    private double productionPeak;
    private double productionAdaptabilityLevel;/* 0..1. How much production is flexible with respect to a peak load */
    private double productionFlexibilityLevel; /* 0..1. What % of time periods production is flexible? */

    private double flexibilityActivationLevel; /* 0..1. What % of time periods have flexibility activated (default != scheduled) */

    private double avgBaselineEnergy;
    private double avgScheduledEnergy;

    public double getConsumptionVariability() {
        return consumptionVariability;
    }

    public void setConsumptionVariability(double consumptionVariability) {
        this.consumptionVariability = consumptionVariability;
    }

    public double getConsumptionPeak() {
        return consumptionPeak;
    }

    public void setConsumptionPeak(double consumptionPeak) {
        this.consumptionPeak = consumptionPeak;
    }

    public double getConsumptionAdaptabilityLevel() {
        return consumptionAdaptabilityLevel;
    }

    public void setConsumptionAdaptabilityLevel(double consumptionAdaptabilityLevel) {
        this.consumptionAdaptabilityLevel = consumptionAdaptabilityLevel;
    }

    public double getProductionVariability() {
        return productionVariability;
    }

    public void setProductionVariability(double productionVariability) {
        this.productionVariability = productionVariability;
    }

    public double getProductionPeak() {
        return productionPeak;
    }

    public void setProductionPeak(double productionPeak) {
        this.productionPeak = productionPeak;
    }

    public double getProductionAdaptabilityLevel() {
        return productionAdaptabilityLevel;
    }

    public void setProductionAdaptabilityLevel(double productionAdaptabilityLevel) {
        this.productionAdaptabilityLevel = productionAdaptabilityLevel;
    }

    public double getAvgBaselineEnergy() {
        return avgBaselineEnergy;
    }

    public void setAvgBaselineEnergy(double avgBaselineEnergy) {
        this.avgBaselineEnergy = avgBaselineEnergy;
    }

    public double getAvgScheduledEnergy() {
        return avgScheduledEnergy;
    }

    public void setAvgScheduledEnergy(double avgScheduledEnergy) {
        this.avgScheduledEnergy = avgScheduledEnergy;
    }

    public double getConsumptionFlexibilityLevel() {
        return consumptionFlexibilityLevel;
    }

    public void setConsumptionFlexibilityLevel(double consumptionFlexibilityLevel) {
        this.consumptionFlexibilityLevel = consumptionFlexibilityLevel;
    }

    public double getProductionFlexibilityLevel() {
        return productionFlexibilityLevel;
    }

    public void setProductionFlexibilityLevel(double productionFlexibilityLevel) {
        this.productionFlexibilityLevel = productionFlexibilityLevel;
    }

    public double getFlexibilityActivationLevel() {
        return flexibilityActivationLevel;
    }

    public void setFlexibilityActivationLevel(double flexibilityActivationLevel) {
        this.flexibilityActivationLevel = flexibilityActivationLevel;
    }

}
