/**
 * 
 */
package emlab.gen.domain.contract;

import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.neo4j.graphdb.Direction;


import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.domain.technology.Substance;

/**
 * @author ejlchappin
 *
 *
 */
@NodeEntity
public class LongTermContract extends Contract {

    private double capacity;
    
	@RelatedTo(type = "LONGTERMCONTRACT_TYPE", elementClass = LongTermContractType.class, direction = Direction.OUTGOING)
	private LongTermContractType longTermContractType;
	
	@RelatedTo(type = "LONGTERMCONTRACT_ZONE", elementClass = Zone.class, direction = Direction.OUTGOING)
	private Zone zone;
	
	@RelatedTo(type = "LONGTERMCONTRACT_MAINFUEL", elementClass = Substance.class, direction = Direction.OUTGOING)
	private Substance mainFuel;

	@RelatedTo(type = "LONGTERMCONTRACT_POWERPLANT", elementClass = PowerPlant.class, direction = Direction.OUTGOING)
	private PowerPlant underlyingPowerPlant;

	@RelatedTo(type = "LONGTERMCONTRACT_DURATION", elementClass = LongTermContractDuration.class, direction = Direction.OUTGOING)
	private LongTermContractDuration duration;

	private double fuelPassThroughFactor;
	private double co2PassThroughFactor;
	private double fuelPriceStart;
	private double co2PriceStart;
	
	
	public Substance getMainFuel() {
		return mainFuel;
	}

	public void setMainFuel(Substance mainFuel) {
		this.mainFuel = mainFuel;
	}

	public double getFuelPassThroughFactor() {
		return fuelPassThroughFactor;
	}

	public void setFuelPassThroughFactor(double fuelPassThroughFactor) {
		this.fuelPassThroughFactor = fuelPassThroughFactor;
	}

	public double getCo2PassThroughFactor() {
		return co2PassThroughFactor;
	}

	public void setCo2PassThroughFactor(double co2PassThroughFactor) {
		this.co2PassThroughFactor = co2PassThroughFactor;
	}

	public double getFuelPriceStart() {
		return fuelPriceStart;
	}

	public void setFuelPriceStart(double fuelPriceStart) {
		this.fuelPriceStart = fuelPriceStart;
	}

	public double getCo2PriceStart() {
		return co2PriceStart;
	}

	public void setCo2PriceStart(double co2PriceStart) {
		this.co2PriceStart = co2PriceStart;
	}
	
	public Zone getZone() {
		return zone;
	}

	public void setZone(Zone zone) {
		this.zone = zone;
	}

	public double getCapacity() {
		return capacity;
	}

	public void setCapacity(double capacity) {
		this.capacity = capacity;
	}

	public LongTermContractType getLongTermContractType() {
		return longTermContractType;
	}

	public void setLongTermContractType(LongTermContractType longTermContractType) {
		this.longTermContractType = longTermContractType;
	}
	public PowerPlant getUnderlyingPowerPlant() {
		return underlyingPowerPlant;
	}

	public void setUnderlyingPowerPlant(PowerPlant underlyingPowerPlant) {
		this.underlyingPowerPlant = underlyingPowerPlant;
	}

	public LongTermContractDuration getDuration() {
		return duration;
	}

	public void setDuration(LongTermContractDuration duration) {
		this.duration = duration;
	}


}
