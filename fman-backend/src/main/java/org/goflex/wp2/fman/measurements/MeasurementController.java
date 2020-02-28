package org.goflex.wp2.fman.measurements;

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
import org.goflex.wp2.core.entities.TimeSeries;
import org.goflex.wp2.fman.common.exception.CustomException;
import org.goflex.wp2.fman.common.search.SearchSpecificationBuilder;
import org.goflex.wp2.fman.measurements.entities.MeasurementPayload;
import org.goflex.wp2.fman.security.SecurityService;
import org.goflex.wp2.fman.user.UserService;
import org.goflex.wp2.fman.user.UserT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * Created by Laurynas Siksnys
 */
@RestController
@RequestMapping("/measurement")
public class MeasurementController {
    private static final Logger logger = LoggerFactory.getLogger(MeasurementController.class);

    @Autowired
    private SecurityService securityService;

    @Autowired
    private MeasurementService measSvc;

    @Autowired
    private UserService usrSvc;

    /* This returns all raw measurements, for a specific user  */
    @GetMapping(value="/", produces = "application/json")
    @ApiOperation(value = "${MeasurementController.getMeasurements}")
    public ResponseEntity<List<MeasurementPayload>> getMeasurements(
                                                    Authentication auth,
                                                    @PageableDefault(size = 10, sort = "id") Pageable pageable,
                                                    @RequestParam(value = "search") String search) throws Exception {

        SearchSpecificationBuilder<MeasurementT> builder = new SearchSpecificationBuilder<>(MeasurementT.class, search);


        // If the current user is not a broker or admin, we only allow the user to see its own measurements
        if (!this.securityService.isBrokerAdmin(auth)) {
            builder.with("userName", ":", this.securityService.getUserName(auth));
        }

        Specification<MeasurementT> spec = builder.build();

        Page<MeasurementT> page = measSvc.findAll(spec, pageable);

        HttpHeaders headers = new HttpHeaders() {
            {
                add("Access-Control-Expose-Headers", "Content-Range");
                add("Content-Range", String.valueOf(page.getTotalElements()));
            }
        };


        ResponseEntity<List<MeasurementPayload>> result = new ResponseEntity<>(page.getContent().stream()
                .map(MeasurementPayload::mapper)
                .collect(Collectors.toList()), headers, HttpStatus.OK);

        return result;
    }

    @PostMapping(value="/post", consumes = "application/json")
    @ApiOperation(value = "${MeasurementController.postMeasurements}")
    public ResponseEntity postMeasurements(Authentication auth,
                                           @RequestBody List<MeasurementPayload> measurements) {

        String userName = this.securityService.getUserName(auth);

        Map<String, UserT> userMap = new HashMap<>();

        for (MeasurementPayload m : measurements) {
            if (m.getUserName().isEmpty()) {
                m.setUserName(userName);
            }

            if (!this.securityService.isBrokerAdmin(auth) && !m.getUserName().equals(userName)) {
                throw new CustomException("Non-admin user is not allowed to upload measurements of another user.", HttpStatus.FORBIDDEN);
            }

            if (!userMap.containsKey(m.getUserName())) {
                UserT u = this.usrSvc.getUserByUserName(m.getUserName());

                // TODO: Check if an exception i raised is not found
                userMap.put(m.getUserName(), u);
            }
        }


        this.measSvc.postMeasurements(measurements
                                        .stream()
                                        .map( m -> {
                                            MeasurementT t = new MeasurementT();
                                            t.setUser(userMap.get(m.getUserName()));
                                            t.setUserName(m.getUserName());
                                            t.setTimeStamp(m.getTimeStamp());
                                            t.setTimeInterval(FlexOffer.toFlexOfferTime(m.getTimeStamp()));
                                            t.setCumulativeEnergy(m.getCumulativeEnergy());
                                            return t;
                                        })
                                        .collect(Collectors.toList()));

        return ResponseEntity.ok().build();
    }


    private DateFormat _dateFromat = new SimpleDateFormat();

    /* This returns all raw measurements, for a specific user  */
    @GetMapping(value="/series", produces = "application/json")
    @ApiOperation(value = "${MeasurementController.getUserMeasurementSeries}")
    public ResponseEntity<TimeSeries> getMeasurementSeries(
            Authentication auth,
            @PathParam("userName") String userName,
            @PathParam("timeFrom") String timeFrom,
            @PathParam("timeTo") String timeTo,
            @PathParam("normalize") Boolean normalize

    ) throws Exception {

        if (userName == null || userName.isEmpty()) {
            userName = this.securityService.getUserName(auth);
        }

        if (!this.securityService.isBrokerAdmin(auth) && userName != this.securityService.getUserName(auth)) {
            throw new CustomException("User "+this.securityService.getUserName(auth)+ " has no access to the data of the user " + userName, HttpStatus.FORBIDDEN );
        }

        long tiFrom = FlexOffer.toFlexOfferTime( timeFrom != null ? _dateFromat.parse(timeFrom) : new Date());
        long tiTo = FlexOffer.toFlexOfferTime( timeTo != null ? _dateFromat.parse(timeTo) : new Date());

        return ResponseEntity.ok(this.measSvc.getUserMeasurementSeries(userName, tiFrom, tiTo, true));
    }

}
