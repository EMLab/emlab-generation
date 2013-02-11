package emlab.gen.repository;

import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;

import emlab.gen.domain.gis.Zone;

@Repository
public interface ZoneRepository extends GraphRepository<Zone> {

}
