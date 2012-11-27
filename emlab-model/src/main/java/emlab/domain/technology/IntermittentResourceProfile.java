package emlab.domain.technology;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import emlab.trend.HourlyCSVTimeSeries;

@NodeEntity
public class IntermittentResourceProfile extends HourlyCSVTimeSeries {

    @RelatedTo(type = "INTERMITTENT_TECHNOLOGY", elementClass = PowerGeneratingTechnology.class, direction = Direction.OUTGOING)
    PowerGeneratingTechnology intermittentTechnology;
    
	@RelatedTo(type = "INTERMITTENT_PRODUCTION_NODE", elementClass = PowerGridNode.class, direction = Direction.INCOMING)
	PowerGridNode intermittentProductionNode;

	public PowerGridNode getIntermittentProductionNode() {
		return intermittentProductionNode;
	}

	public void setIntermittentProductionNode(
	        PowerGridNode intermittentProductionNode) {
		this.intermittentProductionNode = intermittentProductionNode;
	}

	public PowerGeneratingTechnology getIntermittentTechnology() {
        return intermittentTechnology;
    }

    public void setIntermittentTechnology(PowerGeneratingTechnology intermittentTechnology) {
        this.intermittentTechnology = intermittentTechnology;
    }

}
