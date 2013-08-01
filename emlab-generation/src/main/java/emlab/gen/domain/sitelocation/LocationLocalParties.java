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
package emlab.gen.domain.sitelocation;

import org.springframework.data.neo4j.annotation.NodeEntity;

/**
 * Representation of a location
 * 
 * @author jpaling
 * 
 */

@NodeEntity
public class LocationLocalParties {

    private String name;

    private double UtilityLocalParty;

    private double CompensationLocalParty;

    private double factorRandomParty;

    public double getFactorRandomParty() {
        return factorRandomParty;
    }

    public void setFactorRandomParty(double factorRandomParty) {
        this.factorRandomParty = factorRandomParty;
    }

    public double getUtilityLocalParty() {
        return UtilityLocalParty;
    }

    public void setUtilityLocalParty(double utilityLocalParty) {
        UtilityLocalParty = utilityLocalParty;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCompensationLocalParty() {
        return CompensationLocalParty;
    }

    public void setCompensationLocalParty(double compensationLocalParty) {
        CompensationLocalParty = compensationLocalParty;
    }

}
