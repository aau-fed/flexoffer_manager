package org.goflex.wp2.core.util;

/*-
 * #%L
 * GOFLEX::WP2::Core Data Structures
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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by bijay on 8/30/17.
 */
public class FOAConfig {
    private static Logger logger = LoggerFactory.getLogger(FOAConfig.class);
    private Properties properties;

    public FOAConfig(String key, String def){
        String filename = System.getProperty(key, def);
        properties = new Properties();

        try {
            properties.load(new FileInputStream(filename));
        }catch (FileNotFoundException ex){
            logger.info("Properties file not found loading default properties");
        } catch (IOException e) {
            logger.info("Fail to read property file {}", filename, e);
        }

    }

    public String getString(String key, String def){return properties.getProperty(key, def);}

    public int getInt(String key, int def){
        int val = def;
        try {
            val = Integer.parseInt(properties.getProperty(key, Integer.toString(def)));
        }catch(NumberFormatException e){
            logger.info("Unable to parse integer value for key {}", key, e);
        }

        return val;
    }

    public boolean getBoolean(String key, String def){return Boolean.parseBoolean(properties.getProperty(key, def));}
}
