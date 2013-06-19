package emlab.gen.repository;

import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;

import emlab.gen.domain.agent.StrategicReserveOperator;

/**
 * 
 * @author pbhagwat
 * 
 */
@Repository
public interface StrategicReserveOperatorRepository extends
GraphRepository<StrategicReserveOperator> {

}
