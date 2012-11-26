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
package emlab.repository;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import emlab.domain.gis.Zone;
import emlab.domain.technology.PowerGridNode;

public interface PowerGridNodeRepository extends GraphRepository<PowerGridNode> {

	@Query("START zone=node({zone}) match (zone)<-[:REGION]-(powergridnode) WHERE powergridnode.__type__ = 'emlab.domain.technology.PowerGridNode' RETURN powergridnode")
	Iterable<PowerGridNode> findAllPowerGridNodesByZone(@Param("zone") Zone zone);
}
