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

import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.PowerPlantDispatchPlan;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.technology.PowerPlant;

/**
 * Repository for PowerPlantDispatchPlans
 * 
 * @author JCRichstein
 * @author ejlchappin
 *
 */

@Repository
public interface PowerPlantDispatchPlanRepository extends GraphRepository<PowerPlantDispatchPlan> {

    // @Query(value = "g.V.filter{it.getProperty('__type__')=='emlab.gen.domain.market.electricity.PowerPlantDispatchPlan' && it.getProperty('time')==time}", type = QueryType.Gremlin)
    // public Iterable<PowerPlantDispatchPlan> findAllPowerPlantDispatchPlansForTime(@Param("time") long time);

    @Query("START ppdp=node:__types__(\"className:emlab.gen.domain.market.electricity.PowerPlantDispatchPlan\") WHERE (ppdp.time={time} AND ppdp.forecast = {forecast}) RETURN ppdp")
    public Iterable<PowerPlantDispatchPlan> findAllPowerPlantDispatchPlansForTime(@Param("time") long time,
            @Param("forecast") boolean forecast);

    @Query(value = "result = g.v(plant).in('POWERPLANT_DISPATCHPLAN').as('x').propertyFilter('forecast', FilterPipe.Filter.EQUAL, forecast).out('SEGMENT_DISPATCHPLAN').idFilter(segment, FilterPipe.Filter.EQUAL).back('x')", type = QueryType.Gremlin)
    public Iterable<PowerPlantDispatchPlan> findAllPowerPlantDispatchPlanForPowerPlantForSegmentForTime(@Param("plant") PowerPlant plant,
            @Param("segment") Segment segment, @Param("time") long time,
            @Param("forecast") boolean forecast);

    @Query(value = "result = g.v(plant).in('POWERPLANT_DISPATCHPLAN').as('x').propertyFilter('forecast', FilterPipe.Filter.EQUAL, forecast).out('SEGMENT_DISPATCHPLAN').idFilter(segment, FilterPipe.Filter.EQUAL).back('x').propertyFilter('time', FilterPipe.Filter.EQUAL, time); if(!result.hasNext()){return null;} else{return result.next();}", type = QueryType.Gremlin)
    public PowerPlantDispatchPlan findOnePowerPlantDispatchPlanForPowerPlantForSegmentForTime(@Param("plant") PowerPlant plant,
            @Param("segment") Segment segment, @Param("time") long time,
            @Param("forecast") boolean forecast);

    // @Query(value = "g.v(segment).in('SEGMENT_DISPATCHPLAN').propertyFilter('time', FilterPipe.Filter.EQUAL, time)", type = QueryType.Gremlin)
    // public Iterable<PowerPlantDispatchPlan> findAllPowerPlantDispatchPlansForSegmentForTime(@Param("segment") Segment segment,
    // @Param("time") long time);

    @Query("START segment = node({segment}) MATCH (segment)<-[:SEGMENT_DISPATCHPLAN]-(ppdp) WHERE (ppdp.time = {time} AND ppdp.forecast={forecast}) RETURN ppdp")
    public Iterable<PowerPlantDispatchPlan> findAllPowerPlantDispatchPlansForSegmentForTime(@Param("segment") Segment segment,
            @Param("time") long time, @Param("forecast") boolean forecast);

    // @Query(value = "g.v(segment).in('SEGMENT_DISPATCHPLAN').propertyFilter('time', FilterPipe.Filter.EQUAL, time).sort{it.price}._()", type = QueryType.Gremlin)
    // public Iterable<PowerPlantDispatchPlan> findSortedPowerPlantDispatchPlansForSegmentForTime(@Param("segment") Segment segment,
    // @Param("time") long time);

    @Query("START segment = node({segment}) MATCH (segment)<-[:SEGMENT_DISPATCHPLAN]-(ppdp) WHERE (ppdp.time = {time} AND ppdp.forecast={forecast}) RETURN ppdp ORDER BY ppdp.price")
    public Iterable<PowerPlantDispatchPlan> findSortedPowerPlantDispatchPlansForSegmentForTime(@Param("segment") Segment segment,
            @Param("time") long time, @Param("forecast") boolean forecast);

    // descending order
    @Query("START segment = node({segment}) MATCH (segment)<-[:SEGMENT_DISPATCHPLAN]-(ppdp) WHERE (ppdp.time = {time} AND ppdp.forecast={forecast}) RETURN ppdp ORDER BY ppdp.price desc")
    public Iterable<PowerPlantDispatchPlan> findDescendingSortedPowerPlantDispatchPlansForSegmentForTime(
            @Param("segment") Segment segment, @Param("time") long time, @Param("forecast") boolean forecast);

    @Query(value = "g.v(plant).in('POWERPLANT_DISPATCHPLAN').propertyFilter('forecast', FilterPipe.Filter.EQUAL, forecast).propertyFilter('time', FilterPipe.Filter.EQUAL, time)", type = QueryType.Gremlin)
    public Iterable<PowerPlantDispatchPlan> findAllPowerPlantDispatchPlansForPowerPlantForTime(@Param("plant") PowerPlant plant,
            @Param("time") long time, @Param("forecast") boolean forecast);

