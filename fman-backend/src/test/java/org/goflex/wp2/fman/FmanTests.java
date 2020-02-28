package org.goflex.wp2.fman;

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


import org.goflex.wp2.agg.api.AggregationException;
import org.goflex.wp2.core.entities.AggregatedFlexOffer;
import org.goflex.wp2.core.entities.FlexOffer;
import org.goflex.wp2.fman.aggregation.AggregationService;
import org.goflex.wp2.fman.aggregatorportfolio.AggregatorPortfolio;
import org.goflex.wp2.fman.aggregatorportfolio.AggregatorPortfolioService;
import org.goflex.wp2.fman.flexoffer.FlexOfferService;
import org.goflex.wp2.fman.shell.DemoFlexOffers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest()
public class FmanTests {

    private DemoFlexOffers demoGenerator = new DemoFlexOffers();

    @Autowired
    private FlexOfferService foSvc;

    @Autowired
    private AggregationService agg;

    @Autowired
    private AggregatorPortfolioService portService;

    private void generateSomeFlexOffers(int numFos) {
        // Clear the FOs
        this.foSvc.getAllFlexOffers()
                .stream()
                .forEach( f-> this.foSvc.deleteFlexOffer(f.getFlexoffer()) );


        // Generate FOs
        for (FlexOffer f : this.demoGenerator.demoConsumptionFOs(numFos)) {
            this.foSvc.createFlexOffer(f);
        }
    }

    @Test
    @Transactional
    public void testAggregation() throws InterruptedException {
        int numFos = 10;

        this.generateSomeFlexOffers(numFos);

        // Check aggregation result
        int count = 0;
        try {

            Collection<AggregatedFlexOffer> afos = this.agg.aggregateFlexOffers(foSvc.getAllActiveFlexOffers()
                                                                                     .stream()
                                                                                     .map( f -> f.getFlexoffer())
                                                                                     .collect(Collectors.toList()) );

            count = afos.stream()
                        .mapToInt(a -> a.getSubFoMetas().size())
                        .sum();

        } catch (AggregationException ex) {

        }

        assertThat(count).isEqualTo(numFos);
    }

    @Test
    @Transactional
    public void testPorfolioService() throws Exception {
        int numFos = 10;

        this.generateSomeFlexOffers(numFos);

        AggregatorPortfolio port = this.portService.getActivePortfolio();

        assertThat(new ArrayList(port.getFlexOffers()).size()).isEqualTo(numFos);

    }

}