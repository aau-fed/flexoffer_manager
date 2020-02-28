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


import java.util.Date;

/* This encompass a summary of all KPIs */
public class KPISummary {
    private Date periodFrom = null;
    private Date periodTo = null;
    private String summaryOfUserName = null; /* Indicate, if summary was generate for a specific user*/
    private int numberOfFOs = 0;
    private int numberOfMarketCommitments = 0;


    private KPIEnergyAdaptability adaptabilityKPIs = null;
    private KPIAggregatorBenefit aggregatorBenefitKPIs = null;

    public Date getPeriodFrom() {
        return periodFrom;
    }

    public void setPeriodFrom(Date periodFrom) {
        this.periodFrom = periodFrom;
    }

    public Date getPeriodTo() {
        return periodTo;
    }

    public void setPeriodTo(Date periodTo) {
        this.periodTo = periodTo;
    }


    public KPIEnergyAdaptability getAdaptabilityKPIs() {
        return adaptabilityKPIs;
    }

    public void setAdaptabilityKPIs(KPIEnergyAdaptability adaptabilityKPIs) {
        this.adaptabilityKPIs = adaptabilityKPIs;
    }

    public KPIAggregatorBenefit getAggregatorBenefitKPIs() {
        return aggregatorBenefitKPIs;
    }

    public void setAggregatorBenefitKPIs(KPIAggregatorBenefit aggregatorBenefitKPIs) {
        this.aggregatorBenefitKPIs = aggregatorBenefitKPIs;
    }

    public int getNumberOfFOs() {
        return numberOfFOs;
    }

    public void setNumberOfFOs(int numberOfFOs) {
        this.numberOfFOs = numberOfFOs;
    }

    public int getNumberOfMarketCommitments() {
        return numberOfMarketCommitments;
    }

    public void setNumberOfMarketCommitments(int numberOfMarketCommitments) {
        this.numberOfMarketCommitments = numberOfMarketCommitments;
    }

    public String getSummaryOfUserName() {
        return summaryOfUserName;
    }

    public void setSummaryOfUserName(String summaryOfUserName) {
        this.summaryOfUserName = summaryOfUserName;
    }
}
