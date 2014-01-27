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
package emlab.gen.domain.market.electricity;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.transaction.annotation.Transactional;

import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.market.Bid;
import emlab.gen.domain.technology.PowerPlant;

/**
 * The power plant dispatch plan is an extension to the bid, and takes into account information that is necessary for the fulfillment of longt-term contracts and the dispatch of specific power plants.
 * However, the function of it for a pure spot market clearing is identical to a normal bid.
 * 
 * The Electricity Long Term Bid is the reflecting commitment to meet long term contracts. Before being able to make a(n updated) valid bid on the spot market, we need to define what part of capacity
 * of a certain power plant in a certain segment is covered by long-term contracts. This needs to be updated every iteration in the market clearing algorithm.
 * 
 * @author ejlchappin
 * @author jcrichstein
 * 
 */
@NodeEntity
public class PowerPlantDispatchPlan extends Bid {

    @RelatedTo(type = "SEGMENT_DISPATCHPLAN", elementClass = Segment.class, direction = Direction.OUTGOING)
    private Segment segment;

    @RelatedTo(type = "POWERPLANT_DISPATCHPLAN", elementClass = PowerPlant.class, direction = Direction.OUTGOING)
    private PowerPlant powerPlant;

    public static int CONTRACTED = -11;
    public static int PARTLY_CONTRACTED = -10;
    public static int NOT_CONTRACTED = -9;

    private boolean forecast;

    /**
     * Is set to always true, since it the power plant dispatch plan is only for supply bids to the spot market.
     */
    private final boolean supplyBid = true;

    private double capacityLongTermContract;
    /**
     * IMPORTANT: Amount (capacity in MW) that is bid on to the SPOT MARKET, without long term contracts.
     */
    // private double amount;
    /**
     * In the case of the power plant dispatch plan: Marginal cost excluding CO2.
     */
    // private double price;
    private double bidWithoutCO2;

    // private long time;

    private int SRstatus;
    private double oldPrice;

    public int getSRstatus() {
        return SRstatus;
    }

    public void setSRstatus(int sRstatus) {
        SRstatus = sRstatus;
    }

    public double getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(double oldPrice) {
        this.oldPrice = oldPrice;
    }

    @Indexed(indexName = "ppdpTime")
    private int ppdpTime;

    public int getPpdpTime() {
        return ppdpTime;
    }

    public void setPpdpTime(int ppdpTime) {
        this.ppdpTime = ppdpTime;
    }

    public Segment getSegment() {
        return segment;
    }

    public void setSegment(Segment segment) {
        this.segment = segment;
    }

    public PowerPlant getPowerPlant() {
        return powerPlant;
    }

    public void setPowerPlant(PowerPlant powerPlant) {
        this.powerPlant = powerPlant;
    }

    public double getCapacityLongTermContract() {
        return capacityLongTermContract;
    }

    public void setCapacityLongTermContract(double capacityLongTermContract) {
        this.capacityLongTermContract = capacityLongTermContract;
    }

    // public long getTime() {
    // return time;
    // }
    //
    // public void setTime(long time) {
    // this.time = time;
    // this.ppdpTime = (int) time;
    // }

    @Override
    public String toString() {
        return "for " + getBidder() + " power plant: " + getPowerPlant() + " in segment " + segment + " plans to sell long term: "
 + getCapacityLongTermContract() + " plans to sell capacity spot: "
                + getAmount() + "for price: " + getPrice();
    }

    public double getBidWithoutCO2() {
        return bidWithoutCO2;
    }

    public void setBidWithoutCO2(double bidWithoutCO2) {
        this.bidWithoutCO2 = bidWithoutCO2;
    }

    @Override
    public boolean isSupplyBid() {
        return supplyBid;
    }

    public void specifyNotPersist(PowerPlant plant, EnergyProducer producer, ElectricitySpotMarket market, Segment segment, long time,
            double price, double bidWithoutCO2, double spotMarketCapacity,
            double longTermContractCapacity, int status, boolean forecast) {
        this.setPowerPlant(plant);
        this.setSegment(segment);
        this.setTime(time);
        this.setBidder(producer);
        this.setBiddingMarket(market);
        this.setPrice(price);
        this.setBidWithoutCO2(bidWithoutCO2);
        this.setAmount(spotMarketCapacity);
        this.setCapacityLongTermContract(longTermContractCapacity);
        this.setStatus(status);
        this.setForecast(forecast);
    }

    // All transactional methods below are signified by starting with update
    @Transactional
    public void specifyAndPersist(PowerPlant plant, EnergyProducer producer, ElectricitySpotMarket market, Segment segment, long time,
            double price, double bidWithoutCO2, double spotMarketCapacity, double longTermContractCapacity, int status, boolean forecast) {
        this.persist();
        this.specifyNotPersist(plant, producer, market, segment, time, price, bidWithoutCO2, spotMarketCapacity, longTermContractCapacity,
                status, forecast);

    }

    @Transactional
    public void updateCapacityLongTermContract(double capacity) {
        this.setCapacityLongTermContract(capacity);
    }

    @Transactional
    public void updateCapacitySpotMarket(double capacity) {
        this.setAmount(capacity);
    }

    public boolean isForecast() {
        return forecast;
    }

    public void setForecast(boolean forecast) {
        this.forecast = forecast;
    }

}
