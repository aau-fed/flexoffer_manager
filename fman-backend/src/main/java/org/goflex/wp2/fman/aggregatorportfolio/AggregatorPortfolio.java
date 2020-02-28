package org.goflex.wp2.fman.aggregatorportfolio;

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



import java.util.*;
import java.util.stream.Collectors;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.goflex.wp2.core.entities.*;
import org.goflex.wp2.fman.billing.UserBillFactory;
import org.goflex.wp2.fman.inea_market.IneaMarketCommitment;
import org.goflex.wp2.fman.user.usercontract.UserContract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Embeddable;


/**
 * This is a class that represents a portfolio of all aggregator's (FMAN's) assets, including flex-offers to be optimized, etc.
 * It can be aggregated, for more efficient optimization
 *
 * @author Laurynas
 *
 */
public class AggregatorPortfolio {
	final static Logger logger = LoggerFactory.getLogger(AggregatorPortfolio.class);

    /* Fixed operational expences that do not depend on FO schedules */
    private double fixedExpences = 0;

    /* FlexOffers  - inherited from FlexOfferCollection */
    private FlexOfferCollection foCollection;

    /* The flex-offer Id -> contract map  */
    private Map<UUID, UserContract> foContMap = new HashMap<>();

    /* Market commitments / assets */
    private List<IneaMarketCommitment> mk_commit;

	public AggregatorPortfolio(double fixedExpences,
                               Collection<? extends FlexOffer> fos,
                               Map<UUID, UserContract> foContMap,
                               List<IneaMarketCommitment> mk_commit) throws Exception {

        this.fixedExpences = fixedExpences;
		this.mk_commit = mk_commit;
		this.foContMap = foContMap;
		this.foCollection = new FlexOfferCollection(fos);

		/* Flex-offer expences */
		for(FlexOffer f : this.getFlexOffers()) {

			// Check whether the flex-offer has a corresponding contract
            if (!this.foContMap.containsKey(f.getId())) {
			    throw new Exception("A flex-offer is not linked with a flex-offer contract");
            }
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AggregatorPortfolio that = (AggregatorPortfolio) o;
		return Double.compare(that.fixedExpences, fixedExpences) == 0 &&
				Objects.equals(foCollection, that.foCollection) &&
				Objects.equals(foContMap, that.foContMap) &&
				Objects.equals(mk_commit, that.mk_commit);
	}

	public int getFlexOfferIdHash() {
		return Objects.hash(this.foCollection.getFos().stream().map(f -> f.getId()).collect(Collectors.toList()));
	}

	public FlexOfferCollection getFoCollection() {   return foCollection;  }

    public void setFoCollection(FlexOfferCollection foCollection) {   this.foCollection = foCollection;  }

    @JsonIgnore
    public List<FlexOffer> getFlexOffers() {
        return this.foCollection.getFos();
    }

    public Map<UUID, UserContract> getFoContMap() {
        return foContMap;
    }

    public List<IneaMarketCommitment> getMarketCommitments() {
        return mk_commit;
    }

    /* Timeseries */
	public TimeSeries getCommitmentEnergy(Double noDataValue) {
		try {
			FlexOfferCollection c = new FlexOfferCollection(
					this.mk_commit
					.stream()
					.map( m -> m.getWinningFlexOffer() )
					.collect(Collectors.toList()));

			return c.getEnergySeriesByType(TimeSeriesType.tstScheduledEnergy, noDataValue);
		} catch (Exception e) {
			return new TimeSeries();
		}
	}

	/* Compute direct flex-offer schedule expences */
	@JsonProperty("flexOfferCosts")
	public double computeFlexOfferScheduleExpenses() {
		double value = 0;
		for(FlexOffer f : this.getFlexOffers()) {
			value += UserBillFactory.getFlexOfferSchedulingCost(this.foContMap.get(f.getId()), f);
		}
		return value;
	}

	/* Compute market gains */
    @JsonProperty("marketGains")
	public double computeMarketGains() {
		double value=0;

		for (IneaMarketCommitment mc : this.mk_commit) {

			value += mc.getExceptedMarketGain();
		}

		return value;
	}

    /* Compute expected market imbalance */
    @JsonProperty("marketImbalance")
    public double computeMarketImbalance() {
        double imbalance = 0;

        for (IneaMarketCommitment mc : this.mk_commit) {
            imbalance += mc.getExpectedImbalance(this.getFlexOffers());
        }

        return imbalance;
    }

	/* Compute expected market imbalance costs */
    @JsonProperty("marketImbalanceCosts")
	public double computeMarketImbalanceCosts() {
		double imbalanceCost = 0;

		for (IneaMarketCommitment mc : this.mk_commit) {
				imbalanceCost += mc.getExpectedImbalanceFee(this.getFlexOffers());
		}

		return imbalanceCost ;
	}

    @JsonProperty("portfolioCosts")
	public double computePortfolioCosts() {
        double value = this.fixedExpences;

        /* Flex-offer expenses */
        value += this.computeFlexOfferScheduleExpenses();

        /* Market imbalances */
        value += this.computeMarketImbalanceCosts();

        return value;
    }


	/* Compute the schedule cost */
    @JsonProperty("totalPortfolioCost")
	public double computeTotalPortfolioCost(){
		double value = this.computePortfolioCosts();

		/* Market gains */
		value -= this.computeMarketGains();


		return value;
	}

	public double getFixedExpences() {
		return fixedExpences;
	}

	public void setFixedExpences(double fixedExpences) {
		this.fixedExpences = fixedExpences;
	}

}
