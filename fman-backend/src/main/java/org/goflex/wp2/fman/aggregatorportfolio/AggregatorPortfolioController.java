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


import io.swagger.annotations.ApiOperation;
import org.goflex.wp2.core.entities.FlexOffer;
import org.goflex.wp2.fman.optimizer.OptimizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

/*
 * Created by Laurynas Siksnys
 */
@RestController
@PreAuthorize("hasRole('ROLE_ADMIN')")              // Only ADMINs can access this
@RequestMapping("/portfolio")
public class AggregatorPortfolioController {
    private static final Logger logger = LoggerFactory.getLogger(AggregatorPortfolioController.class);

    @Autowired
    private AggregatorPortfolioService pService;

    @Autowired
    private OptimizationService optService;

    @GetMapping(value="/", produces = "application/json")
    @ApiOperation(value = "${AggregatorPortfolioController.getActivePortfolio}")
    public AggregatorPortfolio getPortfolio() throws Exception {

        AggregatorPortfolio p = this.pService.getActivePortfolio();

        return p;
    }

    /* Gets active portfolio flex-offers */
    @GetMapping(value="/flexoffers", produces = "application/json")
    @ApiOperation(value = "${AggregatorPortfolioController.getPFlexOffers}")
    public Collection<FlexOffer> getPFlexOffers() throws Exception {

        AggregatorPortfolio p = this.pService.getActivePortfolio();

        return p.getFlexOffers();
    }

    /* Gets optimized active portfolio flex-offers */
    @GetMapping(value="/opt_flexoffers", produces = "application/json")
    @ApiOperation(value = "${AggregatorPortfolioController.getPOPTFlexOffers}")
    public Collection<FlexOffer> getPOPTFlexOffers() throws Exception {

        AggregatorPortfolio p = this.pService.getLastOrActivePortfolio();

        if (p == null) {
            p = this.optService.runImmediateOptimization();
        }

        return p.getFlexOffers();
    }




}
