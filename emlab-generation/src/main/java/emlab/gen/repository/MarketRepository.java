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

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.annotation.QueryType;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import emlab.gen.domain.agent.NationalGovernment;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.CO2Auction;
import emlab.gen.domain.market.DecarbonizationMarket;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.market.electricity.SegmentLoad;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.domain.technology.Substance;

/**
 * The repository for markets.
 * 
 * @author ejlchappin
 * @author jcrichstein
 * 
 */
@Repository
public interface MarketRepository extends GraphRepository<DecarbonizationMarket> {

    /**
     * Finds all ElectricitySpotMarkets
     * 
     * @return
     */
    @Query(value = "start market=node:__types__(\"className:emlab.gen.domain.market.electricity.ElectricitySpotMarket\") return market")
    public Iterable<ElectricitySpotMarket> findAllElectricitySpotMarkets();

    @Query(value = "g.idx('__types__')[[className:'emlab.gen.domain.market.electricity.ElectricitySpotMarket']].toList()", type = QueryType.Gremlin)
    public List<ElectricitySpotMarket> findAllElectricitySpotMarketsAsList();

    @Query(value = "start market=node:__types__(\"className:emlab.gen.domain.market.electricity.ElectricitySpotMarket\") return count(market)")
    double countAllElectricitySpotMarkets();

    /**
     * Finds the CO2Auction
     * 
     * @return
     */
    @Query(value = "g.idx('__types__')[[className:'emlab.gen.domain.market.CO2Auction']].next()", type = QueryType.Gremlin)
    CO2Auction findCO2Auction();

    /**
     * Gives the electricity spot market for a specific zone
     * 
     * @param zone
     *            the electricity market should be found for
     * @return the found electricity spot market
     */
    @Query(value = "g.v(zone).in('ZONE').filter{it.__type__=='emlab.gen.domain.market.electricity.ElectricitySpotMarket'}.next()", type = QueryType.Gremlin)
    public ElectricitySpotMarket findElectricitySpotMarketForZone(@Param("zone") Zone zone);

    @Query(value = "START nationalG = node({nationalG}), electricityMarket = node:__types__(\"className:emlab.gen.domain.market.electricity.ElectricitySpotMarket\") MATCH (nationalG)-[:GOVERNED_ZONE]->(zone)<-[:ZONE]-(electricityMarket) RETURN electricityMarket")
    public ElectricitySpotMarket findElectricitySpotMarketByNationalGovernment(@Param("nationalG") NationalGovernment nationalG);

    @Query(value = "g.v(plant).out('LOCATION').out('REGION').in('ZONE').filter{it.__type__=='emlab.gen.domain.market.electricity.ElectricitySpotMarket'}.next()", type = QueryType.Gremlin)
    public ElectricitySpotMarket findElectricitySpotMarketByPowerPlant(@Param("plant") PowerPlant plant);

    @Query(value = "segID = g.v(segment).segmentID;"
            + "return g.v(zone).in('ZONE').filter{it.__type__=='emlab.gen.domain.market.electricity.ElectricitySpotMarket'}.out('SEGMENT_LOAD').as('SL').out('SEGMENTLOAD_SEGMENT').filter{it.segmentID==segID}.back('SL').next();", type = QueryType.Gremlin)
    public SegmentLoad findSegmentLoadForElectricitySpotMarketForZone(@Param("zone") Zone zone, @Param("segment") Segment segment);

    /**
     * Gives the market for a specific substance
     * 
     * @param substance
     *            the substance the market should be found for
     * @return the found market
     */
    @Query(value = "g.v(substance).in('SUBSTANCE_MARKET').next()", type = QueryType.Gremlin)
    public DecarbonizationMarket findFirstMarketBySubstance(@Param("substance") Substance substance);

}
