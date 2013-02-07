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

import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.technology.PowerGridNode;

public interface PowerGridNodeRepository extends GraphRepository<PowerGridNode> {

	@Query("START zone=node({zone}) match (zone)<-[:REGION]-(powergridnode) WHERE powergridnode.__type__ = 'emlab.gen.domain.technology.PowerGridNode' RETURN powergridnode")
	Iterable<PowerGridNode> findAllPowerGridNodesByZone(@Param("zone") Zone zone);
	
	@Query(value="g.v(market).out('ZONE').in('REGION').next()", type=QueryType.Gremlin)
	PowerGridNode findFirstPowerGridNodeByElectricitySpotMarket(@Param("market") ElectricitySpotMarket esm);
}
