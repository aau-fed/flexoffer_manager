package org.goflex.wp2.fman.aggregation;

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
import org.goflex.wp2.agg.api.FOAggParameters;
import org.goflex.wp2.agg.impl.foaggregation.FOAggregation;
import org.goflex.wp2.core.entities.*;
import org.goflex.wp2.fman.user.usercontract.UserContract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.*;

/* This service handles FO aggregation, disaggregation */
@Configuration
@Service
public class AggregationService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    // Aggregation component instance and its parameters
    private FOAggregation ac;
    private FOAggParameters pars;

    @Autowired
    public AggregationService(AggregationParameters config_params) throws AggregationException {
        this.ac = new FOAggregation();
        this.pars = new FOAggParameters();

        // Generate aggregation parameters

        // This ensures that production and consumption FOs are aggregated separatelly
        this.pars.setPreferredProfileShape(FOAggParameters.ProfileShape.psAlignBaseline);
        this.pars.getConstraintPair().aggregateOfSameType = FOAggParameters.ConstraintType.acSet;

        if (config_params.getTimeFlexibilityTolerance().isPresent()) {
            this.pars.getConstraintPair().timeFlexibilityTolerance = config_params.getTimeFlexibilityTolerance().get();
            this.pars.getConstraintPair().timeFlexibilityToleranceType = FOAggParameters.ConstraintType.acSet;
        }
        if (config_params.getStartAfterTolerance().isPresent()) {
            this.pars.getConstraintPair().startAfterTolerance = config_params.getStartAfterTolerance().get();
            this.pars.getConstraintPair().startAfterToleranceType = FOAggParameters.ConstraintType.acSet;
        }
    }

    private UUID _generateAFlexOfferHash(AggregatedFlexOffer afo) {

        // Generate reproducable UUID
        final int prime = 31;
        int hash = 1;
        for (AggregatedFlexOfferMetaData m : afo.getSubFoMetas()) {
            hash = hash * prime + m.getSubFlexOffer().getId().hashCode();
        }

        return UUID.nameUUIDFromBytes(ByteBuffer.allocate(4).putInt(hash).array());
    }


    /* Raw aggregation of FOs */
    public Collection<AggregatedFlexOffer> aggregateFlexOffers(Collection<FlexOffer> fos) throws AggregationException {
        /* Aggregate */
        Collection<AggregatedFlexOffer> afos = this.ac.Aggregate(this.pars, fos);

        /* Apply unique Id */
        afos.forEach( f -> f.setId(this._generateAFlexOfferHash(f)));

        return afos;
    }

    /* Raw flex-offer disaggregation */
    public Collection<FlexOffer> disaggregateAFlexOffer(AggregatedFlexOffer afo) throws AggregationException {
        return this.ac.Disaggregate(afo);
    }


    /* In addition to FlexOffer aggregation, we also support UserContract aggregation */
    public UserContract aggregateUserContracts(List<UserContract> subContracts) {
        if (subContracts.size()==0)
            return UserContract.DEFAULT;

        UserContract c = new UserContract();
        // Aggregate fixed expenses
        c.setUserId(0l); // PseudoId
        c.setValid(true);
        c.setDefault(false);
        c.setFixedReward(subContracts.stream()
                .mapToDouble(t->t.getFixedReward())
                .sum());
        c.setEnergyFlexReward(subContracts.stream()
                .mapToDouble(t->t.getEnergyFlexReward())
                .max()
                .orElse(0));

        c.setTimeFlexReward(subContracts.stream()
                .mapToDouble(t->t.getTimeFlexReward())
                .max()
                .orElse(0));

        c.setSchedulingFixedReward(subContracts.stream()
                .mapToDouble(t->t.getSchedulingFixedReward())
                .sum());

        c.setSchedulingEnergyReward(subContracts.stream()
                .mapToDouble(t->t.getSchedulingEnergyReward())
                .max()
                .orElse(0));


        c.setSchedulingStartTimeReward(subContracts.stream()
                .mapToDouble(t->t.getSchedulingStartTimeReward())
                .max()
                .orElse(0));

        return c;
    }


}
