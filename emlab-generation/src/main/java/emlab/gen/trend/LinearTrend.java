package emlab.gen.trend;

import org.springframework.data.neo4j.annotation.NodeEntity;

import agentspring.simulation.SimulationParameter;
import agentspring.trend.Trend;

@NodeEntity
public class LinearTrend extends TimeSeriesImpl implements Trend {

    @SimulationParameter(label = "Increment per time step")
    private double increment;
    @SimulationParameter(label = "Initial Value")
    private double start;

    public double getStart() {
        return start;
    }

    public void setStart(double start) {
        this.start = start;
    }

    public double getValue(long time) {
        return ((double) time * increment) + getStart();
    }

    public double getIncrement() {
        return increment;
    }

    public void setIncrement(double increment) {
        this.increment = increment;
    }

}
