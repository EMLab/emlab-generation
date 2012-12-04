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
package emlab.domain.agent;

import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import agentspring.agent.Agent;
import emlab.domain.gis.Zone;
import emlab.domain.market.electricity.ElectricitySpotMarket;
import emlab.domain.policy.PowerGenerationTechnologyTarget;

@NodeEntity
public class RenewableTargetInvestor extends EnergyProducer implements Agent {
	
	@RelatedTo(type="INVESTOR_TARGET",elementClass=PowerGenerationTechnologyTarget.class, direction=Direction.OUTGOING)
	private Set<PowerGenerationTechnologyTarget> powerGenerationTechnologyTargets;
	
	@RelatedTo(type="INVESTOR_MARKET", elementClass=ElectricitySpotMarket.class, direction=Direction.OUTGOING)
	private ElectricitySpotMarket investorMarket;

	public Set<PowerGenerationTechnologyTarget> getPowerGenerationTechnologyTargets() {
		return powerGenerationTechnologyTargets;
	}

	public void setPowerGenerationTechnologyTargets(
			Set<PowerGenerationTechnologyTarget> powerGenerationTechnologyTargets) {
		this.powerGenerationTechnologyTargets = powerGenerationTechnologyTargets;
	}


	/**
	 * @return the investorMarket
	 */
	public ElectricitySpotMarket getInvestorMarket() {
		return investorMarket;
	}

	/**
	 * @param investorMarket the investorMarket to set
	 */
	public void setInvestorMarket(ElectricitySpotMarket investorMarket) {
		this.investorMarket = investorMarket;
	}


}
