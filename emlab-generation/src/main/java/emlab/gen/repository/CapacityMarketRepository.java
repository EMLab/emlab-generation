/*******************************************************************************
 /*******************************************************************************
 * Copyright 2013 the original author or authors.
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

import emlab.gen.domain.agent.Regulator;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.ClearingPoint;
import emlab.gen.domain.market.capacity.CapacityClearingPoint;
import emlab.gen.domain.market.capacity.CapacityDispatchPlan;
import emlab.gen.domain.market.capacity.CapacityMarket;

/**
 * @author Kaveri
 * 
 */
@Repository
public interface CapacityMarketRepository extends GraphRepository<CapacityMarket> {

    @Query(value = "g.v(zone).in('ZONE').filter{it.__type__=='emlab.gen.domain.market.capacity.CapacityMarket'}", type = QueryType.Gremlin)
    public CapacityMarket findCapacityMarketForZone(@Param("zone") Zone zone);

    @Query(value = "g.idx('__types__')[[className:'emlab.gen.domain.market.capacity.CapacityDispatchPlan']].filter{it.time == tick}.sort{it.price}._()", type = QueryType.Gremlin)
    public Iterable<CapacityDispatchPlan> findAllSortedCapacityDispatchPlansByTime(@Param("tick") long time);

    @Query(value = "g.v(market).in('BIDDINGMARKET').propertyFilter('time', FilterPipe.Filter.EQUAL, time).propertyFilter('status', FilterPipe.Filter.GREATER_THAN, 2)", type = QueryType.Gremlin)
    public Iterable<CapacityDispatchPlan> findAllAcceptedCapacityDispatchPlansForTime(
            @Param("market") CapacityMarket capacityMarket, @Param("time") long time);

    @Query(value = "g.v(market).in('CAPACITY_MARKET').propertyFilter('time', FilterPipe.Filter.EQUAL, time)", type = QueryType.Gremlin)
    public CapacityClearingPoint findOneCapacityClearingPointForTimeAndMarket(@Param("time") long time,
            @Param("market") CapacityMarket capacityMarket);

    @Query(value = "g.v(market).in('CAPACITY_MARKET').propertyFilter('time', FilterPipe.Filter.EQUAL, time)", type = QueryType.Gremlin)
    public ClearingPoint findOneClearingPointForTimeAndCapacityMarket(@Param("time") long time,
            @Param("market") CapacityMarket capacityMarket);

    @Query(value = "g.idx('__types__')[[className:'emlab.gen.domain.market.capacity.CapacityClearingPoint']].filter{it.time == tick}", type = QueryType.Gremlin)
    public CapacityClearingPoint findOneCapacityClearingPointForTime(@Param("time") long time);

    @Query(value = "g.v(zone).in('OF_ZONE')", type = QueryType.Gremlin)
    public Regulator findRegulatorForZone(@Param("zone") Zone zone);

}