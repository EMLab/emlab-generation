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

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import emlab.gen.domain.agent.TargetInvestor;
import emlab.gen.domain.market.ClearingPoint;
import emlab.gen.domain.market.CommodityMarket;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.policy.PowerGeneratingTechnologyTarget;
import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.Substance;

/**
 * @author JCRichstein
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/emlab-gen-test-context.xml"})
@Transactional
public class RepositoryTesting {


    Logger logger = Logger.getLogger(RepositoryTesting.class);


    //------- clearingPointRepository ---------
    @Autowired ClearingPointRepository clearingPointRepository;

    @Test
    public void testfindAllClearingPointsForSubstanceTradedOnCommodityMarkesAndTimeRange(){
        double[][] input = {{0,1},{1,1.1},{2,1.21},{3,1.331},{4,1.4641}};
        Substance substance = new Substance();
        substance.persist();
        CommodityMarket market = new CommodityMarket();
        market.setSubstance(substance);
        market.persist();
        Map<Integer, Double> inputMap = new HashMap();
        for (double[] d : input) {
            ClearingPoint cp = new ClearingPoint();
            cp.setTime((long) d[0]);
            cp.setPrice(d[1]);
            cp.setAbstractMarket(market);
            cp.setForecast(false);
            cp.persist();
            inputMap.put(new Integer((int) d[0]), d[1]);
        }

        //Testing selection of only first one, starting with negative value
        Iterable<ClearingPoint> cps = clearingPointRepository
                .findAllClearingPointsForSubstanceTradedOnCommodityMarkesAndTimeRange(substance, -2l, 0l, false);
        assertTrue(cps.iterator().next().getPrice() == 1);

        cps = clearingPointRepository.findAllClearingPointsForSubstanceTradedOnCommodityMarkesAndTimeRange(substance,
                -2l, 4l, false);
        for(ClearingPoint cp : cps){
            assertTrue(cp.getPrice() == inputMap.get(new Integer((int) cp.getTime())));
            //logger.warn(new Double(cp.getPrice()).toString() + "==" + inputMap.get(new Integer((int) cp.getTime())).toString());
        }
    }

    //SubstanceRepository
    @Autowired SubstanceRepository substanceRepository;

    @Test
    public void testfindAllSubstancesTradedOnCommodityMarkets(){
        Substance coal = new Substance();
        coal.setName("Coal");
        coal.persist();
        Substance co2 = new Substance();
        co2.persist();
        CommodityMarket market = new CommodityMarket();
        market.setSubstance(coal);
        market.persist();

        Iterable<Substance> substancesInDB = substanceRepository.findAllSubstancesTradedOnCommodityMarkets();
        int count = 0;
        for(Substance substance : substancesInDB){
            count++;
            assertTrue(substance.getName().equals("Coal"));
        }
        assertTrue(count == 1);
    }

    //PowerGenerationTechnologyTargetRepository
    @Autowired PowerGenerationTechnologyTargetRepository powerGenerationTechnologyTargetRepository;

    @Test
    public void testfindAllPowerGenerationTechnologyTargetsByMarket(){
        PowerGeneratingTechnology wind = new PowerGeneratingTechnology();
        wind.persist();
        ElectricitySpotMarket marketA = new ElectricitySpotMarket();
        marketA.persist();
        ElectricitySpotMarket marketB = new ElectricitySpotMarket();
        marketB.persist();


        PowerGeneratingTechnologyTarget pgttWindA = new PowerGeneratingTechnologyTarget();
        pgttWindA.setPowerGeneratingTechnology(wind);
        pgttWindA.persist();

        PowerGeneratingTechnologyTarget pggtWindB = new PowerGeneratingTechnologyTarget();
        pggtWindB.setPowerGeneratingTechnology(wind);
        pggtWindB.persist();

        TargetInvestor rtiA = new TargetInvestor();
        rtiA.setInvestorMarket(marketA);
        Set<PowerGeneratingTechnologyTarget> powerGenerationTechnologyTargetsA = new HashSet<PowerGeneratingTechnologyTarget>();
        powerGenerationTechnologyTargetsA.add(pgttWindA);
        rtiA.setPowerGenerationTechnologyTargets(powerGenerationTechnologyTargetsA);
        rtiA.persist();

        TargetInvestor rtiB = new TargetInvestor();
        rtiB.setInvestorMarket(marketB);
        Set<PowerGeneratingTechnologyTarget> powerGenerationTechnologyTargetsB = new HashSet<PowerGeneratingTechnologyTarget>();
        powerGenerationTechnologyTargetsB.add(pggtWindB);
        rtiB.setPowerGenerationTechnologyTargets(powerGenerationTechnologyTargetsB);
        rtiB.persist();

        assertTrue(pgttWindA.getNodeId()==powerGenerationTechnologyTargetRepository.findAllByMarket(marketA).iterator().next().getNodeId());

    }



}
