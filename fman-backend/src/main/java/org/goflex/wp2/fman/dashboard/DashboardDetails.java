package org.goflex.wp2.fman.dashboard;

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



import org.goflex.wp2.core.entities.TimeSeries;
import org.goflex.wp2.fman.kpis.KPIAggregatorBenefit;
import org.goflex.wp2.fman.kpis.KPIEnergyAdaptability;
import org.goflex.wp2.fman.optimizer.OptimizationObjective;

/**
 * This provided summarized details of the dashboard content
 */
public class DashboardDetails {
    private Integer numOfActiveUsers;
    private Integer totalNoOfUsers;
    private Long currentPlanningInterval;
    private OptimizationObjective optimizationObjective;
    private Double currentOperatingPower;
    private TimeSeries defaultSchedule;
    private TimeSeries activeSchedule;
    private TimeSeries overallLowSchedule;
    private TimeSeries overallHighSchedule;
    private TimeSeries aggregatedMeasurements;
    private TimeSeries marketCommitments;
    private KPIEnergyAdaptability kpiLoadAdaptability;
    private KPIAggregatorBenefit kpiAggregatorBenefit;

    public Integer getNumOfActiveUsers() {
        return numOfActiveUsers;
    }

    public void setNumOfActiveUsers(Integer numOfActiveUsers) {
        this.numOfActiveUsers = numOfActiveUsers;
    }

    public Integer getTotalNoOfUsers() {
        return totalNoOfUsers;
    }

    public void setTotalNoOfUsers(Integer totalNoOfUsers) {
        this.totalNoOfUsers = totalNoOfUsers;
    }

    public TimeSeries getDefaultSchedule() {
        return defaultSchedule;
    }

    public void setDefaultSchedule(TimeSeries defaultSchedule) {
        this.defaultSchedule = defaultSchedule;
    }

    public TimeSeries getActiveSchedule() {
        return activeSchedule;
    }

    public void setActiveSchedule(TimeSeries activeSchedule) {
        this.activeSchedule = activeSchedule;
    }

    public TimeSeries getOverallLowSchedule() {
        return overallLowSchedule;
    }

    public void setOverallLowSchedule(TimeSeries overallLowSchedule) {
        this.overallLowSchedule = overallLowSchedule;
    }

    public TimeSeries getOverallHighSchedule() {
        return overallHighSchedule;
    }

    public void setOverallHighSchedule(TimeSeries overallHighSchedule) {   this.overallHighSchedule = overallHighSchedule;  }

    public TimeSeries getAggregatedMeasurements() { return aggregatedMeasurements;  }

    public void setAggregatedMeasurements(TimeSeries aggregatedMeasurements) {   this.aggregatedMeasurements = aggregatedMeasurements;   }

    public Long getCurrentPlanningInterval() {
        return currentPlanningInterval;
    }

    public void setCurrentPlanningInterval(Long currentPlanningInterval) {
        this.currentPlanningInterval = currentPlanningInterval;
    }

    public TimeSeries getMarketCommitments() {   return marketCommitments;  }

    public void setMarketCommitments(TimeSeries marketCommitments) {   this.marketCommitments = marketCommitments;  }

    public KPIEnergyAdaptability getKpiLoadAdaptability() { return kpiLoadAdaptability;  }

    public void setKpiLoadAdaptability(KPIEnergyAdaptability kpiLoadAdaptability) {   this.kpiLoadAdaptability = kpiLoadAdaptability;  }

    public OptimizationObjective getOptimizationObjective() {
        return optimizationObjective;
    }

    public void setOptimizationObjective(OptimizationObjective optimizationObjective) {
        this.optimizationObjective = optimizationObjective;
    }

    public KPIAggregatorBenefit getKpiAggregatorBenefit() {
        return kpiAggregatorBenefit;
    }

    public void setKpiAggregatorBenefit(KPIAggregatorBenefit kpiAggregatorBenefit) {
        this.kpiAggregatorBenefit = kpiAggregatorBenefit;
    }


    public Double getCurrentOperatingPower() {
        return currentOperatingPower;
    }

    public void setCurrentOperatingPower(Double currentOperatingPower) {
        this.currentOperatingPower = currentOperatingPower;
    }

}
