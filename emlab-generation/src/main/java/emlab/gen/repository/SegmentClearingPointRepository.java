/*******************************************************************************
 * Copyright 2012 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package emlab.gen.repository;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.annotation.QueryType;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.market.electricity.SegmentClearingPoint;

public interface SegmentClearingPointRepository extends GraphRepository<SegmentClearingPoint> {

    @Query("START scp=node:__types__(\"className:emlab.gen.domain.market.electricity.SegmentClearingPoint\") WHERE (scp.time={time} AND scp.forecast={forecast}) RETURN scp")
    Iterable<SegmentClearingPoint> findAllSegmentClearingPointsForTime(@Param("time") long time,
            @Param("forecast") boolean forecast);

    @Query("START segment = node({segment}), scp=node:__types__(\"className:emlab.gen.domain.market.electricity.SegmentClearingPoint\") MATCH (segment)<-[:SEGMENT_POINT]-(scp) WHERE (scp.time = {time} AND scp.forecast={forecast}) RETURN scp")
    Iterable<SegmentClearingPoint> findAllSegmentClearingPointsForSegmentAndTime(@Param("time") long time,
            @Param("segment") Segment segment, @Param("forecast") boolean forecast);

    // @Query("START segment = node({segment}), market=node({market}) MATCH (segment)<-[:SEGMENT_POINT]-(scp)-[:MARKET_POINT]->(market) WHERE (scp.time = {time}) RETURN scp")
    // SegmentClearingPoint findOneSegmentClearingPointForMarketSegmentAndTime(@Param("time") long time, @Param("segment") Segment segment,
    // @Param("market") ElectricitySpotMarket electricitySpotMarket);

    @Query(value = "g.v(segment).in('SEGMENT_POINT').propertyFilter('time', FilterPipe.Filter.EQUAL, time).propertyFilter('forecast', FilterPipe.Filter.EQUAL, forecast).as('x').out('MARKET_POINT').idFilter(market, FilterPipe.Filter.EQUAL).back('x')", type = QueryType.Gremlin)
    SegmentClearingPoint findOneSegmentClearingPointForMarketSegmentAndTime(@Param("time") long time, @Param("segment") Segment segment,
            @Param("market") ElectricitySpotMarket electricitySpotMarket,
            @Param("forecast") boolean forecast);
}
