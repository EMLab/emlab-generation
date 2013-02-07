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
package emlab.gen.domain.market;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import emlab.gen.domain.agent.DecarbonizationAgent;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.technology.Substance;

@NodeEntity
public abstract class DecarbonizationMarket extends DecarbonizationAgent {

    @RelatedTo(type = "SUBSTANCE_MARKET", elementClass = Substance.class, direction = Direction.OUTGOING)
    private Substance substance;

    @RelatedTo(type = "ZONE", elementClass = Zone.class, direction = Direction.OUTGOING)
    private Zone zone;

    private boolean auction;
    private double referencePrice;

    public Substance getSubstance() {
        return substance;
    }

    public void setSubstance(Substance substance) {
        this.substance = substance;
    }

    public void setZone(Zone location) {
        this.zone = location;
    }

    public Zone getZone() {
        return zone;
    }

    public String toString() {
        return this.getName();
    }

    public boolean isAuction() {
        return auction;
    }

    public void setAuction(boolean auction) {
        this.auction = auction;
    }

    public double getReferencePrice() {
        return referencePrice;
    }

    public void setReferencePrice(double referencePrice) {
        this.referencePrice = referencePrice;
    }

}
