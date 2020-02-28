package org.goflex.wp2.fman.flexoffer;

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


import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import org.goflex.wp2.core.entities.*;
import org.goflex.wp2.fman.aggregatorportfolio.AggregatorPortfolio;
import org.goflex.wp2.fman.aggregatorportfolio.AggregatorPortfolioService;
import org.goflex.wp2.fman.common.exception.CustomException;
import org.goflex.wp2.fman.common.search.SearchCriteria;
import org.goflex.wp2.fman.common.search.SearchSpecification;
import org.goflex.wp2.fman.common.search.SearchSpecificationBuilder;
import org.goflex.wp2.fman.inea_market.IneaMarketCommitmentService;
import org.goflex.wp2.fman.measurements.MeasurementRepository;
import org.goflex.wp2.fman.measurements.MeasurementService;
import org.goflex.wp2.fman.security.SecurityService;
import org.goflex.wp2.fman.shell.DemoFlexOffers;
import org.goflex.wp2.fman.user.UserRepository;
import org.goflex.wp2.fman.user.UserService;
import org.goflex.wp2.fman.user.UserT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/*
 * Created by Ivan Bizaca on 06/07/2017.
 */

@RestController
@RequestMapping("/flexoffer")
@PropertySource("classpath:fman.properties")
public class FlexOfferController {
    private static final Logger logger = LoggerFactory.getLogger(FlexOfferController.class);

//    @Autowired
//    private AggregatorFlexOfferExchanger aggregatorFlexOfferExchanger;

    @Autowired
    private FlexOfferService foService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private FlexOfferRepository foRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private MeasurementService measurementService;

    @Autowired
    private IneaMarketCommitmentService tradeCommService;

    @Autowired
    private AggregatorPortfolioService apService;

    @Autowired
    private MeasurementRepository measurementRepo;

    @Autowired
    private UserService userService;

    @Resource(name = "scheduleDetail")
    private ConcurrentHashMap<UUID, FlexOfferSchedule> scheduleDetail;


