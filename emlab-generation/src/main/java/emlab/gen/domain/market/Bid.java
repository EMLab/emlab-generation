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
package emlab.gen.domain.market;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.transaction.annotation.Transactional;

import emlab.gen.domain.agent.DecarbonizationAgent;

@NodeEntity
public class Bid {

    public static int FAILED = -1;
    public static int NOT_SUBMITTED = 0;
    public static int SUBMITTED = 1;
    public static int PARTLY_ACCEPTED = 2;
    public static int ACCEPTED = 3;

    @RelatedTo(type = "BIDDER", elementClass = DecarbonizationAgent.class, direction = Direction.INCOMING)
    private DecarbonizationAgent bidder;

    @RelatedTo(type = "BIDDINGMARKET", elementClass = DecarbonizationMarket.class, direction = Direction.OUTGOING)
    private DecarbonizationMarket biddingMarket;

    private double amount;
    private double acceptedAmount;
    private double price;
    @Indexed(indexName = "bidTime")
    private long time;
    private int status;
    private boolean supplyBid;

    public DecarbonizationAgent getBidder() {
        return bidder;
    }

    public void setBidder(DecarbonizationAgent agent) {
        this.bidder = agent;
    }

    public DecarbonizationMarket getBiddingMarket() {
        return biddingMarket;
    }

    public void setBiddingMarket(DecarbonizationMarket market) {
        this.biddingMarket = market;
    }

    /**
     * IMPORTANT this returns the capacity that was bid into the spot market,
     * so for the depending class PPDP this means without the capacity reserved
     * for long-term markets.
     * 
     * @return
     */
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getAcceptedAmount() {
        return acceptedAmount;
    }

    public void setAcceptedAmount(double acceptedAmount) {
        this.acceptedAmount = acceptedAmount;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isSupplyBid() {
        return supplyBid;
    }

    public void setSupplyBid(boolean supplyBid) {
        this.supplyBid = supplyBid;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Changes the amount of a bid
     * 
     * @param bid
     *            the bid to change
     * @param amount
     *            the new amount
     */
    @Transactional
    public void updateAmount(double amount) {
        setAmount(amount);
    }

    /**
     * Changes the status of a bid
     * 
     * @param bid
     *            the bid to change
     * @param status
     *            the new status
     */
    @Transactional
    public void updateStatus(int status) {
        setStatus(status);
    }

    @Override
    public String toString() {
        return "for " + getBiddingMarket() + " price: " + getPrice() + " amount: " + getAmount() + " isSupply: "
                + isSupplyBid();
    }
}
