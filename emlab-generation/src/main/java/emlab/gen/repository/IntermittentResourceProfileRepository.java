package emlab.gen.repository;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.annotation.QueryType;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import emlab.gen.domain.technology.IntermittentResourceProfile;
import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.PowerGridNode;

public interface IntermittentResourceProfileRepository extends GraphRepository<IntermittentResourceProfile> {

    @Query(value = "profiles=g.v(technology).in('INTERMITTENT_TECHNOLOGY').filter{it.__type__=='emlab.gen.domain.technology.IntermittentResourceProfile'}.as('irp').in('INTERMITTENT_PRODUCTION_NODE').filter{it==g.v(node)}.back('irp');"
            + "if(profiles.hasNext()){return profiles.next()} else{return null}", type = QueryType.Gremlin)
    IntermittentResourceProfile findIntermittentResourceProfileByTechnologyAndNode(
            @Param("technology") PowerGeneratingTechnology technology, @Param("node") PowerGridNode node);

}
