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

import emlab.gen.domain.market.ClearingPoint;
import emlab.gen.domain.market.DecarbonizationMarket;
import emlab.gen.domain.technology.Substance;

public interface ClearingPointRepository extends GraphRepository<ClearingPoint> {

    @Query(value = "all = g.v(market).in('MARKET_POINT').filter{it.time==tick && it.forecast==forecast}; if(all.hasNext()){return all.next();} else{return []}", type = QueryType.Gremlin)
    ClearingPoint findClearingPointForMarketAndTime(@Param("market") DecarbonizationMarket market,
            @Param("tick") long tick, @Param("forecast") boolean forecast);

    // @Query("start market=node({market}) match (market)<-[:MARKET_POINT]-(point) where (point.time >= {timeFrom}) and (point.time <= {timeTo}) return avg(point.price)")
    // double
    // calculateAverageClearingPriceForMarketAndTimeRange(@Param("market")
    // DecarbonizationMarket market,
    // @Param("timeFrom") long timeFrom, @Param("timeTo") long timeTo);

    @Query(value = "g.v(market).in('MARKET_POINT').filter{(it.time>=timeFrom) && (it.time<=timeTo) && it.forecast==forecast}.price.mean()", type = QueryType.Gremlin)
    double calculateAverageClearingPriceForMarketAndTimeRange(@Param("market") DecarbonizationMarket market,
            @Param("timeFrom") long timeFrom, @Param("timeTo") long timeTo, @Param("forecast") boolean forecast);

    @Query(value = "g.v(substance).in('SUBSTANCE_MARKET').in('MARKET_POINT').propertyFilter('time', FilterPipe.Filter.GREATER_THAN_EQUAL, timeFrom).propertyFilter('time', FilterPipe.Filter.LESS_THAN_EQUAL, timeTo).propertyFilter('forecast',FilterPipe.Filter.EQUAL,forecast)", type = QueryType.Gremlin)
    Iterable<ClearingPoint> findAllClearingPointsForSubstanceAndTimeRange(@Param("substance") Substance substance,
            @Param("timeFrom") long timeFrom, @Param("timeTo") long timeTo, @Param("forecast") boolean forecast);

    @Query(value = "g.v(market).in('MARKET_POINT').propertyFilter('time', FilterPipe.Filter.GREATER_THAN_EQUAL, timeFrom).propertyFilter('time', FilterPipe.Filter.LESS_THAN_EQUAL, timeTo).propertyFilter('forecast',FilterPipe.Filter.EQUAL,forecast)", type = QueryType.Gremlin)
    Iterable<ClearingPoint> findAllClearingPointsForMarketAndTimeRange(@Param("market") DecarbonizationMarket market,
            @Param("timeFrom") long timeFrom, @Param("timeTo") long timeTo, @Param("forecast") boolean forecast);


    @Query(value = "g.v(substance).in('SUBSTANCE_MARKET').propertyFilter('__type__', FilterPipe.Filter.EQUAL, 'emlab.gen.domain.market.CommodityMarket').in('MARKET_POINT').propertyFilter('time', FilterPipe.Filter.GREATER_THAN_EQUAL, timeFrom).propertyFilter('time', FilterPipe.Filter.LESS_THAN_EQUAL, timeTo).propertyFilter('forecast',FilterPipe.Filter.EQUAL,forecast)", type = QueryType.Gremlin)
    Iterable<ClearingPoint> findAllClearingPointsForSubstanceTradedOnCommodityMarkesAndTimeRange(@Param("substance") Substance substance,
            @Param("timeFrom") long timeFrom, @Param("timeTo") long timeTo,
            @Param("forecast") boolean forecast);

}
