package emlab.domain.agent;


import org.springframework.data.neo4j.annotation.NodeEntity;

import agentspring.agent.Agent;
import agentspring.simulation.SimulationParameter;
/**
 * 
 * @author pbhagwat
 *
 */

@NodeEntity
public class StrategicReserveOperator extends DecarbonizationAgent implements Agent  {

	public double reserveVolume;
	
	@SimulationParameter(label = "Dispatch price of strategic reserve capacity ", from = 0, to = 20000)
	private double reservePrice;

	@SimulationParameter(label = "Price Mark-Up for strategic reserve capacity (as multiplier)", from = 1, to = 2)
	private double reservePriceMarkUp;
	
	@SimulationParameter(label = "percentage of demand as strategic reserve", from = 0, to = 1)
	private double reserveVolumePercent;

	public double getReserveVolume() {
		return reserveVolume;
	}

	public double setReserveVolume(double reserveVolume) {
		return this.reserveVolume = reserveVolume;
	}

	public double getReservePrice() {
		return reservePrice;
	}

	public void setReservePrice(double reservePrice) {
		this.reservePrice = reservePrice;
	}

	public double getReservePriceMarkUp() {
		return reservePriceMarkUp;
	}

	public void setReservePriceMarkUp(double reservePriceMarkUp) {
		this.reservePriceMarkUp = reservePriceMarkUp;
	}

	public double getReserveVolumePercent() {
		return reserveVolumePercent;
	}

	public void setReserveVolumePercent(double reserveVolumePercent) {
		this.reserveVolumePercent = reserveVolumePercent;
	}

	// cash is already defined in Decarbonization agent
}
