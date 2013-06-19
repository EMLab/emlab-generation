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
package emlab.gen.role.investment;

import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.aspects.core.NodeBacked;

import agentspring.role.Role;
import agentspring.role.RoleComponent;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.role.AbstractEnergyProducerRole;

/**
 * @author JCRichstein
 *
 */
@RoleComponent
@NodeEntity
public class GenericInvestmentRole<T extends EnergyProducer> extends AbstractEnergyProducerRole<T> implements Role<T>,
		NodeBacked {

	@Override
	public void act(T agent) {
		// logger.warn(agent.getName() + " does " +
		// agent.getInvestmentRole().getClass().toString());
			agent.getInvestmentRole().act(agent);
	}


}
