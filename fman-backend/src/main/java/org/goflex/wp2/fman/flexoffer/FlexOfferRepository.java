/*
 * Created by bijay
 * GOFLEX :: WP2 :: foa-core
 * Copyright (c) 2018 The GoFlex Consortium
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining  a copy of this software and associated documentation
 *  files (the "Software") to deal in the Software without restriction,
 *  including without limitation the rights to use, copy, modify, merge,
 *  publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions: The above copyright notice and
 *  this permission notice shall be included in all copies or substantial
 *  portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON
 *  INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 *
 *  Last Modified 3/23/18 5:54 PM
 */

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


import org.goflex.wp2.core.entities.FlexOffer;
import org.goflex.wp2.core.entities.FlexOfferState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Repository
@Transactional
public interface FlexOfferRepository extends JpaRepository<FlexOfferT, String>, JpaSpecificationExecutor<FlexOfferT> {

    List<FlexOfferT> findAll();

    @Query("SELECT f.flexoffer FROM FlexOfferT f " +
           "WHERE f.status = :status")
    List<FlexOffer> findByStatus(@Param("status") FlexOfferState status);

    @Query("SELECT f FROM FlexOfferT f " +
           "WHERE f.status = org.goflex.wp2.core.entities.FlexOfferState.Assigned OR " +
            "     f.status = org.goflex.wp2.core.entities.FlexOfferState.Accepted")
    List<FlexOfferT> findAllActive();




    @Query(value = "SELECT f.flexoffer FROM FlexOfferT f " +
            "inner join f.user u " +
            "WHERE u.userId = :userId and f.creationTime >= :startDate and f.creationTime < :endDate ")
    List<FlexOffer> findByUserIDAndMonth(@Param("userId") long userId, @Param("startDate") Date startDate,
                                         @Param("endDate") Date endDate);


    @Query(value = "SELECT f FROM FlexOfferT f " +
                   "WHERE f.creationTime >= :startDate and f.creationTime < :endDate ")
    List<FlexOfferT> findAllCreatedWithin(@Param("startDate") Date startDate,
                                          @Param("endDate") Date endDate);

    @Query(value = "SELECT f FROM FlexOfferT f " +
                   "WHERE f.user.userId = :userId and f.creationTime >= :startDate and f.creationTime < :endDate ")
    List<FlexOfferT> findByUserCreatedWithin(@Param("userId") long userId,
                                             @Param("startDate") Date startDate,
                                             @Param("endDate") Date endDate);


}
