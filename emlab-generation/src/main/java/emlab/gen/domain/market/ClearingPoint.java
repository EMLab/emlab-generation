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
package emlab.gen.domain.market;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spot market clearing point
 * 
 * @author alfredas&emile
 * 
 */
@NodeEntity
public class ClearingPoint {

    @RelatedTo(type = "MARKET_POINT", elementClass = DecarbonizationMarket.class, direction = Direction.OUTGOING)
    DecarbonizationMarket abstractMarket;

    private double price;
    private double volume;
    private long time;
    private boolean forecast;

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public DecarbonizationMarket getAbstractMarket() {
        return abstractMarket;
    }

    public void setAbstractMarket(DecarbonizationMarket abstractMarket) {
        this.abstractMarket = abstractMarket;
    }

    @Override
    public String toString() {
        return " market: " + abstractMarket + ", price " + price + ", volume " + volume + ", time " + time;
    }

    @Transactional
    public void updateAbstractMarket(DecarbonizationMarket market) {
        setAbstractMarket(market);
    }

    public boolean isForecast() {
        return forecast;
    }

    public void setForecast(boolean forecast) {
        this.forecast = forecast;
    }
}
