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

import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.Segment;

@Repository
public interface SegmentRepository extends GraphRepository<Segment> {

    @Query(value = "g.v(market).out('SEGMENT_LOAD').out.filter{it.segmentID==1}.next()", type = QueryType.Gremlin)
    public Segment findPeakSegmentforMarket(@Param("market") ElectricitySpotMarket market);

    @Query(value = "g.v(market).out('SEGMENT_LOAD').out.sort{it.segmentID}_()", type = QueryType.Gremlin)
    public Iterable<Segment> findSegmentforMarketSortedbySegmentID(@Param("market") ElectricitySpotMarket market);
}
