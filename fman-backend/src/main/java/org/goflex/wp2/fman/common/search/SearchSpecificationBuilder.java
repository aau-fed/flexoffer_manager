package org.goflex.wp2.fman.common.search;

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


import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchSpecificationBuilder<T> {
    private final Class<T> typeParameterClass;
    private final List<SearchCriteria> params;

    public SearchSpecificationBuilder(Class<T> typeParameterClass) {
        this.typeParameterClass = typeParameterClass;
        this.params = new ArrayList<SearchCriteria>();
    }

    public SearchSpecificationBuilder(Class<T> typeParameterClass, String searchString) throws Exception {
        this(typeParameterClass);

        this.parseString(searchString);
    }

    public void parseString(String searchString) throws Exception {
        Pattern pattern = Pattern.compile("(\\w+?)(:|%|<|>)((\\w|-|:|.)+?),");
        Matcher matcher = pattern.matcher(searchString + ",");
        while (matcher.find()) {
            this.with(matcher.group(1), matcher.group(2), this.castToComparable(matcher.group(1), matcher.group(3)));
        }
    }

    public static java.util.Date fromISO8601UTC(String dateStr) throws ParseException {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        df.setTimeZone(tz);

        return df.parse(dateStr);
    }

    private Comparable<? extends Object> castToComparable(String key, String str) throws Exception {
        Field fld = null;

        for (Field f : this.typeParameterClass.getDeclaredFields()) {
            if (f.getName().equalsIgnoreCase(key)) {
                fld = f;
                break;
            }
        }

        if (fld == null) throw new Exception("No attribute "+key+" is found");

        if (fld.getType().isAssignableFrom(Date.class))
            return fromISO8601UTC(str);

        if (fld.getType().isEnum()) {
            for (Object c : fld.getType().getEnumConstants()) {
                if (c.toString().equalsIgnoreCase(str)) {
                    return Enum.valueOf((Class<Enum>) fld.getType(), str);
                }
            }

            throw new Exception("Enum has no such value");
        }

        return str;
    }

    public SearchSpecificationBuilder with(String key, String operation, Comparable<? extends Object> value) {
        params.add(new SearchCriteria(key, operation, Comparable.class.cast(value)));
        return this;
    }

    public Specification<T> build() {
        if (params.size() == 0) {
            return null;
        }

        List<Specification<T>> specs = new ArrayList<Specification<T>>();
        for (SearchCriteria param : params) {
            specs.add(new SearchSpecification<T>(param));
        }

        Specification<T> result = specs.get(0);
        for (int i = 1; i < specs.size(); i++) {
            result = Specification.where(result).and(specs.get(i));
        }
        return result;
    }

}
