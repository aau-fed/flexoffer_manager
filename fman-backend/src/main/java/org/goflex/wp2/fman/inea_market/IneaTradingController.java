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


import io.swagger.annotations.ApiOperation;
import org.goflex.wp2.core.entities.FlexOffer;
import org.goflex.wp2.fman.aggregatorportfolio.AggregatorPortfolio;
import org.goflex.wp2.fman.aggregatorportfolio.AggregatorPortfolioService;
import org.goflex.wp2.fman.common.search.SearchSpecificationBuilder;
import org.goflex.wp2.fman.security.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/*
 * Created by Laurynas Siksnys
 */
@RestController
@PreAuthorize("hasRole('ROLE_ADMIN')")              // Only ADMINs can access this
@RequestMapping("/trading")
public class IneaTradingController {
    private static final Logger logger = LoggerFactory.getLogger(IneaTradingController.class);

    @Autowired
    private IneaTradingService tService;

    @Autowired
    private IneaMarketCommitmentService tcomService;

    @Autowired
    private IneaTransactionHistoryRepository tRepo;

    @GetMapping(value="/generateOffer", produces = "application/json")
    @ApiOperation(value = "${IneaTradingController.getCurrentOffer}")
    public FlexOffer getShowOffer() throws Exception {
        FlexOffer f = null;
        try {
            f = this.tService.computeTradingOffer();
        } catch(Exception ex) {
            logger.error("No trading bid (Flex-Offer) was generated. Trading is not possible. Error:" + ex.getMessage());
        }

        return f;
    }

    @GetMapping(value="/operationState", produces = "application/json")
    @ApiOperation(value = "${IneaTradingController.getOperationState}")
    public ResponseEntity<Integer> getOperationState() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return new ResponseEntity<>(this.tService.getOperationState(), headers, HttpStatus.OK);
    }


    @GetMapping(value="/config", produces = "application/json")
    @ApiOperation(value = "${IneaTradingController.getConfig}")
    public IneaTradingConfig getConfig() throws Exception {
        return this.tService.getConfig();
    }

    @PostMapping(value="/config", consumes = "application/json")
    @ApiOperation(value = "${IneaTradingController.setConfig}")
    public void setConfig(@RequestBody IneaTradingConfig config) throws Exception {
        this.tService.setConfig(config);
    }

    @GetMapping(value="/activeTransaction", produces = "application/json")
    @ApiOperation(value = "${IneaTradingController.getActiveTransaction}")
    public ResponseEntity<IneaTransactionHistoryT> getActiveTransaction() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        IneaTransactionHistoryT trans = this.tService.getActiveTransaction(new Date()).orElse(null);

        return new ResponseEntity<>(trans, headers, HttpStatus.OK);
    }

    @PostMapping(value="/sendUpdateBid")
    @ApiOperation(value = "${IneaTradingController.sendUpdateBid}")
    public void sendUpdateBid() throws Exception {
        this.tService.manuallySendBidIn();
    }

    @GetMapping(value="/history", produces = "application/json")
    @ApiOperation(value = "${MeasurementController.getTradingHistory}")
    public ResponseEntity<List<IneaTransactionHistoryT>> getTradingHistory(
            Authentication auth,
            @PageableDefault(size = 10, sort = "transactionTime", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(value = "search", required = false) String search) throws Exception {

        Specification<IneaTransactionHistoryT> spec = null;

        SearchSpecificationBuilder<IneaTransactionHistoryT> builder;

        if (search != null ) {
            builder = new SearchSpecificationBuilder<IneaTransactionHistoryT>(IneaTransactionHistoryT.class, search);
            spec = builder.build();
        }


        Page<IneaTransactionHistoryT> page = tRepo.findAll(spec, pageable);

        HttpHeaders headers = new HttpHeaders() {
            {
                add("Access-Control-Expose-Headers", "Content-Range");
                add("Content-Range", String.valueOf(page.getTotalElements()));
            }
        };


        ResponseEntity<List<IneaTransactionHistoryT>> result = new ResponseEntity<>(page.getContent().stream()
                .collect(Collectors.toList()), headers, HttpStatus.OK);

        return result;
    }

    @GetMapping(value="/commitments", produces = "application/json")
    @ApiOperation(value = "${MeasurementController.getTradingCommitments}")
    public ResponseEntity<List<IneaMarketCommitment>> getTradingCommitments(Authentication auth,
                                                                            @RequestParam(value = "dateFrom", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date dateFrom,
                                                                            @RequestParam(value = "dateTo", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date dateTo) throws Exception {
        long intervalFrom = FlexOffer.toFlexOfferTime(dateFrom);
        long intervalTo = FlexOffer.toFlexOfferTime(dateTo) + 1;

        List<IneaMarketCommitment> comm = this.tcomService.getCommitmentTransactions(intervalFrom, intervalTo);

        return ResponseEntity.ok(comm);
    }

}
