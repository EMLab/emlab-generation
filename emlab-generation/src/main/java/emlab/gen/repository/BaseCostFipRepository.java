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
package emlab.gen.repository;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.annotation.QueryType;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import emlab.gen.domain.policy.renewablesupport.BaseCostFip;
import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.PowerGridNode;

/**
 * @author Kaveri3012
 *
 */

@Repository
public interface BaseCostFipRepository extends GraphRepository<BaseCostFip> {

    @Query(value = "g.v(tech).in('BASECOST_FOR_TECHNOLOGY').as('x').out('BASECOST_FOR_LOCATION').filter{it==g.v(gridnode)}.back('x').filter{it.startTime==tick}", type = QueryType.Gremlin)
    public BaseCostFip findOneBaseCostForTechnologyAndNodeAndTime(@Param("gridnode") PowerGridNode node,
            @Param("tech") PowerGeneratingTechnology technology, @Param("tick") long tick);

    @Query(value = "g.v(tech).in('BASECOST_FOR_TECHNOLOGY').as('x').out('BASECOST_FOR_LOCATION').filter{it==g.v(gridnode)}.back('x').filter{it.startTime>=timeFrom && it.startTime<=timeTo}", type = QueryType.Gremlin)
    Iterable<BaseCostFip> findAllBaseCostFipsForTechnologyLocationAndTimeRange(@Param("gridnode") PowerGridNode node,
            @Param("tech") PowerGeneratingTechnology technology, @Param("timeFrom") long timeFrom,
            @Param("timeTo") long timeTo);

}