    @Query(value = "g.v(producer).out('BIDDER').propertyFilter('time', FilterPipe.Filter.EQUAL, time).propertyFilter('forecast', FilterPipe.Filter.EQUAL, forecast)", type = QueryType.Gremlin)
    public Iterable<PowerPlantDispatchPlan> findAllPowerPlantDispatchPlansForEnergyProducerForTime(
            @Param("producer") EnergyProducer producer, @Param("time") long time, @Param("forecast") boolean forecast);

    @Query(value = "g.v(producer).out('BIDDER').propertyFilter('time', FilterPipe.Filter.EQUAL, time).propertyFilter('status', FilterPipe.Filter.GREATER_THAN_EQUAL , 2).propertyFilter('forecast', FilterPipe.Filter.EQUAL, forecast)", type = QueryType.Gremlin)
    public Iterable<PowerPlantDispatchPlan> findAllAcceptedPowerPlantDispatchPlansForEnergyProducerForTime(
            @Param("producer") EnergyProducer producer, @Param("time") long time, @Param("forecast") boolean forecast);

    @Query(value = "g.v(producer).out('BIDDER').propertyFilter('time', FilterPipe.Filter.EQUAL, time).propertyFilter('forecast', FilterPipe.Filter.EQUAL, forecast).as('x').out('SEGMENT_DISPATCHPLAN').idFilter(segment, FilterPipe.Filter.EQUAL).back('x')", type = QueryType.Gremlin)
    public Iterable<PowerPlantDispatchPlan> findAllPowerPlantDispatchPlansForEnergyProducerForTimeAndSegment(
            @Param("segment") Segment segment, @Param("producer") EnergyProducer producer, @Param("time") long time, @Param("forecast") boolean forecast);

    @Query(value = "g.v(producer).out('BIDDER').propertyFilter('time', FilterPipe.Filter.EQUAL, time).propertyFilter('forecast', FilterPipe.Filter.EQUAL, forecast).propertyFilter('status', FilterPipe.Filter.GREATER_THAN_EQUAL, 2).as('x').out('SEGMENT_DISPATCHPLAN').idFilter(segment, FilterPipe.Filter.EQUAL).back('x')", type = QueryType.Gremlin)
    public Iterable<PowerPlantDispatchPlan> findAllAcceptedPowerPlantDispatchPlansForEnergyProducerForTimeAndSegment(
            @Param("segment") Segment segment, @Param("producer") EnergyProducer producer, @Param("time") long time, @Param("forecast") boolean forecast);

    // @Query("START segment = node({segment}), market=node({market}) MATCH (segment)<-[:SEGMENT_DISPATCHPLAN]-(ppdp)-[:BIDDINGMARKET]->(market) WHERE (ppdp.time = {time}) and (ppdp.status >= 2) RETURN ppdp")
    // public Iterable<PowerPlantDispatchPlan> findAllAcceptedPowerPlantDispatchPlansForMarketSegmentAndTime(
    // @Param("market") ElectricitySpotMarket esm, @Param("segment") Segment segment, @Param("time") long time);

    @Query(value = "g.v(market).in('BIDDINGMARKET').propertyFilter('time', FilterPipe.Filter.EQUAL, time).propertyFilter('forecast', FilterPipe.Filter.EQUAL, forecast).propertyFilter('status', FilterPipe.Filter.GREATER_THAN_EQUAL, 2).as('x').out('SEGMENT_DISPATCHPLAN').idFilter(segment, FilterPipe.Filter.EQUAL).back('x')", type = QueryType.Gremlin)
    public Iterable<PowerPlantDispatchPlan> findAllAcceptedPowerPlantDispatchPlansForMarketSegmentAndTime(
            @Param("market") ElectricitySpotMarket esm, @Param("segment") Segment segment, @Param("time") long time, @Param("forecast") boolean forecast);

    // @Query("START segment = node({segment} MATCH (segment)<-[:SEGMENT_DISPATCHPLAN]-(ppdp)<-[:BIDDER]-(node({producer})) WHERE (ppdp.time = {time}) AND (ppdp.status >=1) RETURN ppdp")
    // public Iterable<PowerPlantDispatchPlan> findAllAcceptedPowerPlantDispatchPlansForEnergyProducerForTimeAndSegment(
    // @Param("segment") Segment segment, @Param("producer") EnergyProducer producer, @Param("time") long time);
}

