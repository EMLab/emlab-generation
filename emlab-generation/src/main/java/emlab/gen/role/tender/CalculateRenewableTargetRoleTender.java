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
import emlab.gen.domain.agent.Regulator;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.repository.Reps;
import emlab.gen.util.GeometricTrendRegression;

/**
 * @author rjjdejeu
 *
 */

@RoleComponent
public class CalculateRenewableTargetRoleTender extends AbstractRole<Regulator> implements Role<Regulator> {

    @Autowired
    Reps reps;

    @Override
    @Transactional
    public void act(Regulator regulator) {

        // Zone is the country
        Zone zone = regulator.getZone();
        ElectricitySpotMarket market = reps.marketRepository.findElectricitySpotMarketForZone(zone);

    }

    // predicts demand for a full year for a certain period of the spot market,
    // based on looking back for a certain time

    public double predictDemandForElectricitySpotMarket(ElectricitySpotMarket market,
            long numberOfYearsBacklookingForForecasting, long futureTimePoint) {

        // in predicting a geometric trend (linear regression) method is used

        GeometricTrendRegression gtr = new GeometricTrendRegression();

        // starts at current year,
        // stops when time is greater then the current year minus years looking
        // in the past AND when time non-negative
        // it decrements with 1
        /*  Where is the numberOfYearsBacklookingForForecasting defined for the
         *  regulator?
         */

        for (long time = getCurrentTick(); time > getCurrentTick() - numberOfYearsBacklookingForForecasting
                && time >= 0; time = time - 1) {

            // this results into a regression by looping over previous demand
            // data till a certain point in the past.
            // DOT operator is defining where to find the method, and what to
            // act on (if applicable)

            // getValue is used as follows; it is
            /*
             * @Override public double getValue(long time) { return
             * timeSeries[(int) time - (int) startingYear]; }
             */ 

            gtr.addData(time, market.getDemandGrowthTrend().getValue(time));
        }

        /* new variable forecast is made to use the regression to a certain
         * point of the agent in the future
         * this futureTimePoint could be in my case (the regulator) the lead +
         */ permit time = 4 (biomass is the max)
        double forecast = gtr.predict(futureTimePoint);

        /* Double.isNan() method returns true if this Double value is a
         * Not-a-Number (NaN), false otherwise.
         */ What happens here exactly and why?

        if (Double.isNaN(forecast))
            forecast = market.getDemandGrowthTrend().getValue(getCurrentTick());

        // This should be then the demand in MWh predicted in the future
        return forecast;
    }

    // Is this method now applied to the regulator via 'act', yes right?

    /*
     * Gets the established renewable energy targets for NL and DE from a stored
     * file in data/policyNREAP_DE_2050.csv and data/policyNREAP_NL_2050.csv
     * which are stored as a scenario in the file Tender.xml, but this should be
     * included in a complete scenario
     */

    // <bean id="tenderTargets" class="emlab.gen.role.tender">
    //
    // <bean id="germanyRenewableTarget"
    // class="emlab.gen.trend.TimeSeriesCSVReader"
    // p:filename="/data/policyGoalsNREAP_DE_2050.csv" p:startingYear="0"
    // p:delimiter="," p:variableName="germany_Target" />
    //
    // <bean id="theNetherlandsRenewableTarget"
    // class="emlab.gen.trend.TimeSeriesCSVReader"
    // p:filename="/data/policyGoalsNREAP_NL_2050.csv" p:startingYear="0"
    // p:delimiter="," p:variableName="theNetherlands_target" />
    //
    //
    // </beans>

    /*
     * does tenderTargets relate to the scenario file? Or do I need to define
     * something is else to relate tothis part of the scenario file?
     */
    public double targetFactor = tenderTargets.getTrend().getValue(futureTimePoint);

    /*
     * Calculates the target for the current tick. But How do I retrieve
     * forecast from line 99? I thought 'return' would do the job
     */

    double renewableTarget = targetFactor * forecast;

}
