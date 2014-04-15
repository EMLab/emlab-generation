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
package emlab.gen.role.co2policy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.transaction.annotation.Transactional;

import agentspring.role.AbstractRole;
import agentspring.role.RoleComponent;
import emlab.gen.domain.agent.Government;
import emlab.gen.repository.Reps;
import emlab.gen.repository.StrategicReserveOperatorRepository;

/**
 * @author JCRichstein
 *
 */
@RoleComponent
public class MarketStabilityReserveRole extends AbstractRole<Government> {

    @Transient
    @Autowired
    Reps reps;

    @Transient
    @Autowired
    StrategicReserveOperatorRepository strategicReserveOperatorRepository;

    @Autowired
    Neo4jTemplate template;

    @Transactional
    public void act(Government government) {
        double allowancesInCirculation = reps.decarbonizationAgentRepository.determinePreviouslyBankedCO2Certificates();
        double inflowToMarketReserve = calculateInflowToMarketReserveForTimeStep(allowancesInCirculation, government);
        government.setStabilityReserve(government.getStabilityReserve() + inflowToMarketReserve);
        government.getCo2CapTrend().setValue(getCurrentTick(),
                government.getCo2CapTrend().getValue(getCurrentTick()) - inflowToMarketReserve);
    }

    public double calculateInflowToMarketReserveForTimeStep(double bankedCertificatesInTick,
            Government government) {
        double allowancesInCirculation = bankedCertificatesInTick;
        if (allowancesInCirculation > government.getStabilityReserveAddingThreshold()) {
            double allowancesToBeAddedToReserve = Math.max(
                    allowancesInCirculation * government.getStabilityReserveAddingPercentage(),
                    government.getStabilityReserveAddingMinimum());
            return allowancesToBeAddedToReserve;
        } else if (allowancesInCirculation < government.getStabilityReserveReleasingThreshold()) {
            double allowancesToBeReleased = Math.min(government.getStabilityReserve(),
                    government.getStabilityReserveReleaseQuantity());
            return -allowancesToBeReleased;
        }
        return 0;
    }
}
