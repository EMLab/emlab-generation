package emlab.repository;

import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;

import emlab.domain.agent.StrategicReserveOperator;

/**
 * 
 * @author pbhagwat
 *
 */
@Repository
public interface StrategicReserveOperatorRepository extends GraphRepository<StrategicReserveOperator>  {

}
