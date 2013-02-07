/**
 * 
 */
package emlab.gen.domain.contract;

import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.neo4j.graphdb.Direction;


import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.domain.technology.Substance;

/**
 * @author ejlchappin
 * 
 * 
 */
@NodeEntity
public class LongTermContractOffer {

	@RelatedTo(type = "LONGTERMCONTRACTOFFER_POWERPLANT", elementClass = PowerPlant.class, direction = Direction.OUTGOING)
	private PowerPlant underlyingPowerPlant;

	@RelatedTo(type = "LONGTERMCONTRACTOFFER_DURATION", elementClass = LongTermContractDuration.class, direction = Direction.OUTGOING)
	private LongTermContractDuration duration;

	@RelatedTo(type = "LONGTERMCONTRACTOFFER_TYPE", elementClass = LongTermContractType.class, direction = Direction.OUTGOING)
	private LongTermContractType longTermContractType;

	@RelatedTo(type = "LONGTERMCONTRACTOFFER_SELLER", elementClass = EnergyProducer.class, direction = Direction.OUTGOING)
	private EnergyProducer seller;

	@RelatedTo(type = "LONGTERMCONTRACTOFFER_ZONE", elementClass = Zone.class, direction = Direction.OUTGOING)
	private Zone zone;

	private double price;
	private Substance mainFuel;
	private double fuelPassThroughFactor;
	private double co2PassThroughFactor;
	private double fuelPriceStart;
	private double co2PriceStart;
	private double capacity;
	private double start;

	public PowerPlant getUnderlyingPowerPlant() {
		return underlyingPowerPlant;
	}

	public void setUnderlyingPowerPlant(PowerPlant underlyingPowerPlant) {
		this.underlyingPowerPlant = underlyingPowerPlant;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

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

	public LongTermContractType getLongTermContractType() {
		return longTermContractType;
	}

	public void setLongTermContractType(
			LongTermContractType longTermContractType) {
		this.longTermContractType = longTermContractType;
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

	public EnergyProducer getSeller() {
		return seller;
	}

	public void setSeller(EnergyProducer seller) {
		this.seller = seller;
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

	public double getStart() {
		return start;
	}

	public void setStart(double start) {
		this.start = start;
	}
	public LongTermContractDuration getDuration() {
		return duration;
	}

	public void setDuration(LongTermContractDuration duration) {
		this.duration = duration;
	}
	public String toString() {
		return "Offer of " + getSeller() + " for " + getUnderlyingPowerPlant()
				+ " price " + getPrice() + " zone " + getZone() + " capacity " + getCapacity()
				+ " type " + getLongTermContractType() + " start time " + getStart()
				+ " duration " + getDuration() + " fuel price start "
				+ getFuelPriceStart() + " fuel price pass " + getFuelPassThroughFactor()
				+ " co2PriceStart " + getCo2PriceStart() + " co2 pass "
				+ getCo2PassThroughFactor();
	}
}
