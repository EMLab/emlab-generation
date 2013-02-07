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

import agentspring.role.Role;
import agentspring.role.RoleComponent;
import emlab.gen.domain.agent.CommoditySupplier;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.market.ClearingPoint;
import emlab.gen.domain.market.CommodityMarket;
import emlab.gen.repository.Reps;

/**
 * Creates and clears the {@link CommodityMarket}. {@link EnergyProducer} submit bids to purchase commodities; {@link CommoditySupplier} submits ask offers to sell commodities rights
 * 
 * @author <a href="mailto:E.J.L.Chappin@tudelft.nl">Emile Chappin</a>
 * @author <a href="mailto:A.Chmieliauskas@tudelft.nl">Alfredas Chmieliauskas</a>
 * 
 */
@RoleComponent
public class ClearCommodityMarketRole extends AbstractMarketRole<CommodityMarket> implements Role<CommodityMarket> {

    @Autowired
    Reps reps;

    public void act(CommodityMarket market) {

        logger.info("Clearing the commodity market for {}", market.getSubstance());

        // clear the market
        // Iterable<Bid> demandBids = reps.bidRepository.findDemandBidsForMarketForTime(market, getCurrentTick());
        // Iterable<Bid> supplyBids = reps.bidRepository.findOffersForMarketForTime(market, getCurrentTick());

        // ClearingPoint clearingPoint = calculateClearingPoint(market, supplyBids, demandBids, getCurrentTick());
        ClearingPoint clearingPoint = calculateClearingPoint(market, getCurrentTick());

        if (clearingPoint != null) {
            // clearingPoint.updateAbstractMarket(market); // TODO why is this line here, is this needed, it is already done before right?
            logger.info("Clearing: price " + clearingPoint.getPrice() + " / volume " + clearingPoint.getVolume());
        } else {
            logger.warn("{} did not clear!", market);
        }
    }

    @Override
    public Reps getReps() {
        return reps;
    }

}
