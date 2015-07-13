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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.transaction.annotation.Transactional;

import emlab.gen.domain.agent.Regulator;
import emlab.gen.domain.contract.Contract;
import emlab.gen.repository.Reps;
import emlab.gen.domain.policy.renewablesupport.TenderClearingPoint;

/**
 * @author rjjdejeu
 *
 */
public class OrganizeRenewableTenderPaymentsRole extends Contract {

    @Transient
    @Autowired
    Reps reps;

    @Transient
    @Autowired
    Neo4jTemplate template;

    @Transactional
    public void act(Regulator regulator) {
        
        
        // Retrieve accepted BIDS that are active and update them: 
        if  (startingTimePayments =< endTimePayments) {
        cashflow to investor = cashflow;
        endTimePayments = endTimePayments – 1;
        }

        // keep track of total subsidy spent per year
        double sumOfSubsidy = sum of all cashflow to investor for currentTick()
        

    }
}