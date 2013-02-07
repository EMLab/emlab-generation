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

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.annotation.QueryType;
import org.springframework.data.neo4j.repository.GraphRepository;

import emlab.gen.domain.agent.EnergyProducer;

/**
 * @author JCRichstein
 *
 */
public interface EnergyProducerRepository extends
		GraphRepository<EnergyProducer> {
	
	@Query(value="result = g.idx('__types__')[[className:'emlab.gen.domain.agent.EnergyProducer']].propertyFilter('__type__', FilterPipe.Filter.NOT_EQUAL, 'emlab.gen.domain.agent.TargetInvestor').toList();" +
			"if(result == null){return null;} else {Collections.shuffle(result); return result;}", type=QueryType.Gremlin)
	List<EnergyProducer> findAllEnergyProducersExceptForRenewableTargetInvestorsAtRandom();
	
}
