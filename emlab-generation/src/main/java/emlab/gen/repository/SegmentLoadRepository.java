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
import org.springframework.stereotype.Repository;

import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.market.electricity.SegmentLoad;

/**
 * Repository for segment loads
 * 
 * @author ejlchappin
 * 
 */
@Repository
public interface SegmentLoadRepository extends GraphRepository<SegmentLoad> {

    /**
     * Finds the segment loads for a certain segment.
     * 
     * @param segment
     *            the segment to find the load for
     * @return the segment load
     */
    @Query("start segment=node({segment}) match (segment)<-[:SEGMENTLOAD_SEGMENT]-(segmentload) return segmentload")
    public Iterable<SegmentLoad> findSegmentLoadBySegment(@Param("segment") Segment segment);

    /**
     * Finds the segment load for a certain segment and market
     * 
     * @param segment
     *            the segment to find the load for
     * @param market
     *            the market to find the load for
     * @return
     */
    @Query(value = "segID = g.v(segment).segmentID;"
            + "double baseLoad = g.v(market).out('SEGMENT_LOAD').as('x').out('SEGMENTLOAD_SEGMENT').filter{it.segmentID==segID}.back('x').baseLoad.next();"
            + "return baseLoad", type = QueryType.Gremlin)
    public double returnSegmentBaseLoadBySegmentAndMarket(@Param("segment") Segment segment, @Param("market") ElectricitySpotMarket market);

    // peak Load by Zone

    @Query(value = "g.v(zone).in('ZONE').filter{it.__type__=='emlab.gen.domain.market.electricity.ElectricitySpotMarket'}.outE('SEGMENT_LOAD').inV.max{it.baseLoad}.baseLoad", type = QueryType.Gremlin)
    double peakLoadbyZoneMarketandTime(@Param("zone") Zone zone, @Param("market") ElectricitySpotMarket market);

}
