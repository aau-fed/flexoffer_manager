package org.goflex.wp2.fman.inea_market;

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


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.goflex.wp2.core.entities.*;
import org.goflex.wp2.core.wrappers.IneaHeartBeatMessage;
import org.goflex.wp2.core.wrappers.IneaHeatBeatRequestWrapper;
import org.goflex.wp2.core.wrappers.OperationInformation;
import org.goflex.wp2.fman.aggregatorportfolio.AggregatorPortfolio;
import org.goflex.wp2.fman.aggregatorportfolio.AggregatorPortfolioService;
import org.goflex.wp2.fman.inea_market.entities.*;
import org.goflex.wp2.fman.inea_market.events.IneaMarketEvent;
import org.goflex.wp2.fman.measurements.MeasurementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * This is the main entry point to INEA's market
 */
@Service
public class IneaMarketAPIService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ApplicationEventPublisher publisher;

    @Value("${fman.inea.url}")
    private String INEAUrl;

    private String INEAFOPushUrl;

    @Value("${fman.inea.offeredById}")
    private int INEA_OFFERERBY_ID;

    @Value("${fman.inea.operationDataArray.length}")
    private int ineaOperationDataLength;
    public int getIneaOperationDataLength() {
        return this.ineaOperationDataLength;
    }


    // private DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private IneaFlexOfferRequestWrapper generateINEAFO(FlexOffer f, long ineaId) {

        IneaFlexOfferWrapper[] tempFO = new IneaFlexOfferWrapper[1];
        tempFO[0] = new IneaFlexOfferWrapper();
        tempFO[0].setId(ineaId);
        tempFO[0].setState(f.getState());
        tempFO[0].setOfferedById(this.INEA_OFFERERBY_ID);//53490, 165731
        tempFO[0].setNumSecondsPerInterval(900);
        tempFO[0].setCreationTime(f.getCreationTime());
        tempFO[0].setStartAfterTime(f.getStartAfterTime());
        tempFO[0].setStartBeforeTime(f.getStartAfterTime());
        tempFO[0].setAssignment(Assignment.obligatory);
        tempFO[0].setAssignmentBeforeTime(f.getAssignmentBeforeTime());
        // tempFO[0].setFlexOfferType(FlexOfferType.imbalance);  // 2018-09-21 Gregor said this was not needed.

        FlexOfferSliceInea[] flexOfferProfileConstraints = new FlexOfferSliceInea[f.getFlexOfferProfileConstraints().length];
        for (int i = 0; i < flexOfferProfileConstraints.length; i++) {
            flexOfferProfileConstraints[i] = new FlexOfferSliceInea();
            FlexOfferConstraintInea[] energyConstraintList = new FlexOfferConstraintInea[1];

            for (int j = 0; j < energyConstraintList.length; j++) {
                energyConstraintList[j] = new FlexOfferConstraintInea();

                // Note, on the INEA side, positive values are production, unlike here
                energyConstraintList[j].setUpperBound(-1.0 * f.getFlexOfferProfileConstraint(i).getEnergyConstraint(j).getLower());
                energyConstraintList[j].setLowerBound(-1.0 * f.getFlexOfferProfileConstraint(i).getEnergyConstraint(j).getUpper());
            }
            flexOfferProfileConstraints[i].setEnergyConstraintList(energyConstraintList);
            flexOfferProfileConstraints[i].setTariffConstraint(f.getFlexOfferProfileConstraint(i).getTariffConstraint());

        }
        tempFO[0].setFlexOfferProfileConstraints(flexOfferProfileConstraints);

        // Generate the default schedule
        if (f.getDefaultSchedule() != null) {
            FlexOfferScheduleInea defSch = new FlexOfferScheduleInea(f.getDefaultSchedule().getStartTime(), Arrays.stream(f.getDefaultSchedule().getScheduleSlices())
                                                                                                            .map(s -> new FlexOfferScheduleSliceInea(-1.0 * s.getEnergyAmount()))
                                                                                                            .toArray(FlexOfferScheduleSliceInea[]::new));
            tempFO[0].setDefaultSchedule(defSch);
        }

        // 2018-09-21 Gregor: this is not needed
