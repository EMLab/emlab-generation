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
package emlab.gen.domain.technology;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

/**
 * Class to provide information about PowerGridNode specific capacity limits for
 * PowerGeneratingTechnologies. The investment and dismantlement algorithms are
 * the roles, that explicitly need to keep within the limits.
 * 
 * {@link permanentUpperCapacityLimit} given in MW.
 * 
 * @author JCRichstein
 * 
 */
@NodeEntity
public class PowerGeneratingTechnologyNodeLimit {
	
	@RelatedTo(type = "NODEPGTLIMIT_PGT", elementClass = PowerGeneratingTechnology.class, direction = Direction.OUTGOING)
	private PowerGeneratingTechnology powerGeneratingTechnology;
	
	@RelatedTo(type = "NODEPGTLIMIT_NODE", elementClass = PowerGridNode.class, direction = Direction.OUTGOING)
	private PowerGridNode powerGridNode;

	private double permanentUpperCapacityLimit;

	public PowerGeneratingTechnology getPowerGeneratingTechnology() {
		return powerGeneratingTechnology;
	}

	public void setPowerGeneratingTechnology(PowerGeneratingTechnology powerGeneratingTechnology) {
		this.powerGeneratingTechnology = powerGeneratingTechnology;
	}

	public PowerGridNode getPowerGridNode() {
		return powerGridNode;
	}

	public void setPowerGridNode(PowerGridNode powerGridNode) {
		this.powerGridNode = powerGridNode;
	}

	public double getPermanentUpperCapacityLimit() {
		return permanentUpperCapacityLimit;
	}

	public void setPermanentUpperCapacityLimit(double permanentUpperCapacityLimit) {
		this.permanentUpperCapacityLimit = permanentUpperCapacityLimit;
	}

	/**
	 * Function is time-dependent to enable flexible node limits.
	 */
	public double getUpperCapacityLimit(long time) {
		return getPermanentUpperCapacityLimit();
	}

}
