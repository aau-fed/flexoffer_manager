package org.goflex.wp2.fman.optimizer;

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



import org.goflex.wp2.fman.aggregatorportfolio.AggregatorPortfolio;


/* LP based energy amount optimizer */

// TODO: Implement an LP-based amount optimizer in the future
public class AmountFlexLPOptimizer {
    private AggregatorPortfolio fp;

    public AmountFlexLPOptimizer(AggregatorPortfolio fp) {
    	this.fp = fp;

    	this._generateLPproblem();
    }

    private void _generateLPproblem() {
//        //Objective function
//        double[] c = new double[] { -1., -1. };
//
//        //Inequalities constraints
//        double[][] G = new double[][] {{4./3., -1}, {-1./2., 1.}, {-2., -1.}, {1./3., 1.}};
//        double[] h = new double[] {2., 1./2., 2., 1./2.};
//
//        //Bounds on variables
//        double[] lb = new double[] {0 , 0};
//        double[] ub = new double[] {10, 10};
//
//        //optimization problem
//        LPOptimizationRequest or = new LPOptimizationRequest();
//        or.setC(c);
//        or.setG(G);
//        or.setH(h);
//        or.setLb(lb);
//        or.setUb(ub);
//        or.setDumpProblem(true);
//
//        //optimization
//        LPPrimalDualMethod opt = new LPPrimalDualMethod();
//
//        opt.setLPOptimizationRequest(or);
//        try {
//            opt.optimize();
//        } catch (JOptimizerException e) {
//            e.printStackTrace();
//        }
    }

}
