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
package emlab.gen.repository;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.util.Pipeline;

import emlab.gen.domain.agent.DecarbonizationAgent;
import emlab.gen.domain.agent.EnergyConsumer;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.contract.Contract;
import emlab.gen.domain.contract.LongTermContract;
import emlab.gen.domain.contract.LongTermContractDuration;
import emlab.gen.domain.contract.LongTermContractOffer;
import emlab.gen.domain.contract.LongTermContractType;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.domain.technology.Substance;

@Repository
public class ContractRepository extends AbstractRepository<Contract> {

    static Logger logger = LoggerFactory.getLogger(ContractRepository.class);

    @Autowired
    Neo4jTemplate template;

    public Iterable<LongTermContract> findLongTermContractsForEnergyProducerActiveAtTime(EnergyProducer energyProducer, long time) {
        Pipe<Vertex, Vertex> contractPipe = new LabeledEdgePipe("CONTRACT_FROM", LabeledEdgePipe.Step.IN_OUT);
        // filter by time
        Pipe<Vertex, Vertex> pipeline = new Pipeline<Vertex, Vertex>(contractPipe);

        List<LongTermContract> list = new ArrayList<LongTermContract>();
        // Only if current time is between start and finish time
        for (Contract contract : findAllByPipe(energyProducer, pipeline)) {
            if (contract.getStart() <= time && contract.getFinish() >= time) {
                list.add((LongTermContract) contract);
            }
        }
        return list;
    }

    public Iterable<LongTermContract> findLongTermContractsForEnergyProducerForSegmentActiveAtTime(EnergyProducer energyProducer,
            Segment segment, long time) {

        Pipe<Vertex, Vertex> contractPipe = new LabeledEdgePipe("CONTRACT_FROM", LabeledEdgePipe.Step.IN_OUT);

        Pipe<Vertex, Vertex> pipeline = new Pipeline<Vertex, Vertex>(contractPipe);

        List<LongTermContract> list = new ArrayList<LongTermContract>();
        for (Contract contract : findAllByPipe(energyProducer, pipeline)) {

            // filter by time
            LongTermContract ltc = (LongTermContract) contract;
            if (ltc.getStart() <= time && ltc.getFinish() >= time) {
                if (ltc.getLongTermContractType().getSegments().contains(segment)) {
                    list.add((LongTermContract) contract);
                }
            }
        }
        return list;
    }

    public Iterable<Contract> findLongTermContractsForEnergyConsumerActiveAtTime(EnergyConsumer energyConsumer, long time) {
        Pipe<Vertex, Vertex> contractPipe = new LabeledEdgePipe("CONTRACT_TO", LabeledEdgePipe.Step.IN_OUT);
        // filter by time
        Pipe<Vertex, Vertex> pipeline = new Pipeline<Vertex, Vertex>(contractPipe);

        List<Contract> list = new ArrayList<Contract>();
        // Only if current time is between start and finish time
        for (Contract contract : findAllByPipe(energyConsumer, pipeline)) {
            if (contract.getStart() <= time && contract.getFinish() >= time) {
                list.add(contract);
            }
        }
        return list;
    }

    public Iterable<LongTermContract> findLongTermContractsForEnergyConsumerForSegmentActiveAtTime(EnergyConsumer consumer,
            Segment segment, long time) {

        List<LongTermContract> list = new ArrayList<LongTermContract>();
        for (Contract contract : findLongTermContractsForEnergyConsumerActiveAtTime(consumer, time)) {

            // filter by time
            LongTermContract ltc = (LongTermContract) contract;
            if (ltc.getStart() <= time && ltc.getFinish() >= time) {
                if (ltc.getLongTermContractType().getSegments().contains(segment)) {
                    list.add((LongTermContract) contract);
                }
            }
        }
        return list;
    }

