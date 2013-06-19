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

import emlab.gen.domain.market.Bid;
import emlab.gen.domain.market.DecarbonizationMarket;

/**
 * Repository for bids (partly adapted from Alfredas PGC model)
 * 
 * @author jcrichstein
 * @author alfredas
 * 
 */
@Repository
public interface BidRepository extends GraphRepository<Bid> {

	/**
	 * Finds all demand bids for a market for a time
	 * 
	 * @param market
	 * @param time
	 * @return the found bids
	 */
	// @Query(value =
	// "g.v(market).in('BIDDINGMARKET').filter{it.time == time}.filter{it.supplyBid == false}",
	// type = QueryType.Gremlin)
	// public Iterable<Bid> findDemandBidsForMarketForTime(@Param("market")
	// DecarbonizationMarket market, @Param("time") long time);

	@Query("START bid=node:__types__(\"className:emlab.gen.domain.market.Bid\") WHERE (bid.time={time}) RETURN bid")
	Iterable<Bid> findAllBidsForForTime(@Param("time") long time);

	@Query("START market=node({market}) MATCH (market)<-[:BIDDINGMARKET]-(bid) WHERE (bid.time = {time}) and (bid.supplyBid=false) RETURN bid ORDER BY bid.price desc")
	Iterable<Bid> findDemandBidsForMarketForTime(@Param("market") DecarbonizationMarket market, @Param("time") long time);

	@Query("START market=node({market}) MATCH (market)<-[:BIDDINGMARKET]-(bid) WHERE (bid.time = {time}) and (bid.supplyBid=false) and (bid.status>=2) RETURN bid")
	Iterable<Bid> findAllAcceptedDemandBidsForMarketForTime(@Param("market") DecarbonizationMarket market,
			@Param("time") long time);

	/**
	 * Finds all supply bids for a market for a time. Cypher adapted from PGC
	 * (Alfredas)
	 * 
	 * @param market
	 * @param time
	 * @return
	 */
	// @Query(value =
	// "g.v(market).in('BIDDINGMARKET').filter{it.time == time}.filter{it.supplyBid == true}",
	// type = QueryType.Gremlin)
	// public Iterable<Bid> findOffersForMarketForTime(@Param("market")
	// DecarbonizationMarket market, @Param("time") long time);

	@Query("START market=node({market}) MATCH (market)<-[:BIDDINGMARKET]-(bid) WHERE (bid.time = {time}) and (bid.supplyBid=true) RETURN bid ORDER BY bid.price")
	Iterable<Bid> findOffersForMarketForTime(@Param("market") DecarbonizationMarket market, @Param("time") long time);

	@Query("START market=node({market}) MATCH (market)<-[:BIDDINGMARKET]-(bid) WHERE (bid.time = {time}) and (bid.supplyBid=true) and (bid.status>=2) RETURN bid")
	Iterable<Bid> findAllAcceptedOffersForMarketForTime(@Param("market") DecarbonizationMarket market,
			@Param("time") long time);

	/**
	 * Find bids for a market for a time
	 * 
	 * @param market
	 * @param time
	 * @param isSupply
	 *            supply or demand bids
	 * @return the bids
	 */

	@Query(value = "g.v(market).in('BIDDINGMARKET').filter{it.time == time}.filter{it.supplyBid == isSupply}", type = QueryType.Gremlin)
	Iterable<Bid> getBidsForMarketForTime(@Param("market") DecarbonizationMarket market, @Param("time") long time,
			@Param("isSupply") boolean isSupply);

	@Query("START market=node({market}) MATCH (market)<-[:BIDDINGMARKET]-(bid) WHERE (bid.time = {time}) and (bid.supplyBid=true) and (bid.price <= {price}) RETURN bid ORDER BY bid.price")
	Iterable<Bid> findOffersForMarketForTimeBelowPrice(@Param("market") DecarbonizationMarket market,
			@Param("time") long time, @Param("price") double price);

