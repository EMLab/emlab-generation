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
package emlab.gen.domain.market.capacity;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import emlab.gen.domain.agent.EnergyConsumer;
import emlab.gen.domain.agent.Regulator;
import emlab.gen.domain.market.DecarbonizationMarket;

/**
 * @author Kaveri Agent Capacity Market for implementation of a Simple Capacity
 *         Market
 * 
 */
@NodeEntity
public class CapacityMarket extends DecarbonizationMarket {

    @RelatedTo(type = "WITH_REGULATOR", elementClass = Regulator.class, direction = Direction.OUTGOING)
    private Regulator regulator;

    @RelatedTo(type = "WITH_CONSUMER", elementClass = EnergyConsumer.class, direction = Direction.OUTGOING)
    private EnergyConsumer consumer;

    public EnergyConsumer getConsumer() {
        return consumer;
    }

    public void setConsumer(EnergyConsumer consumer) {
        this.consumer = consumer;
    }

    public Regulator getRegulator() {
        return regulator;
    }

    public void setRegulator(Regulator regulator) {
        this.regulator = regulator;
    }

}