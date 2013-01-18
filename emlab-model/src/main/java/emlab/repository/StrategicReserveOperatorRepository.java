

package emlab.repository;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.annotation.QueryType;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import emlab.domain.agent.StrategicReserveOperator;
import emlab.domain.market.electricity.ElectricitySpotMarket;

public interface StrategicReserveOperatorRepository extends GraphRepository<StrategicReserveOperator>  {


	


}