	/**
	 * Find demand bids above a certain price, and return them in descending
	 * order.
	 * 
	 * @param market
	 * @param time
	 * @param price
	 * @return
	 */
	@Query("START market=node({market}) MATCH (market)<-[:BIDDINGMARKET]-(bid) WHERE (bid.time = {time}) and (bid.supplyBid=false) and (bid.price >= {price}) RETURN bid ORDER BY bid.price desc")
	Iterable<Bid> findDemandBidsForMarketForTimeAbovePrice(@Param("market") DecarbonizationMarket market,
			@Param("time") long time, @Param("price") double price);

	@Query("START market=node({market}) MATCH (market)<-[:BIDDINGMARKET]-(bid) WHERE (bid.time = {time}) and (bid.supplyBid=false) and (bid.price >= {price}) RETURN sum(bid.amount)")
	double calculateDemandBidsForMarketForTimeForPrice(@Param("market") DecarbonizationMarket market,
			@Param("time") long time, @Param("price") double price);

	@Query("START market=node({market}) MATCH (market)<-[:BIDDINGMARKET]-(bid) WHERE (bid.time = {time}) and (bid.supplyBid=false) and (bid.price = {price}) RETURN bid ORDER BY bid.price desc")
	Iterable<Bid> findDemandBidsForMarketForTimeForPrice(@Param("market") DecarbonizationMarket market,
			@Param("time") long time, @Param("price") double price);

	@Query("START market=node({market}) MATCH (market)<-[:BIDDINGMARKET]-(bid) WHERE (bid.time = {time}) and (bid.supplyBid=true) and (bid.price = {price}) RETURN bid ORDER BY bid.price desc")
	Iterable<Bid> findOffersForMarketForTimeForPrice(@Param("market") DecarbonizationMarket market,
			@Param("time") long time, @Param("price") double price);

	@Query("START market=node({market}) MATCH (market)<-[:BIDDINGMARKET]-(bid) WHERE (bid.time = {time}) and (bid.supplyBid=true) and (bid.price = {price}) RETURN sum(bid.amount)")
	double calculateOffersForMarketForTimeForPrice(@Param("market") DecarbonizationMarket market,
			@Param("time") long time, @Param("price") double price);

	@Query("START market=node({market}) MATCH (market)<-[:BIDDINGMARKET]-(bid) WHERE (bid.time = {time}) and (bid.supplyBid=false) RETURN sum(bid.amount)")
	double calculateTotalDemandForMarketForTime(@Param("market") DecarbonizationMarket market, @Param("time") long time);

	@Query("START market=node({market}) MATCH (market)<-[:BIDDINGMARKET]-(bid) WHERE (bid.time = {time}) and (bid.supplyBid=false) and (bid.price >= {price}) RETURN sum(bid.amount)")
	double calculateTotalDemandForMarketForTimeForPrice(@Param("market") DecarbonizationMarket market,
			@Param("time") long time, @Param("price") double price);

	@Query("START market=node({market}) MATCH (market)<-[:BIDDINGMARKET]-(bid) WHERE (bid.time = {time}) and (bid.supplyBid=true) RETURN sum(bid.amount)")
	double calculateTotalSupplyForMarketForTime(@Param("market") DecarbonizationMarket market, @Param("time") long time);

	@Query("START market=node({market}) MATCH (market)<-[:BIDDINGMARKET]-(bid) WHERE (bid.time = {time}) and (bid.supplyBid=true) RETURN max(bid.price)")
	double calculateTotalSupplyPriceForMarketForTime(@Param("market") DecarbonizationMarket market,
			@Param("time") long time);

	@Query("START market=node({market}) MATCH (market)<-[:BIDDINGMARKET]-(bid) WHERE (bid.time = {time}) and (bid.supplyBid=true) RETURN min(bid.price)")
	double calculateMinimumSupplyPriceForMarketForTime(@Param("market") DecarbonizationMarket market,
			@Param("time") long time);

}
