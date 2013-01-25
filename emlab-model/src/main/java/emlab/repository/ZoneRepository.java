package emlab.repository;


import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;

import emlab.domain.gis.Zone;


@Repository
public interface ZoneRepository extends GraphRepository<Zone> {

	
}
