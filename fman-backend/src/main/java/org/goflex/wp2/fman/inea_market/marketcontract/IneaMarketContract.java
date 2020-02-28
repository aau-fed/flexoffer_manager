package org.goflex.wp2.fman.inea_market.marketcontract;

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



import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * This class defines the contract between the Aggregator and the INEA's Market.
 *  
 * @author Laurynas
 *
 */
@Configuration
@ConfigurationProperties(prefix = "inea")
public class IneaMarketContract {

	/**
	 * Aggregator pays the (large amount of ) imbalance fee, if it deviates from INEA's reference schedule
	 */
	private double imbalanceFee = 10e6; // 10EUR/kWh

	/**
	 * Imbalance cut-off margin: No imbalances cost will be applicable if actual imbalance is below this cutoff margin
	 *
	 * @return
	 */
	private double imbalanceCutOffMargin = 1; // 1 kWh

	public double getImbalanceFee() {
		return imbalanceFee;
	}

	public void setImbalanceFee(double imbalanceFee) {
		this.imbalanceFee = imbalanceFee;
	}

	public double getImbalanceCutOffMargin() {
		return imbalanceCutOffMargin;
	}

	public void setImbalanceCutOffMargin(double imbalanceAmountMargin) {
		this.imbalanceCutOffMargin = imbalanceAmountMargin;
	}
}