    public LongTermContract findLongTermContractForPowerPlantActiveAtTime(PowerPlant plant, long time) {

        for (Contract c : findAll()) {
            LongTermContract ltc = (LongTermContract) c;
            // It active
            if (ltc.getStart() <= time && ltc.getFinish() >= time) {
                if (ltc.getUnderlyingPowerPlant().equals(plant)) {
                    return ltc;
                }
            }
        }
        return null;
    }

    public Iterable<LongTermContract> findLongTermContractsForEnergyConsumerForSegmentForZoneActiveAtTime(EnergyConsumer consumer,
            Segment segment, Zone zone, long currentTick) {
        List<LongTermContract> list = new ArrayList<LongTermContract>();
        for (LongTermContract ltc : findLongTermContractsForEnergyConsumerForSegmentActiveAtTime(consumer, segment, currentTick)) {
            if (ltc.getZone().equals(zone)) {
                list.add(ltc);
            }
        }
        return list;
    }

    /**
     * Creates a long term contract
     * 
     * @return
     */
    // TODO not transactional, so make it transactional when used.
    public LongTermContract submitLongTermContractForElectricity(PowerPlant plant, DecarbonizationAgent seller, DecarbonizationAgent buyer,
            Zone zone, double price, double capacity, LongTermContractType longTermContractType, long time,
            LongTermContractDuration duration, boolean signed, Substance mainFuel, double fuelPassThroughFactor,
            double co2PassThroughFactor, double fuelPriceStart, double co2PriceStart) {

        LongTermContract contract = new LongTermContract().persist();
        contract.setUnderlyingPowerPlant(plant);
        contract.setFrom(seller);
        contract.setTo(buyer);
        contract.setZone(zone);
        contract.setPricePerUnit(price);
        contract.setCapacity(capacity);
        contract.setLongTermContractType(longTermContractType);
        contract.setStart(time);
        contract.setFinish(time + duration.getDuration() - 1);
        contract.setDuration(duration);
        contract.setSigned(signed);
        contract.setMainFuel(mainFuel);
        contract.setFuelPassThroughFactor(fuelPassThroughFactor);
        contract.setCo2PassThroughFactor(co2PassThroughFactor);
        contract.setFuelPriceStart(fuelPriceStart);
        contract.setCo2PriceStart(co2PriceStart);
        return contract;
    }

    // TODO not transactional, so make it transactional when used.
    public LongTermContractOffer submitLongTermContractOfferForElectricity(EnergyProducer seller, PowerPlant plant, Zone zone,
            double price, double capacity, LongTermContractType longTermContractType, long time, LongTermContractDuration duration,
            Substance mainFuel, double fuelPassThroughFactor, double co2PassThroughFactor, double fuelPriceStart, double co2PriceStart) {

        LongTermContractOffer offer = new LongTermContractOffer().persist();
        offer.setSeller(seller);
        offer.setUnderlyingPowerPlant(plant);
        offer.setZone(zone);
        offer.setPrice(price);
        offer.setCapacity(capacity);
        offer.setLongTermContractType(longTermContractType);
        offer.setStart(time);
        offer.setDuration(duration);
        offer.setMainFuel(mainFuel);
        offer.setFuelPassThroughFactor(fuelPassThroughFactor);
        offer.setCo2PassThroughFactor(co2PassThroughFactor);
        offer.setFuelPriceStart(fuelPriceStart);
        offer.setCo2PriceStart(co2PriceStart);
        return offer;
    }

    @Transactional
    public void removeOffer(LongTermContractOffer offer) {
        offer.remove();
    }

    @Transactional
    public void removeAllOffers() {
        for (LongTermContractOffer offer : template.repositoryFor(LongTermContractOffer.class).findAll()) {
            offer.remove();
        }
    }

    @Transactional
    public void reassignLongTermContractToNewPowerPlant(LongTermContract longTermContract, PowerPlant plant) {
        longTermContract.setUnderlyingPowerPlant(plant);
    }

}