    @GetMapping(value = "/", produces = "application/json")
    @PreAuthorize("hasRole('ROLE_BROKER') || hasRole('ROLE_PROSUMER') || hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "${FlexOfferController.getFlexOfferDetails}")
    public FlexOfferDetails getFlexOfferDetails() {

        FlexOfferDetails foDetails = new FlexOfferDetails();

        // Generate timeseries data
        try {
            Date now = new Date();

            AggregatorPortfolio port = this.apService.getActivePortfolio();

            foDetails.setDefaultSchedule(port.getFoCollection().getEnergySeriesByType(TimeSeriesType.tstBaselineEnergy));
            foDetails.setActiveSchedule(port.getFoCollection().getEnergySeriesByType(TimeSeriesType.tstScheduledEnergy));
            foDetails.setOverallLowSchedule(port.getFoCollection().getEnergySeriesByType(TimeSeriesType.tstMinEnergy));
            foDetails.setOverallHighSchedule(port.getFoCollection().getEnergySeriesByType(TimeSeriesType.tstMaxEnergy));

            long startInterval = FlexOffer.toFlexOfferTime(now) - 3600 * 24 / FlexOffer.numSecondsPerInterval;
            TimeSeries aggregatedMeasurements = measurementService.getAggregatedMeasurementSeries(startInterval,
                    FlexOffer.toFlexOfferTime(now), false);

            // de-accumulate before returning to client gui
            Double cumEnergyUntil = this.measurementRepo.getCommulativeEnergyUntil(startInterval - 1);
            aggregatedMeasurements = aggregatedMeasurements.deAccumulate(cumEnergyUntil != null ? cumEnergyUntil : 0.0);

            foDetails.setAggregatedMeasurements(aggregatedMeasurements);
        } catch (Exception e) {
            logger.error("Error generating flexoffer details: ", e);
        }
        return foDetails;
    }

    @GetMapping(value = "/active", produces = "application/json")
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_BROKER') || hasRole('ROLE_PROSUMER')")
    @ApiOperation(value = "${FlexOfferController.getActiveFlexOffers}")
    public ResponseEntity<List<FlexOfferT>> getACtiveFlexOffers() {
        try {

            List<FlexOfferT> activeFOs = this.foService.getAllActiveFlexOffers();

            HttpHeaders headers = new HttpHeaders() {
                {
                    add("Access-Control-Expose-Headers", "Content-Range");
                    add("Content-Range", String.valueOf(activeFOs.size()));
                }
            };

            ResponseEntity<List<FlexOfferT>> result = new ResponseEntity<>(activeFOs, headers, HttpStatus.OK);
            return result;
        } catch (Exception e) {
            logger.error("Error getting active flexoffers: ", e);
            return null;
        }
    }


    @PostMapping(value = "/{userName}", produces = "application/json")
    @PreAuthorize("hasRole('ROLE_BROKER') || hasRole('ROLE_PROSUMER') || hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "${FlexOfferController.createFlexOffer}")
    public FlexOfferT createFlexOffer(@RequestBody FlexOffer flexOffer,
                                      @PathVariable(value = "userName") String userName,
                                      Authentication auth) {
        // FlexOffer can be posted by prosumer-users, or broker-users which post of prosumer user behalf (characterized by userId)

        // Check whether the user is posting of FO is allowed
        if (userName != auth.getName() && !securityService.isBrokerAdmin(auth)) {
            throw new CustomException("Only broker users and admins are allowed to post on behalf of other users.", HttpStatus.FORBIDDEN);

            // TODO: Here we can add some check, for if the broker is allowed to post on user behalf
        }


        // offeredById sent by FOA is of the form username@deviceId
        UserT user = this.userService.getUserByUserName(flexOffer.getOfferedById().split("@")[0]);
        if (user == null) {
            userName = auth.getName();
        } else {
            userName = user.getUserName();
        }

        // Override the offered by Id
        flexOffer.setOfferedById(userName);

        // Do actual FO saving
        return this.foService.createFlexOffer(flexOffer);
    }

    @PutMapping(value = "/forceUpdate/{userName}", produces = "application/json")
    @PreAuthorize("hasRole('ROLE_BROKER') || hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "${FlexOfferController.forceUpdateFlexOffer}")
    public ResponseEntity forceUpdateFlexOffer(@PathVariable(value = "userName") String userName,
                                               @RequestBody List<Object> updatedFos,
                                               Authentication auth) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            updatedFos.forEach(obj -> {
                LinkedHashMap<String, Object> hashMap = (LinkedHashMap<String, Object>) obj;
                UUID foId = UUID.fromString(hashMap.get("id").toString());
                String offeredById = hashMap.get("offeredById").toString();
                Optional<FlexOfferT> oldFo = this.foService.getFlexOffer(foId);
                if (!oldFo.isPresent()) {
                    logger.warn("FlexOffer {} to be updated not found.", foId);
                }
                try {
                    this.foService.updateFlexOfferUser(oldFo, offeredById);
                } catch (Exception ex) {
                    logger.warn(ex.getMessage());
                }
            });
            return new ResponseEntity(new ResponseMessage(HttpStatus.OK, "success", null), HttpStatus.OK);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            return new ResponseEntity(new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR, "error", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //    @RequestMapping(value = "deleteFlexOffer/{foaId}/{flexOfferId}", method = RequestMethod.DELETE)
    @PutMapping(value = "/{id}", produces = "application/json")
    @PreAuthorize("hasRole('ROLE_BROKER') || hasRole('ROLE_PROSUMER') || hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "${FlexOfferController.updateFlexOffer}")
    public FlexOfferT updateFlexOffer(@PathVariable(value = "flexOfferId") UUID flexOfferId,
                                      @RequestBody FlexOffer newFo,
                                      Authentication auth) {

        Optional<FlexOfferT> oldFo = this.foService.getFlexOffer(flexOfferId);

        if (!oldFo.isPresent()) {
            throw new CustomException("FlexOffer to be updated not found.", HttpStatus.NOT_FOUND);
        }
        if (!securityService.isBrokerAdmin(auth)) {
            UserT user = (UserT) auth.getPrincipal();

            if (oldFo.get().getUser().getUserId() != user.getUserId()) {
                throw new CustomException("User has no permission to change a FlexOffer of other user.", HttpStatus.FORBIDDEN);
            }
        }

        return this.foService.updateFlexOffer(oldFo.get().getFlexoffer(), newFo);
    }

    @DeleteMapping(value = "/{id}", produces = "application/json")
    @PreAuthorize("hasRole('ROLE_BROKER') || hasRole('ROLE_PROSUMER') || hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "${FlexOfferController.deleteFlexOffer}")
    public ResponseEntity<Object> deleteFlexOffer(@PathVariable(value = "flexOfferId") UUID flexOfferId,
                                                  Authentication auth) {

        Optional<FlexOfferT> oldFo = this.foService.getFlexOffer(flexOfferId);

        if (!oldFo.isPresent()) {
            throw new CustomException("FlexOffer to be deleted not found.", HttpStatus.NOT_FOUND);
        }

        if (!securityService.isBrokerAdmin(auth)) {
            UserT user = (UserT) auth.getPrincipal();

            if (oldFo.get().getUser().getUserId() != user.getUserId()) {
                throw new CustomException("User has no permission to change a FlexOffer of other user.", HttpStatus.FORBIDDEN);
            }
        }


        if (oldFo.get().getFlexoffer().getState() == FlexOfferState.Executed && !securityService.isAdmin(auth)) {
            throw new CustomException("Only admins can delete FlexOffers in the state \"Executed\"", HttpStatus.FORBIDDEN);
        }

        this.foService.deleteFlexOffer(oldFo.get().getFlexoffer());

        return new ResponseEntity<>(HttpStatus.OK);
    }


    /**
     * creates one consumptiona and production FO for testing
     *
     * @param auth
     * @return
     */

    @PostMapping(value = "/testFOs", produces = "application/json")
    @PreAuthorize("hasRole('ROLE_BROKER') || hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "${FlexOfferController.generateTestFOs}")
    public List<FlexOfferT> generateTestFOs(Authentication auth) {
        List<GeoLocation> randomLocations = new ArrayList<>();
        randomLocations.add(new GeoLocation(9.903188733358325, 57.054119741877976));
        randomLocations.add(new GeoLocation(9.903612650305243, 57.05454297541864));
        randomLocations.add(new GeoLocation(9.903204624111144, 57.05475371511368));
        randomLocations.add(new GeoLocation(9.903021140930457, 57.0547126156572));
        randomLocations.add(new GeoLocation(9.903670092359729, 57.0539131274820));
        randomLocations.add(new GeoLocation(9.903187300795816, 57.053961109759));
        randomLocations.add(new GeoLocation(9.903287483786697, 57.05410344472106));
        randomLocations.add(new GeoLocation(9.903262348072897, 57.05484004065241));
        randomLocations.add(new GeoLocation(9.903684085415055, 57.05413793491759));
        randomLocations.add(new GeoLocation(9.903807578452405, 57.05396189783569));
        randomLocations.add(new GeoLocation(9.903513918955166, 57.054055182536175));
        randomLocations.add(new GeoLocation(9.903081414611888, 57.054559860682744));
        randomLocations.add(new GeoLocation(9.903079596161183, 57.054683681689696));
        randomLocations.add(new GeoLocation(9.903728846532374, 57.05480698608715));
        randomLocations.add(new GeoLocation(9.903176872040673, 57.05393445573363));
        randomLocations.add(new GeoLocation(9.902873720370248, 57.05462918663404));
        randomLocations.add(new GeoLocation(9.903256219818928, 57.05450207406304));
        randomLocations.add(new GeoLocation(9.903332078447951, 57.05435660300283));
        randomLocations.add(new GeoLocation(9.903099199822009, 57.053922658503666));
        randomLocations.add(new GeoLocation(9.903479415228379, 57.054415471353614));
        randomLocations.add(new GeoLocation(9.903746460918628, 57.05437072070439));
        randomLocations.add(new GeoLocation(9.903387432169579, 57.05471053707927));
        randomLocations.add(new GeoLocation(9.903692344550905, 57.053965442695905));
        randomLocations.add(new GeoLocation(9.903098969694893, 57.054177428737994));
        randomLocations.add(new GeoLocation(9.903287285673413, 57.054376460594604));
        randomLocations.add(new GeoLocation(9.990595285673413, 57.012293123123123));
        randomLocations.add(new GeoLocation(9.990595287285623, 57.012293322321122));

        Random random = new Random();

        DemoFlexOffers d = new DemoFlexOffers();
        List<FlexOfferT> fos = new ArrayList<>();
        for (FlexOffer f : d.demo(1)) {
            f.setOfferedById(auth.getName());
            Map<String, GeoLocation> location = new HashMap<>();
            location.put("userLocation", randomLocations.get(random.nextInt(randomLocations.size())));
            f.setLocationId(location);

            fos.add(this.foService.createFlexOffer(f));
        }

        return fos;
    }


    @GetMapping(value = "/history", produces = "application/json")
    @PreAuthorize("hasRole('ROLE_BROKER') || hasRole('ROLE_PROSUMER') || hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "${FlexOfferController.getFlexOfferHistory}")
    public ResponseEntity<FlexOfferDetails> getFlexOfferHistory(
            @PageableDefault(size = 10, sort = "creationTime", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "user", required = false) String username) throws Exception {

        try {

            Specification<FlexOfferT> spec = null;

            if (username != null) {
                UserT user = userRepo.findByUserName(username);
                spec = new SearchSpecification<>(new SearchCriteria("user", ":", user));
            }

            if (search != null) {
                SearchSpecificationBuilder<FlexOfferT> builder = new SearchSpecificationBuilder<>(FlexOfferT.class, search);

                if (spec == null) {
                    spec = builder.build();
                } else {
                    spec = spec.and(builder.build());
                }
            }

            Page<FlexOfferT> page = foRepo.findAll(spec, pageable);

            /* Generate aggregated data for the currently filtered flexoffers */
            FlexOfferCollection foCollection = new FlexOfferCollection(foRepo.findAll(spec)
                    .stream()
                    .map(t -> t.getFlexoffer())
                    .collect(Collectors.toList()));

            FlexOfferDetails foDetails = new FlexOfferDetails();
            foDetails.setDefaultSchedule(foCollection.getEnergySeriesByType(TimeSeriesType.tstBaselineEnergy));
            foDetails.setActiveSchedule(foCollection.getEnergySeriesByType(TimeSeriesType.tstScheduledEnergy));
            foDetails.setOverallLowSchedule(foCollection.getEnergySeriesByType(TimeSeriesType.tstMinEnergy));
            foDetails.setOverallHighSchedule(foCollection.getEnergySeriesByType(TimeSeriesType.tstMaxEnergy));

            TimeSeries aggregatedMeasurements = measurementService.getAggregatedMeasurementSeries(foCollection.getMinTid(),
                    foCollection.getMaxTid(), false);

            // de-accumulate before returning to client gui

            Double cumEnergyUntil = this.measurementRepo.getCommulativeEnergyUntil(foCollection.getMinTid() - 1);
            aggregatedMeasurements = aggregatedMeasurements.deAccumulate(cumEnergyUntil != null ? cumEnergyUntil : 0.0);

            foDetails.setAggregatedMeasurements(aggregatedMeasurements);

            foDetails.setMarketCommitments(this.tradeCommService.getCommitmentEnergy(foCollection.getMinTid(),
                    foCollection.getMaxTid()));
            foDetails.setFlexOfferTList(new ArrayList<>(page.getContent()));

            HttpHeaders headers = new HttpHeaders() {
                {
                    add("Access-Control-Expose-Headers", "Content-Range");
                    add("Content-Range", String.valueOf(page.getTotalElements()));
                }
            };
            //ResponseEntity<List<FlexOfferT>> result = new ResponseEntity<>(foTs, headers, HttpStatus.OK);
            ResponseEntity<FlexOfferDetails> result = new ResponseEntity<>(foDetails, headers, HttpStatus.OK);
            return result;
        } catch (Exception e) {
            logger.error("Error generating flexoffer details: ", e);
            return null;
        }
    }

    @PostMapping(value = "/schedules", produces = "application/json")
    @PreAuthorize("hasRole('ROLE_BROKER') || hasRole('ROLE_PROSUMER') || hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "${FlexOfferController.getSchedule}")
    public ResponseEntity<Object> listenFoaHeartBeat(@RequestBody int operationState,
                                                     Authentication auth) {
        ConcurrentHashMap<UUID, FlexOfferSchedule> scheduleDetailCopy = new ConcurrentHashMap<>(scheduleDetail);
//        ScheduleWrapper scheduleWrapper = new ScheduleWrapper();
//        scheduleWrapper.setId(UUID.randomUUID());
//        scheduleWrapper.setFlexOfferSchedule(scheduleDetailCopy);
        scheduleDetail.keySet().removeAll(scheduleDetailCopy.keySet()); // remove elements of the copy only
        return new ResponseEntity<>(scheduleDetailCopy, HttpStatus.OK);
    }

//    @RequestMapping(value = "/{id}/schedule", method = RequestMethod.POST)
//    public ResponseEntity<?> requestFlexOfferSchedule(@RequestBody OperationInformation operationalInfo, @PathVariable(value = "foaID") String foaId){
//
//        ConcurrentHashMap<UUID, FlexOfferSchedule> tempHashMap = null;
//        if (foaId != null) {
//            /**if operational data is not null stored it in memory*/
//            if(operationalInfo != null) {
//                synchronized (foaOperationalInfoData) {
//                    foaOperationalInfoData.put(foaId, operationalInfo);
//                }
//            }
//            ScheduleWrapper scheduleWrapper = new ScheduleWrapper();
//            tempHashMap = fmanScheduleService.GetUndeliveredSchedules(foaId);
//            fmanScheduleService.UpdateScheduleStatus(true, tempHashMap, foaId);
//            scheduleWrapper.setFlexOfferSchedule(tempHashMap);
//            return new ResponseEntity<>(scheduleWrapper, HttpStatus.OK);
//        }
//        else {
//            return new ResponseEntity<>(tempHashMap,HttpStatus.BAD_REQUEST);
//        }
//    }

//    @RequestMapping(value = "/get/schedules/{foaID}", method = RequestMethod.GET)
//    public ResponseEntity<?> requestFlexOfferSchedule(@PathVariable(value = "foaID") String foaId){
//        return new ResponseEntity<>(fmanScheduleService.GetSchedules(foaId), HttpStatus.OK);
//    }
//
//    @RequestMapping(value = "/get/flexoffer/{foaId}", method = RequestMethod.GET)
//    public ResponseEntity<List<FlexOffer>> getFlexOffers(@PathVariable(value = "foaId") String foaId) {
//        if (foaId != null) {
//            List<FlexOffer> flexOffers = foaService.getFlexOfferbyFoaID(foaId);
//            if (flexOffers != null) {
//                return new ResponseEntity<>(flexOffers, HttpStatus.OK);
//            }
//            else {
//                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//            }
//        }
//        else {
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        }
//    }
//
//    @RequestMapping(value = "/get/flexoffer/{foaId}/{flexOfferId}", method = RequestMethod.GET)
//    public ResponseEntity<FlexOffer> getFlexOffer(@PathVariable(value = "foaId") String foaId, @PathVariable(value = "flexOfferId") UUID flexOfferId) {
//        if (foaId != null && flexOfferId != null) {
//            List<Object> params = new ArrayList <Object>();
//            params.add(foaId);
//            params.add(flexOfferId);
//            FlexOffer flexOffer = foaService.getFlexOfferbyFoaIDFoID(params);
//            if (flexOffer != null) {
//                return new ResponseEntity<>(flexOffer, HttpStatus.OK);
//            }
//            else {
//                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//            }
//        }
//        else {
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        }
//    }

//    @RequestMapping(value = "/saveContract", method = RequestMethod.POST)
//    public ResponseEntity<?> saveContract(@RequestBody UserContract contract) {
//        if (contract.getContractId() == null) {
//            contractService.saveContract(contract);
//            return new ResponseEntity<>(HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        }
//    }


}

