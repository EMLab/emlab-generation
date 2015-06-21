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
package emlab.gen.domain.policy.renewablesupport;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.RelatedTo;

import emlab.gen.domain.contract.Contract;
import emlab.gen.domain.technology.PowerPlant;

/**
 * @author Kaveri3012 this class is meant to specify a support price for a given
 *         duration. The price may have been generated either from a quantity
 *         based scheme or a price based scheme.
 */
public class SupportPriceContract extends Contract {

    @RelatedTo(type = "FOR_SUPPORT_SCHEME", elementClass = RenewableSupportScheme.class, direction = Direction.OUTGOING)
    private RenewableSupportScheme renewableSupportScheme;

    @RelatedTo(type = "FOR_POWER_PLANT", elementClass = PowerPlant.class, direction = Direction.OUTGOING)
    private PowerPlant plant;

    public RenewableSupportScheme getRenewableSupportScheme() {
        return renewableSupportScheme;
    }

    public void setRenewableSupportScheme(RenewableSupportScheme renewableSupportScheme) {
        this.renewableSupportScheme = renewableSupportScheme;
    }

    public PowerPlant getPlant() {
        return plant;
    }

    public void setPlant(PowerPlant plant) {
        this.plant = plant;
    }

}
