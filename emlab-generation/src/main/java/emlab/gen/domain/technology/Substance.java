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
package emlab.gen.domain.technology;

import org.springframework.data.neo4j.annotation.NodeEntity;

/**
 * Used to describe substances in the simulation. If it refers to a fuel, the
 * basic unit is is MJ. energyDensity, in conjunction with unit can be used to
 * integrate more commonly used quantities in the simulation.
 * 
 * @author JCRichstein
 * 
 */

@NodeEntity
public class Substance {

    private String name;
    private double quality;
    private double energyDensity;
    private double co2Density;
	private String unit;

    public String getName() {
        return name;
    }

    public void setName(String label) {
        this.name = label;
    }

    public double getQuality() {
        return quality;
    }

    public void setQuality(double quality) {
        this.quality = quality;
    }

    public double getEnergyDensity() {
        return energyDensity;
    }

    public void setEnergyDensity(double energyDensity) {
        this.energyDensity = energyDensity;
    }

    public double getCo2Density() {
        return co2Density;
    }

    public void setCo2Density(double co2Density) {
        this.co2Density = co2Density;
    }
    
	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String toString(){
    	return this.getName();
    }
}
