/*******************************************************************************
 * Copyright 2014 the original author or authors.
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
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import emlab.gen.domain.market.electricity.IntermittentTechnologyNodeLoadFactor;
import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.PowerGridNode;
import emlab.gen.domain.technology.PowerPlant;

/**
 * @author jrichstein
 *
 */
public interface IntermittentTechnologyNodeLoadFactorRepository extends
GraphRepository<IntermittentTechnologyNodeLoadFactor> {
    //
    // @Query(value = "nodeName=g.v(plant).out('LOCATION').name;"
    // +
    // "g.v(plant).out('TECHNOLOGY').in('LOADFACTOR_TECHNOLOGY').as('x').out('LOADFACTOR_NODE').filter{it.name==nodeName}.back('x').next()",
    // type = QueryType.Gremlin)
    // IntermittentTechnologyNodeLoadFactor
    // findIntermittentTechnologyNodeLoadFactorForPowerPlant(
    // @Param("plant") PowerPlant plant);

    @Query("start plant=node({plant}) match (plant)-[:LOCATION]->(node)<-[:LOADFACTOR_NODE]-(intTechNodeLoadFactor)-[:LOADFACTOR_TECHNOLOGY]->(tech)<-[:TECHNOLOGY]-(plant) return intTechNodeLoadFactor")
    IntermittentTechnologyNodeLoadFactor findIntermittentTechnologyNodeLoadFactorForPowerPlant(
            @Param("plant") PowerPlant plant);

    @Query("start gridnode=node({node}), tech=node({tech}) match (gridnode)<-[:LOADFACTOR_NODE]-(intTechNodeLoadFactor)-[:LOADFACTOR_TECHNOLOGY]->(tech) return intTechNodeLoadFactor")
    IntermittentTechnologyNodeLoadFactor findIntermittentTechnologyNodeLoadFactorForNodeAndTechnology(
            @Param("node") PowerGridNode node, @Param("tech") PowerGeneratingTechnology tech);

}
