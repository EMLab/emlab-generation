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
import emlab.gen.domain.gis.Zone;
import emlab.gen.trend.TimeSeriesImpl;

@NodeEntity
public class NationalGovernment extends DecarbonizationAgent implements Agent {

    @RelatedTo(type = "GOVERNED_ZONE", elementClass = Zone.class, direction = Direction.OUTGOING)
    private Zone governedZone;

	@RelatedTo(type = "MINCO2PRICE_TREND", elementClass = TimeSeriesImpl.class, direction = Direction.OUTGOING)
	private TimeSeriesImpl minNationalCo2PriceTrend;

	public TimeSeriesImpl getMinNationalCo2PriceTrend() {
        return minNationalCo2PriceTrend;
    }

	public void setMinNationalCo2PriceTrend(TimeSeriesImpl minNationalCo2PriceTrend) {
        this.minNationalCo2PriceTrend = minNationalCo2PriceTrend;
    }

    public Zone getGovernedZone() {
		return governedZone;
	}

	public void setGovernedZone(Zone governedZone) {
		this.governedZone = governedZone;
	}

	public double getPaymentEffectivePriceDifferenceBetweenNationalMinPriceAndGivenMarketPrice(double marketPrice, long tick) {
        if (minNationalCo2PriceTrend.getValue(tick) > marketPrice) {
            return (minNationalCo2PriceTrend.getValue(tick) - marketPrice);
        } else {
            return 0d;
        }
    }

}
