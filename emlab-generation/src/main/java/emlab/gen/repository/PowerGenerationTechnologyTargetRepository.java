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

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.annotation.QueryType;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.policy.PowerGeneratingTechnologyTarget;
import emlab.gen.domain.technology.PowerGeneratingTechnology;

/**
 * @author JCRichstein
 *
 */
public interface PowerGenerationTechnologyTargetRepository extends
		GraphRepository<PowerGeneratingTechnologyTarget> {
	
	@Query(value="result = g.v(market).in('INVESTOR_MARKET').out('INVESTOR_TARGET').as('x').out('TARGET_TECHNOLOGY').idFilter(tech, FilterPipe.Filter.EQUAL).back('x'); ; if(!result.hasNext()){return null;} else{return result.next();}", type=QueryType.Gremlin)
	PowerGeneratingTechnologyTarget findOneByTechnologyAndMarket(@Param("tech") PowerGeneratingTechnology tech, @Param("market") ElectricitySpotMarket market);

	@Query(value="result = g.v(market).in('INVESTOR_MARKET').out('INVESTOR_TARGET')", type=QueryType.Gremlin)
	Iterable<PowerGeneratingTechnologyTarget> findAllByMarket(@Param("market") ElectricitySpotMarket market);
}
