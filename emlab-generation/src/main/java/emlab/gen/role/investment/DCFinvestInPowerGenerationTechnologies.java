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
package emlab.gen.role.investment;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math.stat.regression.SimpleRegression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.aspects.core.NodeBacked;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.Role;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.market.ClearingPoint;
import emlab.gen.domain.technology.Substance;
import emlab.gen.repository.Reps;

@Configurable
@NodeEntity
public class DCFinvestInPowerGenerationTechnologies<T extends EnergyProducer> extends GenericInvestmentRole<T>
implements Role<T>,
NodeBacked {

    @Transient
    @Autowired
    Reps reps;

    @Transactional
    @Override
    public void act(EnergyProducer agent) {

        Map<Substance, SimpleRegression> expectedFuelPrices = new HashMap<Substance, SimpleRegression>();
        for (Substance substance : reps.genericRepository.findAll(Substance.class)) {

            // co2
        }
        // price

        // Investment decision
        agent.setWillingToInvest(false);
    }

    SimpleRegression calculateRegressionBasedOnTimeStepsAndSubstance(long startTime, long endTime, Substance substance) {

        SimpleRegression sr = new SimpleRegression();

        Iterable<ClearingPoint> clearingPoints = reps.clearingPointRepository.findAllClearingPointsForSubstanceAndTimeRange(substance,
 startTime, endTime, false);

        for (ClearingPoint cp : clearingPoints) {
            sr.addData(cp.getTime(), cp.getPrice());
        }
        return sr;
    }

}
