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
package emlab.gen.domain.agent;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import agentspring.agent.Agent;
import emlab.gen.domain.technology.Substance;
import emlab.gen.trend.TimeSeriesImpl;

@NodeEntity
public class CommoditySupplier extends DecarbonizationAgent implements Agent {

    @RelatedTo(type = "SUBSTANCE", elementClass = Substance.class, direction = Direction.OUTGOING)
    private Substance substance;

	@RelatedTo(type = "TREND", elementClass = TimeSeriesImpl.class, direction = Direction.OUTGOING)
	private TimeSeriesImpl priceOfCommodity;

    public Substance getSubstance() {
        return substance;
    }

    public void setSubstance(Substance substance) {
        this.substance = substance;
    }

	public TimeSeriesImpl getPriceOfCommodity() {
        return priceOfCommodity;
    }

	public void setPriceOfCommodity(TimeSeriesImpl priceOfCommodity) {
        this.priceOfCommodity = priceOfCommodity;
    }

    public double getAmountOfCommodity() {
        return Double.MAX_VALUE;
    }
}
