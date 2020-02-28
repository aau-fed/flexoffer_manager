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
import org.goflex.wp2.fman.optimizer.OptimizationObjective;

import java.util.Date;

/**
 * This provides summarized FMAN configuration
 */
public class DashboardConfig {
    private Boolean aggregationEnabled;
    private Boolean tradingEnabled;
    private OptimizationObjective optimizationObjective;
    private String currencySymbol;
    private Double currencyRatioToEur;
    private Date optimizationFrom;
    private Date optimizationTo;

    public Boolean getAggregationEnabled() {
        return aggregationEnabled;
    }

    public void setAggregationEnabled(Boolean aggregationEnabled) {
        this.aggregationEnabled = aggregationEnabled;
    }

    public Boolean getTradingEnabled() {
        return tradingEnabled;
    }

    public void setTradingEnabled(Boolean tradingEnabled) {
        this.tradingEnabled = tradingEnabled;
    }

    public OptimizationObjective getOptimizationObjective() {
        return optimizationObjective;
    }

    public void setOptimizationObjective(OptimizationObjective optimizationObjective) {
        this.optimizationObjective = optimizationObjective;
    }

    public Date getOptimizationFrom() {
        return optimizationFrom;
    }

    public void setOptimizationFrom(Date optimizationFrom) {
        this.optimizationFrom = optimizationFrom;
    }

    public Date getOptimizationTo() {
        return optimizationTo;
    }

    public void setOptimizationTo(Date optimizationTo) {
        this.optimizationTo = optimizationTo;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public Double getCurrencyRatioToEur() {
        return currencyRatioToEur;
    }

    public void setCurrencyRatioToEur(Double currencyRatioToEur) {
        this.currencyRatioToEur = currencyRatioToEur;
    }

}
