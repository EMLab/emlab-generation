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
package emlab.gen.role.tender;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.transaction.annotation.Transactional;

import emlab.gen.domain.agent.Regulator;
import emlab.gen.domain.contract.CashFlow;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.PowerPlantDispatchPlan;
import emlab.gen.domain.market.electricity.SegmentLoad;
import emlab.gen.domain.policy.renewablesupport.RenewableSupportScheme;
import emlab.gen.domain.policy.renewablesupport.SupportPriceContract;
import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.repository.Reps;

/**
 * @author rjjdejeu
 *
 */
public class OrganizingPaymentsFromTenderRole extends RenewableSupportScheme {
    
    @Transient
    @Autowired
    Reps reps;

    @Transient
    @Autowired
    Neo4jTemplate template;

    @Transactional
    public void act(Regulator regulator) {

        SupportPriceContract contract = null;

        //if
        if (investor.getbidPrice <= getClearingPoint.getCurrentTick()) {
                    contract = new SupportPriceContract();
                    contract.setStart(getCurrentTick());
                }

//        IF (bidPrice of investor < clearingPrice ) {
//
//            Contract contract = new Contract();
//            
//            contractLength = 15;
//            contractPrice = clearingPrice of current tick;
//            contractQuantity = bidQuantity;
//            yearlyContractPayment = contractPrice * contractQuantity;
            
        }

        }
    }
}

}
