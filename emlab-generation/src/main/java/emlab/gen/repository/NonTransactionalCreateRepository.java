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


import org.springframework.stereotype.Repository;

import emlab.gen.domain.agent.DecarbonizationAgent;
import emlab.gen.domain.contract.CashFlow;
import emlab.gen.domain.market.Bid;
import emlab.gen.domain.market.DecarbonizationMarket;
import emlab.gen.domain.technology.PowerPlant;

/**
 * Repository for cash flows
 * @author ejlchappin
 *
 */
@Repository
public class NonTransactionalCreateRepository extends AbstractRepository<CashFlow> {

	/**
	 * Creates cash flow.
	 * Note: this is not transactional, so when called, it should be transactional there!
	 * @param from 
	 * @param to
	 * @param amount the money transfered
	 * @param type what the cashflow is about
	 * @param time the time
	 * @param plant the power plant related to this cash flow
	 * @return the cash flow
	 */
	public CashFlow createCashFlow(DecarbonizationAgent from,
			DecarbonizationAgent to, double amount, int type, long time,
			PowerPlant plant) {
		CashFlow cashFlow = new CashFlow().persist();
		cashFlow.setFrom(from);
		cashFlow.setTo(to);
		cashFlow.setMoney(amount);
		cashFlow.setType(type);
		cashFlow.setTime(time);
		cashFlow.setRegardingPowerPlant(plant);
		from.setCash(from.getCash() - amount);
		if (to != null) {
			to.setCash(to.getCash() + amount);
		}

		return cashFlow;
	}
	

    /**
     * Submit bids to a market. 
     * Note: this is not transactional, so when called, it should be transactional there!
     * @param market
     * @param agent 
     * @param time
     * @param isSupply
     * @param price
     * @param amount
     * @return the submitted bid
     */
    public Bid submitBidToMarket(DecarbonizationMarket market, DecarbonizationAgent agent, long time, boolean isSupply, double price,
            double amount) {

        Bid bid = new Bid().persist();
        bid.setBiddingMarket(market);
        bid.setBidder(agent);
        bid.setSupplyBid(isSupply);
        bid.setTime(time);
        bid.setPrice(price);
        bid.setAmount(amount);
        bid.setStatus(Bid.SUBMITTED);
        return bid;
    }

}