// package emlab.gen.repository;
//
// import java.util.ArrayList;
// import java.util.List;
//
// import org.springframework.stereotype.Repository;
// import org.springframework.transaction.annotation.Transactional;
//
// import com.tinkerpop.blueprints.pgm.Vertex;
// import com.tinkerpop.gremlin.pipes.filter.PropertyFilterPipe;
// import com.tinkerpop.pipes.Pipe;
// import com.tinkerpop.pipes.filter.FilterPipe;
// import com.tinkerpop.pipes.util.Pipeline;
//
// import emlab.gen.domain.agent.EnergyProducer;
// import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
// import emlab.gen.domain.market.electricity.PowerPlantDispatchPlan;
// import emlab.gen.domain.market.electricity.Segment;
// import emlab.gen.domain.technology.PowerPlant;
//
// @Repository
// public class PowerPlantDispatchPlanRepository extends AbstractRepository<PowerPlantDispatchPlan> {
//
// public PowerPlantDispatchPlan findOnePowerPlantDispatchPlanForPowerPlantForSegmentForTime(PowerPlant plant, Segment segment, long time) {
// for (PowerPlantDispatchPlan plan : findAllPowerPlantDispatchPlansForPowerPlantForTime(plant, time)) {
// if (plan.getSegment().equals(segment)) {
// return plan;
// }
// }
// return null;
// }
//
// public Iterable<PowerPlantDispatchPlan> findAllPowerPlantDispatchPlansForSegmentForTime(Segment segment, long time) {
//
// // get incoming bids
// Pipe<Vertex, Vertex> bids = new LabeledEdgePipe("SEGMENT_DISPATCHPLAN", LabeledEdgePipe.Step.BOTH_BOTH);
// // filter by time
// Pipe<Vertex, Vertex> timeFilter = new PropertyFilterPipe<Vertex, Long>("time", time, FilterPipe.Filter.EQUAL);
// // create pipeline
// Pipe<Vertex, Vertex> pipeline = new Pipeline<Vertex, Vertex>(bids, timeFilter);
// return this.findAllByPipe(segment, pipeline);
// }
//
// public Iterable<PowerPlantDispatchPlan> findAllPowerPlantDispatchPlansForTime(long time) {
// List<PowerPlantDispatchPlan> list = new ArrayList<PowerPlantDispatchPlan>();
// for (PowerPlantDispatchPlan plan : findAll()) {
// if (plan.getTime() == time) {
// list.add(plan);
// }
// }
// return list;
// }
//
// public Iterable<PowerPlantDispatchPlan> findAllPowerPlantDispatchPlansForPowerPlantForTime(PowerPlant powerPlant, long time) {
// Pipe<Vertex, Vertex> bids = new LabeledEdgePipe("POWERPLANT_DISPATCHPLAN", LabeledEdgePipe.Step.BOTH_BOTH);
// // filter by time
// Pipe<Vertex, Vertex> timeFilter = new PropertyFilterPipe<Vertex, Long>("time", time, FilterPipe.Filter.EQUAL);
// // create pipeline
// Pipe<Vertex, Vertex> pipeline = new Pipeline<Vertex, Vertex>(bids, timeFilter);
//
// return this.findAllByPipe(powerPlant, pipeline);
// }
//
// public Iterable<PowerPlantDispatchPlan> findAllPowerPlantDispatchPlansForEnergyProducerForTime(EnergyProducer energyProducer, long time) {
// Pipe<Vertex, Vertex> bids = new LabeledEdgePipe("BIDDER", LabeledEdgePipe.Step.BOTH_BOTH);
// // filter by time
// Pipe<Vertex, Vertex> timeFilter = new PropertyFilterPipe<Vertex, Long>("time", time, FilterPipe.Filter.EQUAL);
// // create pipeline
// Pipe<Vertex, Vertex> pipeline = new Pipeline<Vertex, Vertex>(bids, timeFilter);
//
// return this.findAllByPipe(energyProducer, pipeline);
// }
//
// @Transactional
// public PowerPlantDispatchPlan submitOrUpdatePowerPlantDispatchPlanForSpotMarket(PowerPlant plant, EnergyProducer producer,
// ElectricitySpotMarket market, Segment segment, long time, double price, double capacity) {
//
// // make a new one if it
// PowerPlantDispatchPlan plan = findOnePowerPlantDispatchPlanForPowerPlantForSegmentForTime(plant, segment, time);
// if (plan == null) {
// plan = new PowerPlantDispatchPlan().persist();
// plan.setPowerPlant(plant);
// plan.setSegment(segment);
// plan.setTime(time);
//
// }
// plan.setBidder(producer);
// plan.setBiddingMarket(market);
// plan.setPrice(price);
// plan.setCapacitySpotMarket(capacity);
// plan.setCapacityLongTermContract(0d);
// return null;
// }
//
// @Transactional
// public void updateCapacityLongTermContract(PowerPlantDispatchPlan plan, double capacity) {
// plan.setCapacityLongTermContract(capacity);
// // if(plan.getCapacitySpotMarket() + capacity >
// // plan.getPowerPlant().getTechnology().getCapacity()){
// // logger.warn("PROBLEM: Adding to much ltc capacity to dispatch plan: "
// // + plan);
// // }
// }
//
// @Transactional
// public void updateCapacitySpotMarket(PowerPlantDispatchPlan plan, double capacity) {
// plan.setCapacitySpotMarket(capacity);
// // if(plan.getCapacityLongTermContract() + capacity >
// // plan.getPowerPlant().getTechnology().getCapacity()){
// // logger.warn("PROBLEM: Adding to much spot capacity to dispatch plan: "
// // + plan);
// // }
// }
// }
