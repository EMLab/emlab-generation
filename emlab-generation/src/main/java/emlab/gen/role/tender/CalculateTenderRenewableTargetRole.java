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
package emlab.gen.role.tender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.AbstractRole;
import agentspring.role.Role;
import agentspring.role.RoleComponent;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.SegmentLoad;
import emlab.gen.domain.policy.renewablesupport.RelativeRenewableTarget;
import emlab.gen.domain.policy.renewablesupport.RenewableSupportScheme;
import emlab.gen.repository.Reps;
import emlab.gen.util.GeometricTrendRegression;

/**
 * @author rjjdejeu
 *
 */

@RoleComponent
public class CalculateTenderRenewableTargetRole extends AbstractRole<RenewableSupportScheme> implements
        Role<RenewableSupportScheme> {

    @Autowired
    Reps reps;

    @Transactional
    public void act(RenewableSupportScheme scheme) {

        double demandFactor;
        double targetFactor;
        Zone zone = scheme.getRegulator().getZone();
        ElectricitySpotMarket market = reps.marketRepository.findElectricitySpotMarketForZone(zone);

        // get demand factor
        demandFactor = predictDemandForElectricitySpotMarket(market, scheme.getRegulator()
                .getNumberOfYearsLookingBackToForecastDemand(), scheme.getFutureTenderOperationStartTime());

        // get renewable energy target in factor (percent)
        RelativeRenewableTarget target = reps.relativeRenewableTargetRepository
                .findRelativeRenewableTargetByRegulator(scheme.getRegulator());
        targetFactor = target.getTrend().getValue(scheme.getFutureTenderOperationStartTime());

        // get totalLoad in MWh
        double totalConsumption = 0;
        for (SegmentLoad segmentLoad : reps.segmentLoadRepository.findAll()) {
            totalConsumption += segmentLoad.getBaseLoad() * demandFactor * segmentLoad.getSegment().getLengthInHours();
        }

        // renewable target for tender operation start year in MWh is
        double renewableTargetInMwh = demandFactor * targetFactor * totalConsumption;
        scheme.getRegulator().setAnnualRenewableTargetInMwh(renewableTargetInMwh);

    }

    public double predictDemandForElectricitySpotMarket(ElectricitySpotMarket market,
            long numberOfYearsBacklookingForForecasting, long futureTimePoint) {
        GeometricTrendRegression gtr = new GeometricTrendRegression();
        for (long time = getCurrentTick(); time > getCurrentTick() - numberOfYearsBacklookingForForecasting
                && time >= 0; time = time - 1) {
            gtr.addData(time, market.getDemandGrowthTrend().getValue(time));
        }
        double forecast = gtr.predict(futureTimePoint);
        if (Double.isNaN(forecast))
            forecast = market.getDemandGrowthTrend().getValue(getCurrentTick());
        return forecast;
    }
}
