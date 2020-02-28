package org.goflex.wp2.fman.inea_market;

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



import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.goflex.wp2.core.entities.FlexOffer;
import org.goflex.wp2.core.entities.FlexOfferSchedule;
import org.goflex.wp2.core.util.FlexOfferJsonConvertor;
import org.goflex.wp2.core.util.FlexOfferScheduleJsonConvertor;
import org.goflex.wp2.fman.inea_market.marketcontract.IneaMarketContract;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "trading_history", indexes = { @Index(columnList="ineaId"), @Index(columnList="state"),  @Index(columnList="tradingIntervalFrom, tradingIntervalTo") })
public class IneaTransactionHistoryT {

    public enum TransactionState {
        tActive,            // The transaction is still active
        tCancelled,          // The transaction is not active, e.g., error occured or it has been overriden by a new transaction
        tClosed             // The transaction was active, and now closed
    }


    @Id
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "transactionTime", columnDefinition = "DATETIME")
    private Date transactionTime;

    @Column(nullable = true)
    private Long ineaId;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private TransactionState state;

    @Column(nullable = true,length=255)
    private String stateMessage;

    @Column(name = "FlexOffer", nullable = false, length=100000)
    @Convert(converter = FlexOfferJsonConvertor.class)
    private FlexOffer tradingFlexOffer;

    @Column(nullable = false)
    private Long tradingIntervalFrom;

    @Column(nullable = false)
    private Long tradingIntervalTo;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "schedulingTime", columnDefinition = "DATETIME", nullable = true)
    private Date schedulingTime;

//    @Column(name = "WinningSchedule", nullable = true, length = 100000)
//    @Convert(converter = FlexOfferScheduleJsonConvertor.class)
//    private FlexOfferSchedule winningSchedule;

    @Embedded()
    private IneaMarketContract activeContract;

    public Date getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(Date transactionTime) {
        this.transactionTime = transactionTime;
    }

    public FlexOffer getTradingFlexOffer() {
        return tradingFlexOffer;
    }

    public void setTradingFlexOffer(FlexOffer tradingFlexOffer) {
        this.tradingFlexOffer = tradingFlexOffer;
    }

    public IneaMarketContract getActiveContract() {
        return activeContract;
    }

    public void setActiveContract(IneaMarketContract activeContract) {
        this.activeContract = activeContract;
    }

    public Long getIneaId() {
        return ineaId;
    }

    public void setIneaId(Long ineaId) {
        this.ineaId = ineaId;
    }

//    public FlexOfferSchedule getWinningSchedule() {   return winningSchedule;   }
//
//    public void setWinningSchedule(FlexOfferSchedule winningSchedule) {
//        this.winningSchedule = winningSchedule;
//    }

    public TransactionState getState() {    return state;   }

    public void setState(TransactionState state) {    this.state = state;   }

    public String getStateMessage() {     return stateMessage;   }

    public void setStateMessage(String stateMessage) {    this.stateMessage = stateMessage;   }

    public Long getTradingIntervalFrom() {    return tradingIntervalFrom;  }

    public void setTradingIntervalFrom(Long tradingIntervalFrom) {  this.tradingIntervalFrom = tradingIntervalFrom;  }

    public Long getTradingIntervalTo() { return tradingIntervalTo;  }

    public void setTradingIntervalTo(Long tradingIntervalTo) {  this.tradingIntervalTo = tradingIntervalTo;  }

    public Date getSchedulingTime() {
        return this.schedulingTime;
    }

    public void setSchedulingTime(Date time) {
        this.schedulingTime = time;
    }

    @JsonProperty("tradingTimeFrom")
    public Date getTradingTimeFrom() {
        return FlexOffer.toAbsoluteTime(this.getTradingIntervalFrom());
    }

    @JsonProperty("tradingTimeTo")
    public Date getTradingTimeTo() {
        return new Date(FlexOffer.toAbsoluteTime(this.getTradingIntervalTo() + 1).getTime() - 1);
    }

}
