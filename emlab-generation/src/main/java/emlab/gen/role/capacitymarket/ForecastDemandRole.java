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
package emlab.gen.role.capacitymarket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.AbstractRole;
import agentspring.role.Role;
import agentspring.role.RoleComponent;
import emlab.gen.domain.agent.Regulator;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.repository.Reps;
import emlab.gen.util.GeometricTrendRegression;

/**
 * @author Kaveri
 * 
 */
@RoleComponent
public class ForecastDemandRole extends AbstractRole<Regulator> implements Role<Regulator> {

    @Autowired
    Reps reps;

    @Override
    @Transactional
    public void act(Regulator regulator) {
        long capabilityYear = 0;
        capabilityYear = getCurrentTick() + regulator.getTargetPeriod();

        Zone zone = regulator.getZone();
        ElectricitySpotMarket market = reps.marketRepository.findElectricitySpotMarketForZone(zone);

        // double trend =
        // market.getDemandGrowthTrend().getValue(getCurrentTick());
        // double peakLoadforMarket = trend * peakLoadforMarketNOtrend;
        // double reserveMargin = regulator.getReserveMargin();
        // double demandTarget = peakLoadforMarket * (1 + reserveMargin);

        // regulator.setDemandTarget(demandTarget);

        /*
         * // Computing Demand (the current year's demand is not considered for
         * // regression, as it is forecasted. double expectedDemandFactor = 0d;
         */

        double expectedDemandFactor = 0d;
        if (getCurrentTick() < 2) {

            expectedDemandFactor = market.getDemandGrowthTrend().getValue(getCurrentTick());
        } else {

            GeometricTrendRegression gtr = new GeometricTrendRegression();
            for (long time = getCurrentTick() - 1; time > getCurrentTick() - 1
                    - regulator.getNumberOfYearsLookingBackToForecastDemand()
                    && time >= 0; time = time - 1) {
                gtr.addData(time, market.getDemandGrowthTrend().getValue(time));
            }
            expectedDemandFactor = gtr.predict(capabilityYear);
        }
        logger.warn("ExpectedDemandFactor for this tick: " + expectedDemandFactor);
        logger.warn("demand factor " + market.getDemandGrowthTrend().getValue(getCurrentTick()));
        // Calculate peak demand across all markets

        double peakLoadforMarketNOtrend = reps.segmentLoadRepository.peakLoadbyZoneMarketandTime(zone, market);
        double peakExpectedDemand = peakLoadforMarketNOtrend * expectedDemandFactor;

        // Compute demand target by multiplying reserve margin double double
        double demandTarget = peakExpectedDemand * (1 + regulator.getReserveMargin());

        regulator.setDemandTarget(demandTarget);

    }

}