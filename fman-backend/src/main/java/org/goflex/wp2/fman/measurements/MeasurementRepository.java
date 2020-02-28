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


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;


@Repository
public interface MeasurementRepository extends JpaRepository<MeasurementT, Long>, JpaSpecificationExecutor<MeasurementT> {
    interface TimeIntervalAndValue{
        Long getTimeInterval();
        Double getValue();
    }

    // 2019-03-29 Updated so the negative energy (production) can be handled
    @Query(value = "SELECT m.timeInterval AS timeInterval, m.cumulativeEnergy AS value FROM MeasurementT m "+
                   "WHERE m.userName = :userName AND m.timeInterval >= :timeIntervalStart and m.timeInterval< :timeIntervalEnd AND "+
                   "m.timeStamp = (SELECT MAX(timeStamp) FROM MeasurementT s WHERE m.userName = s.userName AND m.timeInterval = s.timeInterval) "+
                   "ORDER BY m.timeInterval ASC" )
    public List<TimeIntervalAndValue> aggregateForTimeIntervals(@Param("userName") String userName,
                                                              @Param("timeIntervalStart") long timeIntervalStart,
                                                              @Param("timeIntervalEnd") long timeIntervalEnd);

    @Query(value = "SELECT m.timeInterval AS timeInterval, SUM(m.cumulativeEnergy) AS value FROM MeasurementT m " +
            "WHERE m.timeInterval >= :timeIntervalStart and m.timeInterval< :timeIntervalEnd AND "+
            "m.timeStamp = (SELECT MAX(timeStamp) FROM MeasurementT s WHERE m.userName = s.userName AND m.timeInterval = s.timeInterval) "+
            "GROUP BY m.timeInterval "+
            "ORDER BY m.timeInterval ASC" )
    public List<TimeIntervalAndValue> aggregateForTimeIntervals(
                                                              @Param("timeIntervalStart") long timeIntervalStart,
                                                              @Param("timeIntervalEnd") long timeIntervalEnd);

    // To compute current energy, we compute the difference between two adjecent commulativeEnergy values, sum all that
    @Query(value =
            //"SELECT sum((ce1 - ce2) * 3600 / GREATEST(TIMESTAMPDIFF(SECOND, t1, t2), 1) ) "+
            "SELECT sum((ce1 - ce2) * 3600 / 60 ) "+ // assuming all data points are one minute apart
            "FROM ( "+
            "SELECT (SELECT cumulative_energy as ce1 "+
            "        FROM measurement s "+
            "        WHERE s.user_name=m.user_name AND s.time_stamp >= :lookbackTimestamp AND s.time_stamp <= :currentTimestamp "+
            "        ORDER BY s.time_stamp DESC LIMIT 1) as ce1, "+

            "(SELECT cumulative_energy "+
            "FROM measurement s "+
            "WHERE s.user_name=m.user_name AND s.time_stamp >= :lookbackTimestamp AND s.time_stamp <= :currentTimestamp "+
            "ORDER BY s.time_stamp DESC LIMIT 1 OFFSET 1) as ce2, "+

            "(SELECT time_stamp "+
            " FROM measurement s "+
            " WHERE s.user_name=m.user_name AND s.time_stamp >= :lookbackTimestamp AND s.time_stamp <= :currentTimestamp "+
            " ORDER BY s.time_stamp DESC LIMIT 1) as t2,  "+

            "(SELECT time_stamp "+
             "FROM measurement s "+
             "WHERE s.user_name=m.user_name AND s.time_stamp >= :lookbackTimestamp AND s.time_stamp <= :currentTimestamp "+
             "ORDER BY s.time_stamp DESC LIMIT 1 OFFSET 1) as t1 "+
             "FROM measurement m "+
             "GROUP BY m.user_name) AS q ", nativeQuery = true)
    public Double aggregateCurrentPower(@Param("currentTimestamp") Date currentTimestamp, @Param("lookbackTimestamp") Date lookbackTimestamp) ;

    @Query(value = "SELECT MAX(m.cumulativeEnergy) FROM MeasurementT m " +
            "WHERE m.timeInterval <= :timeInterval")
    public Double getCommulativeEnergyUntil(@Param("timeInterval") long timeInterval);

    @Query(value = "SELECT MAX(m.cumulativeEnergy) FROM MeasurementT m " +
            "WHERE m.userName = :userName AND m.timeInterval <= :timeInterval")
    public Double getCommulativeEnergyUntil(@Param("userName") String userName, @Param("timeInterval") long timeInterval);
}