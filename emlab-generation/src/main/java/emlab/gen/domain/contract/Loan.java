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
package emlab.gen.domain.contract;


import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.neo4j.graphdb.Direction;

import emlab.gen.domain.agent.DecarbonizationAgent;
import emlab.gen.domain.technology.PowerPlant;

@NodeEntity
public class Loan {

    @RelatedTo(type = "LEND_TO_AGENT", elementClass = DecarbonizationAgent.class, direction = Direction.OUTGOING)
    private DecarbonizationAgent from;

    @RelatedTo(type = "LEND_BY_AGENT", elementClass = DecarbonizationAgent.class, direction = Direction.OUTGOING)
    private DecarbonizationAgent to;

    @RelatedTo(type = "REGARDING_POWERPLANT", elementClass = PowerPlant.class, direction = Direction.OUTGOING)
    private PowerPlant regardingPowerPlant;

    private double amountPerPayment;
    private long totalNumberOfPayments;
    private long numberOfPaymentsDone;
    private long loanStartTime;

    public long getLoanStartTime() {
        return loanStartTime;
    }

    public void setLoanStartTime(long loanStartTime) {
        this.loanStartTime = loanStartTime;
    }

    public long getTotalNumberOfPayments() {
		return totalNumberOfPayments;
	}

	public double getAmountPerPayment() {
		return amountPerPayment;
	}

	public void setAmountPerPayment(double amountPerPayment) {
		this.amountPerPayment = amountPerPayment;
	}

	public void setTotalNumberOfPayments(long totalNumberOfPayments) {
		this.totalNumberOfPayments = totalNumberOfPayments;
	}

	public long getNumberOfPaymentsDone() {
		return numberOfPaymentsDone;
	}

	public void setNumberOfPaymentsDone(long numberOfPaymentsDone) {
		this.numberOfPaymentsDone = numberOfPaymentsDone;
	}

	public DecarbonizationAgent getFrom() {
        return from;
    }

    public void setFrom(DecarbonizationAgent from) {
        this.from = from;
    }

    public DecarbonizationAgent getTo() {
        return to;
    }

    public void setTo(DecarbonizationAgent to) {
        this.to = to;
    }

    public PowerPlant getRegardingPowerPlant() {
        return regardingPowerPlant;
    }

    public void setRegardingPowerPlant(PowerPlant regardingPowerPlant) {
        this.regardingPowerPlant = regardingPowerPlant;
    }
}
