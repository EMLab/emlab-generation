package emlab.repository;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.annotation.QueryType;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import emlab.domain.technology.IntermittentResourceProfile;
import emlab.domain.technology.PowerGeneratingTechnology;
import emlab.domain.technology.PowerGridNode;

public interface IntermittentResourceProfileRepository extends
        GraphRepository<IntermittentResourceProfile> {

	@Query(value = "g.v(technology).in('INTERMITTENT_TECHNOLOGY').filter{it.__type__=='emlab.domain.technology.IntermittentResourceProfile'}.as('irp').in('INTERMITTENT_PRODUCTION_NODE').filter{it==g.v(node)}.back('irp').next()", type = QueryType.Gremlin)
	IntermittentResourceProfile findIntermittentResourceProfileByTechnologyAndNode(
	        @Param("technology") PowerGeneratingTechnology technology,
	        @Param("node") PowerGridNode node);

}
