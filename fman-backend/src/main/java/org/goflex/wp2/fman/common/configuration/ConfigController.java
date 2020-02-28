package org.goflex.wp2.fman.common.configuration;

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


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.goflex.wp2.core.entities.ResponseMessage;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author muhaftab
 * created: 12/5/18
 */

@RestController
@RequestMapping("/config")
public class ConfigController {

    @PostMapping(value = "/loglevel/{loglevel}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity setLoggingLevel(@PathVariable(value = "loglevel") String loglevel) {
        String msg;
        String packageName = "org.goflex.wp2";
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        if (loglevel.equalsIgnoreCase("ALL")) {
            loggerContext.getLogger(packageName).setLevel(Level.ALL);
            msg = "Logging level successfully set to " + loglevel;
        } else if (loglevel.equalsIgnoreCase("TRACE")) {
            loggerContext.getLogger(packageName).setLevel(Level.TRACE);
            msg = "Logging level successfully set to " + loglevel;
        } else if (loglevel.equalsIgnoreCase("DEBUG")) {
            loggerContext.getLogger(packageName).setLevel(Level.DEBUG);
            msg = "Logging level successfully set to " + loglevel;
        } else if (loglevel.equalsIgnoreCase("INFO")) {
            loggerContext.getLogger(packageName).setLevel(Level.INFO);
            msg = "Logging level successfully set to " + loglevel;
        } else if (loglevel.equalsIgnoreCase("WARN")) {
            loggerContext.getLogger(packageName).setLevel(Level.WARN);
            msg = "Logging level successfully set to " + loglevel;
        } else if (loglevel.equalsIgnoreCase("ERROR")) {
            loggerContext.getLogger(packageName).setLevel(Level.ERROR);
            msg = "Logging level successfully set to " + loglevel;
        } else if (loglevel.equalsIgnoreCase("OFF")) {
            loggerContext.getLogger(packageName).setLevel(Level.OFF);
            msg = "Logging level successfully set to " + loglevel;
        } else {
            msg = "Error, not a known loglevel: " + loglevel;
        }

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setMessage(msg);
        responseMessage.setStatus(HttpStatus.OK);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

}
