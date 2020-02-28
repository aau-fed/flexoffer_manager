package org.goflex.wp2.fman.common.currency;

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


import org.goflex.wp2.fman.common.configuration.PersistentConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


/**
 * This service builds instances of the flex-offer portfolio, applying aggregation if needed, etc.
 */
@Service
public class CurrencyService {
    static final String C_CURRENCY_SYMBOL = "fman.currency.symbol";
    static final String C_CURRENCY_RATIOTOEUR = "fman.currency.RATIOTOEUR";

    @Autowired
    private PersistentConfigurationService cfgService;

    public String getCurrencySymbol() {
        return this.cfgService.getValue(C_CURRENCY_SYMBOL)
                              .orElse("EUR");
    }

    public Double getCurrencyRatioToEur() {
        return this.cfgService.getValue(C_CURRENCY_RATIOTOEUR)
                              .map(s -> Double.valueOf(s))
                              .orElse(1.0);
    }


    public void setCurrency(String symbol, Double ratioToEur) {
        this.cfgService.setValue(C_CURRENCY_SYMBOL, symbol);
        this.cfgService.setValue(C_CURRENCY_RATIOTOEUR, ratioToEur.toString());
    }
}