//        if (f.getFlexOfferTariffConstraint() != null) {
//            TariffConstraintProfileInea tariffConstraintProfile = new TariffConstraintProfileInea();
//            tariffConstraintProfile.setStartTime(f.getFlexOfferTariffConstraint().getStartTime());
//            TariffSlice[] tariffConstraintSlices = new TariffSlice[f.getFlexOfferTariffConstraint().getTariffSlices().length];
//            for (int i = 0; i < tariffConstraintSlices.length; i++) {
//                tariffConstraintSlices[i] = new TariffSlice();
//                tariffConstraintSlices[i] = new TariffSlice(1, f.getFlexOfferTariffConstraint().getTariffSlice(i).getTariffLower(), f.getFlexOfferTariffConstraint().getTariffSlice(i).getTariffUpper());
//            }
//            tariffConstraintProfile.setTariffSlices(tariffConstraintSlices);
//
//            tempFO[0].setFlexOfferTariffConstraint(tariffConstraintProfile);
//        }

        IneaFlexOfferRequestWrapper fos = new IneaFlexOfferRequestWrapper();
        fos.setFlexOffer(tempFO);
        return fos;

    }

    private int _sendFOtoINEA(FlexOffer f, long ineaId) {
        logger.info("Sending Flex-offer to INEA FMAR");

        HttpHeaders httpHeaders = new HttpHeaders();
        HttpEntity<IneaFlexOfferRequestWrapper> entity = new HttpEntity<>(this.generateINEAFO(f, ineaId), httpHeaders);


        /**Option 1 */
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response1 = restTemplate.exchange(this.INEAUrl, HttpMethod.POST, entity, String.class);
        logger.info(response1.getBody());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseJsn = null;
        try {
            responseJsn = mapper.readTree(response1.getBody().replace("[", "").replace("]", ""));
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }

        return responseJsn.get("flexOffer").get("id").asInt();
    }

    public Optional<Long> sendNewFlexOffer(FlexOffer f) throws Exception {
        return Optional.of((long) this._sendFOtoINEA(f, -1));
    }

    public Optional<Long> updateExistingFlexOffer(long ineaId, FlexOffer f) throws Exception {
        return Optional.of((long) this._sendFOtoINEA(f, ineaId));
    }

    public boolean isINEAresponseOK(Optional<Long> ineaId) {
        return ineaId.isPresent() && ineaId.get() >= 0;
    }

    public String decodeINEAresponse(Optional<Long> ineaId) {
        if (ineaId.isPresent()) {
            long id = ineaId.get();

            if (id >= 0)
                return "Flex-Offer Accepted";
            if (id == -1)
                return "Flex-Offer Rejected: Flex-Offer with such an Id does not exist";
            if (id == -2)
                return "Flex-Offer Rejected: Prosumer with such an Id does not exist or has trading rights granted";
            if (id == -3)
                return "Flex-Offer Rejected: Flex-Offer is under execution.";
            if (id < -3)
                return "Flex-Offer Rejected: Unknown reason";
        }

        return "No response from INEA market";
    }

    public void sendHeartBeat(OperationInformation opInfo) {

        if (opInfo != null) {
            IneaHeatBeatRequestWrapper heartBeatMessage = new IneaHeatBeatRequestWrapper();
            heartBeatMessage.setHeartbeat(new IneaHeartBeatMessage());

            Date foaTime = new Date();
            heartBeatMessage.getHeartbeat().setFoaTime(foaTime);
            heartBeatMessage.getHeartbeat().setOfferedById(String.valueOf(INEA_OFFERERBY_ID));
            heartBeatMessage.getHeartbeat().setOperationInfo(opInfo);


            logger.info("Sending Heart Beats to INEA");
            logger.info(opInfo.toString());
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders httpHeaders = new HttpHeaders();
            HttpEntity<IneaHeatBeatRequestWrapper> entity = new HttpEntity<>(heartBeatMessage, httpHeaders);


            ResponseEntity<String> response = restTemplate.exchange(this.INEAUrl, HttpMethod.POST, entity, String.class);
            //logger.info(response.toString());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode responseJsn = null;
            try {
                if (response.getBody() != null) {
                    responseJsn = mapper.readTree(response.getBody());

                    // Process schedules, if any
                    if (responseJsn.size() != 0) {
                        for (JsonNode node : responseJsn) {

                            if (!node.hasNonNull("id") || !node.hasNonNull("flexOfferSchedule") || node.get("flexOfferSchedule").asText().equalsIgnoreCase("null")) {
                                logger.error("INEA has returned an unexpected response");
                                continue;
                            }

                            int ineaFoID = node.get("id").asInt();
                            int scheduleID = node.get("flexOfferSchedule").get("scheduleId").asInt();
                            String startTime = node.get("flexOfferSchedule").get("startTime").asText();
                            int duration = 0;
                            double energyAmount = 0;
                            double tariff = 0;

                            int noOfSlices = node.get("flexOfferSchedule").get("scheduleSlices").size();

                            FlexOfferScheduleSlice[] flexOfferScheduleSlice = new FlexOfferScheduleSlice[noOfSlices];
                            for (int i = 0; i < noOfSlices; i++) {
                                JsonNode node1 = node.get("flexOfferSchedule").get("scheduleSlices").get(i);
                                //System.out.print(node1.get("duration").asInt());

                                flexOfferScheduleSlice[i] = new FlexOfferScheduleSlice();
                                flexOfferScheduleSlice[i].setDuration(node1.get("duration").asInt());

                                flexOfferScheduleSlice[i].setEnergyAmount(-1.0 * node1.get("energyAmount").asDouble());

                                flexOfferScheduleSlice[i].setTariff(node1.get("tariff").asDouble());
                            }

                            FlexOfferSchedule flexOfferSchedule = new FlexOfferSchedule();
                            flexOfferSchedule.setScheduleId(scheduleID);
                            flexOfferSchedule.setScheduleSlices(flexOfferScheduleSlice);

                            flexOfferSchedule.setStartTime(javax.xml.bind.DatatypeConverter.parseDateTime(startTime).getTime());

                            logger.info("New schedule received from Inea");
                            logger.info(flexOfferSchedule.toString());

                            // Emit the schedule
                            IneaMarketEvent evt = new IneaMarketEvent(this);
                            evt.setIneaId(ineaFoID);
                            evt.setType(IneaMarketEvent.IneaEventType.evtScheduleAvailable);
                            evt.setSchedule(flexOfferSchedule);
                            this.publisher.publishEvent(evt);
                        }
                    } else {
                        logger.debug("Response from Inea has no schedule data");
                    }
                } else {
                    logger.warn("Empty response body received from Inea");
                }
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        }
    }

}
