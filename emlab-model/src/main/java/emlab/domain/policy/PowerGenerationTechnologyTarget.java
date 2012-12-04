/*******************************************************************************
 * Copyright 2012 the original author or authors.
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
package emlab.domain.policy;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import emlab.domain.technology.PowerGeneratingTechnology;
import emlab.trend.StepTrend;


/**
 * @author JCRichstein
 *
 */
@NodeEntity
public class PowerGenerationTechnologyTarget {
	
	/**
	 * {@link powerGeneratingTechnology} defines the technology that the target applies to
	 */
	@RelatedTo(type="TARGET_TECHNOLOGY", elementClass=PowerGeneratingTechnology.class, direction=Direction.OUTGOING)
	PowerGeneratingTechnology powerGeneratingTechnology; 
	
	/**
	 * {@link trend} contains the target installed capacity of the technology
	 * in MW.
	 */
	@RelatedTo(type="TARGET_TREND", elementClass=StepTrend.class, direction=Direction.OUTGOING)
	StepTrend trend;
	
	public PowerGeneratingTechnology getPowerGeneratingTechnology() {
		return powerGeneratingTechnology;
	}

	public void setPowerGeneratingTechnology(
			PowerGeneratingTechnology powerGeneratingTechnology) {
		this.powerGeneratingTechnology = powerGeneratingTechnology;
	}

	public StepTrend getTrend() {
		return trend;
	}

	public void setTrend(StepTrend trend) {
		this.trend = trend;
	}

}
