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
package emlab.gen.util;

import org.neo4j.graphdb.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.aspects.core.NodeBacked;
import org.springframework.data.neo4j.support.Neo4jTemplate;

import agentspring.facade.Filters;
import agentspring.trend.Trend;

import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jVertex;

import emlab.gen.domain.agent.CommoditySupplier;
import emlab.gen.domain.contract.LongTermContract;
import emlab.gen.domain.market.ClearingPoint;
import emlab.gen.domain.market.DecarbonizationMarket;
import emlab.gen.domain.market.electricity.PowerPlantDispatchPlan;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.domain.technology.Substance;
import emlab.gen.repository.Reps;
import emlab.gen.role.AbstractEnergyProducerRole;

public class FiltersImpl implements Filters {
    @Autowired
    Neo4jTemplate template;

    @Autowired
    Reps reps;

    private Dummy dummy;

    @Override
    public void init() {
        dummy = new Dummy();
    }

    public boolean plantIsOperational(Object node, long tick) {
        NodeBacked entity = this.getEntity(node);
        if (!(entity instanceof PowerPlant))
            throw new RuntimeException("Vertex is not a Power plant");
        PowerPlant plant = (PowerPlant) entity;
        return plant.isOperational(tick);
    }

    public boolean plantIsExpectedToBeOperational(Object node, long tick) {
        NodeBacked entity = this.getEntity(node);
        if (!(entity instanceof PowerPlant))
            throw new RuntimeException("Vertex is not a Power plant");
        PowerPlant plant = (PowerPlant) entity;
        return plant.isExpectedToBeOperational(tick);
    }

    public boolean plantIsInPipeline(Object node, long tick) {
        NodeBacked entity = this.getEntity(node);
        if (!(entity instanceof PowerPlant))
            throw new RuntimeException("Vertex is not a Power plant");
        PowerPlant plant = (PowerPlant) entity;
        return plant.isInPipeline(tick);
    }

    public double calculateCO2Emissions(Object node, long tick) {
        NodeBacked entity = this.getEntity(node);
        if (!(entity instanceof PowerPlant))
            throw new RuntimeException("Vertex is not a Power plant");
        PowerPlant plant = (PowerPlant) entity;
        return plant.calculateCO2EmissionsAtTime(tick, false);
    }

    public double getTrendValue(Object node, long tick) {
        NodeBacked entity = this.getEntity(node);
        if (!(entity instanceof Trend)) {
            throw new RuntimeException("Vertex is not a Trend");
        }
        Trend trend = (Trend) entity;
        return trend.getValue(tick);
    }

    public double findLastKnownPriceOnMarket(Object node, long tick) {
        NodeBacked entity = this.getEntity(node);
        if (!(entity instanceof DecarbonizationMarket)) {
            throw new RuntimeException("Vertex is not a Market");
        }
        DecarbonizationMarket market = (DecarbonizationMarket) entity;

        // Emiliano stuff:
        Double average = calculateAverageMarketPriceBasedOnClearingPoints(reps.clearingPointRepositoryOld
                .findClearingPointsForMarketAndTime(market, tick, false));
        Substance substance = market.getSubstance();

        if (average != null) {
            return average;
        }

        average = calculateAverageMarketPriceBasedOnClearingPoints(reps.clearingPointRepositoryOld.findClearingPointsForMarketAndTime(
                market, tick - 1, false));
        if (average != null) {
            return average;
        }

        if (market.getReferencePrice() > 0) {
            return market.getReferencePrice();
        }

        for (CommoditySupplier supplier : reps.genericRepository.findAll(CommoditySupplier.class)) {
            if (supplier.getSubstance().equals(substance)) {
                return supplier.getPriceOfCommodity().getValue(tick);
            }
        }

        return 0d;
    }

    public double determineProductionOfDispatchPlanInMWh(Object node, long tick) {
        NodeBacked entity = this.getEntity(node);
        if (!(entity instanceof PowerPlantDispatchPlan)) {
            throw new RuntimeException("Vertex is not a Dispatch plan");
        }
        PowerPlantDispatchPlan plan = (PowerPlantDispatchPlan) entity;

        if (tick == plan.getTime()) {
            return plan.getSegment().getLengthInHours()
                    * (plan.getAcceptedAmount() + plan.getCapacityLongTermContract());
        }
        return 0d;
    }

    public double findLastKnownPriceOnMarketInGJ(Object node, long tick) {
        double price = findLastKnownPriceOnMarket(node, tick);

        NodeBacked entity = this.getEntity(node);
        if (!(entity instanceof DecarbonizationMarket)) {
            throw new RuntimeException("Vertex is not a Market");
        }
        DecarbonizationMarket market = (DecarbonizationMarket) entity;

        Substance substance = market.getSubstance();

        return price / substance.getEnergyDensity();
    }

    private Double calculateAverageMarketPriceBasedOnClearingPoints(Iterable<ClearingPoint> clearingPoints) {
        double priceTimesVolume = 0d;
        double volume = 0d;

        for (ClearingPoint point : clearingPoints) {
            priceTimesVolume += point.getPrice() * point.getVolume();
            volume += point.getVolume();
        }
        if (volume > 0) {
            return priceTimesVolume / volume;
        }
        return null;
    }

    private NodeBacked getEntity(Object node) {
        if (!(node instanceof Neo4jVertex))
            throw new RuntimeException("Object is not neo4j vertex");
        Neo4jVertex vertex = (Neo4jVertex) node;
        Node n = vertex.getRawVertex();
        NodeBacked entity = template.createEntityFromStoredType(n);
        return entity;
    }

    public boolean ltcIsActive(Object node, long tick) {
        NodeBacked entity = this.getEntity(node);
        if (!(entity instanceof LongTermContract))
            throw new RuntimeException("Vertex is not a Power plant");
        LongTermContract contract = (LongTermContract) entity;
        if (contract.getStart() <= tick & contract.getFinish() >= tick) {
            return true;
        } else {
            return false;
        }
    }

    private class Dummy extends AbstractEnergyProducerRole {

        public Reps getReps() {
            return reps;
        }

    }
}
