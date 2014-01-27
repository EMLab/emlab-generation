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
package emlab.gen.role.market;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.Role;
import agentspring.role.RoleComponent;
import emlab.gen.domain.agent.EnergyConsumer;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.contract.CashFlow;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.Bid;
import emlab.gen.domain.market.CO2Auction;
import emlab.gen.domain.market.ClearingPoint;
import emlab.gen.domain.market.CommodityMarket;
import emlab.gen.domain.market.DecarbonizationMarket;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.repository.Reps;

/**
 * Creates and clears the {@link ElectricitySpotMarket} for one {@link Zone}. {@link EnergyConsumer} submit bids to purchase electricity; {@link EnergyProducer} submit ask offers to sell power. The
 * market is divided into {@link Segment}s and cleared for each segment.
 * 
 * @author <a href="mailto:E.J.L.Chappin@tudelft.nl">Emile Chappin</a>
 * 
 * @author <a href="mailto:A.Chmieliauskas@tudelft.nl">Alfredas Chmieliauskas</a>
 * 
 */
@RoleComponent
public class ProcessAcceptedBidsRole extends AbstractMarketRole<DecarbonizationMarket> implements Role<DecarbonizationMarket> {

    @Autowired
    private Reps reps;

    @Override
    public Reps getReps() {
        return reps;
    }

    @Override
    @Transactional
    public void act(DecarbonizationMarket market) {

        logger.info("Process accepted bids to cash flow now");
        int cashFlowType = 0;
        boolean isCO2Traded = false;
        if (market instanceof CO2Auction) {
            cashFlowType = CashFlow.CO2AUCTION;
            isCO2Traded = true;
        } else if (market instanceof CommodityMarket) {
            cashFlowType = CashFlow.COMMODITY;
        } else {
            cashFlowType = CashFlow.UNCLASSIFIED;
        }

        // clear the market for each segment of the load duration curve
        Iterable<Bid> acceptedSupplyBids = reps.bidRepository.findAllAcceptedOffersForMarketForTime(market, getCurrentTick());
        Iterable<Bid> acceptedDemandBids = reps.bidRepository.findAllAcceptedDemandBidsForMarketForTime(market, getCurrentTick());

        // Assuming only one price on this market for this time step and
        // iteration.
        ClearingPoint clearingPoint = reps.clearingPointRepositoryOld.findClearingPointForMarketAndTime(market,
                getCurrentTick(), false);

        for (Bid bid : acceptedSupplyBids) {
            // if (bid.getStatus() >= Bid.PARTLY_ACCEPTED) {
            reps.nonTransactionalCreateRepository.createCashFlow(market, bid.getBidder(),
                    bid.getAcceptedAmount() * clearingPoint.getPrice(), cashFlowType, getCurrentTick(), null);
            if (isCO2Traded) {
                bid.getBidder().setCo2Allowances(bid.getBidder().getCo2Allowances() - bid.getAcceptedAmount());
            }
            // }
        }
        for (Bid bid : acceptedDemandBids) {
            // if (bid.getStatus() >= Bid.PARTLY_ACCEPTED) {
            reps.nonTransactionalCreateRepository.createCashFlow(bid.getBidder(), market,
                    bid.getAcceptedAmount() * clearingPoint.getPrice(), cashFlowType, getCurrentTick(), null);
            if (isCO2Traded) {
                bid.getBidder().setCo2Allowances(bid.getBidder().getCo2Allowances() + bid.getAcceptedAmount());
            }
            // }
        }
    }

}
