package emlab.gen.trend;

import org.springframework.data.neo4j.annotation.NodeEntity;

import agentspring.simulation.SimulationParameter;
import agentspring.trend.Trend;

@NodeEntity
public class GeometricTrend extends TimeSeriesImpl implements Trend {

    private double growthRate;

    @SimulationParameter(label = "Initial Value")
    private double start;

    public double getStart() {
        return start;
    }

    public void setStart(double start) {
        this.start = start;
    }

    public double getValue(long time) {
        return (Math.pow((1 + growthRate), time) * getStart());
    }

    public double getGrowthRate() {
        return growthRate;
    }

    public void setGrowthRate(double growthRate) {
        this.growthRate = growthRate;
    }

}
