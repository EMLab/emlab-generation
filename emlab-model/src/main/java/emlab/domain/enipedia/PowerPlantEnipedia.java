package emlab.domain.enipedia;

import java.util.Date;

import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;

import agentspring.lod.LODId;
import agentspring.lod.LODProperty;
import agentspring.lod.LODType;

@NodeEntity
@LODType(type = "Category:Powerplant", limit = "500")
public class PowerPlantEnipedia {

    @LODProperty("http://www.w3.org/2000/01/rdf-schema#label")
    @Indexed
    String name;

    @LODProperty("Property:Primary_fuel_type")
    FuelEnipedia primaryFuel;

    // @LODProperty("Property:Year_built", optional = true)
    Date yearBuilt;

    // For location:
    @LODProperty(value = "Property:Country", optional = true)
    PowerGridNodeEnipedia country;

    @LODProperty(value = "Property:Longitude", optional = true)
    double longitude;

    @LODProperty(value = "Property:Latitude", optional = true)
    double latitude;

    // For capacity and efficiency
    @LODProperty(value = "Property:Generation_capacity_electrical_MW", optional = true)
    double generationCapacity;

    @LODProperty(value = "Property:Operating_efficiency", optional = true)
    double operatingEfficiency;

    @LODProperty(value = "Property:Annual_Energyoutput_MWh", optional = true)
    double energyOutput;

    @LODProperty(value = "Property:Annual_Carbonemissions_kg", optional = true)
    double carbonEmissions;

    @LODId
    @Indexed
    String id;

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getGenerationCapacity() {
        return generationCapacity;
    }

    public void setGenerationCapacity(double generationCapacity) {
        this.generationCapacity = generationCapacity;
    }

    public double getOperatingEfficiency() {
        return operatingEfficiency;
    }

    public void setOperatingEfficiency(double operatingEfficiency) {
        this.operatingEfficiency = operatingEfficiency;
    }

    public double getEnergyOutput() {
        return energyOutput;
    }

    public void setEnergyOutput(double energyOutput) {
        this.energyOutput = energyOutput;
    }

    public double getCarbonEmissions() {
        return carbonEmissions;
    }

    public void setCarbonEmissions(double carbonEmissions) {
        this.carbonEmissions = carbonEmissions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getYearBuilt() {
        return yearBuilt;
    }

    public void setYearBuilt(Date yearBuilt) {
        this.yearBuilt = yearBuilt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FuelEnipedia getPrimaryFuel() {
        return primaryFuel;
    }

    public void setPrimaryFuel(FuelEnipedia primaryFuel) {
        this.primaryFuel = primaryFuel;
    }

    public PowerGridNodeEnipedia getCountry() {
        return country;
    }

    public void setCountry(PowerGridNodeEnipedia country) {
        this.country = country;
    }

}