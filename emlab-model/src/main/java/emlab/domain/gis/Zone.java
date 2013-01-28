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
package emlab.domain.gis;

import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class Zone {

    private String name;
    private boolean isStrategicReserveOperatorDeployed;
    private double reserveVolumePercentSR;
    private double reservePriceSR;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return "Zone " + name;
    }

	public boolean isStrategicReserveOperatorDeployed() {
		return isStrategicReserveOperatorDeployed;
	}

	public void setStrategicReserveOperatorDeployed(
			boolean isStrategicReserveOperatorDeployed) {
		this.isStrategicReserveOperatorDeployed = isStrategicReserveOperatorDeployed;
	}

	public double getReserveVolumePercentSR() {
		return reserveVolumePercentSR;
	}

	public void setReserveVolumePercentSR(double reserveVolumePercent) {
		this.reserveVolumePercentSR = reserveVolumePercent;
	}

	public double getReservePriceSR() {
		return reservePriceSR;
	}

	public void setReservePriceSR(double reservePriceSR) {
		this.reservePriceSR = reservePriceSR;
	}

}
