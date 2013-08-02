package emlab.gen.repository;

import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;

import emlab.gen.domain.sitelocation.LocalGovernment;

@Repository
public interface LocalGovernmentRepository extends GraphRepository<LocalGovernment> {

}
