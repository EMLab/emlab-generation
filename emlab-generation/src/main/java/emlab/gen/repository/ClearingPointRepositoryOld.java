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
package emlab.gen.repository;

import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.gremlin.pipes.filter.PropertyFilterPipe;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.filter.FilterPipe;
import com.tinkerpop.pipes.filter.FilterPipe.Filter;
import com.tinkerpop.pipes.util.Pipeline;

import emlab.gen.domain.market.ClearingPoint;
import emlab.gen.domain.market.DecarbonizationMarket;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.market.electricity.SegmentClearingPoint;
import emlab.gen.util.Utils;

/**
 * Repository for {ClearingPoint}s
 * 
 * @author ejlchappin
 * 
 */
@Repository
public class ClearingPointRepositoryOld extends AbstractRepository<ClearingPoint> {

    public ClearingPoint findClearingPointForSegmentAndTime(Segment segment, long time, boolean forecast) {
        Iterator<ClearingPoint> i = findClearingPointsForSegmentAndTime(segment, time, forecast).iterator();
        if (i.hasNext()) {
            return i.next();
        }
        return null;
    }

    public Iterable<ClearingPoint> findClearingPointsForSegmentAndTime(Segment segment, long time, boolean forecast) {
        Pipe<Vertex, Vertex> clearingPointsPipe2 = new LabeledEdgePipe("SEGMENT_POINT", LabeledEdgePipe.Step.IN_OUT);
        // filter by time
        Pipe<Vertex, Vertex> timeFilter = new PropertyFilterPipe<Vertex, Long>("time", time, FilterPipe.Filter.EQUAL);
        Pipe<Vertex, Vertex> forecastFilter = new PropertyFilterPipe<Vertex, Boolean>("forecast", forecast,
                Filter.EQUAL);
        Pipeline<Vertex, Vertex> clearingPoint = new Pipeline<Vertex, Vertex>(clearingPointsPipe2, timeFilter,
                forecastFilter);
        return findAllByPipe(segment, clearingPoint);
    }


    public ClearingPoint findClearingPointForMarketAndTime(DecarbonizationMarket market, long time, boolean forecast) {

        Iterator<ClearingPoint> i = findClearingPointsForMarketAndTime(market, time, forecast).iterator();
        if (i.hasNext()) {
            return i.next();
        }
        return null;
    }


    public Iterable<ClearingPoint> findClearingPointsForMarketAndTime(DecarbonizationMarket market, long time,
            boolean forecast) {
        // TODO: test this
        Pipe<Vertex, Vertex> clearingPoints = new LabeledEdgePipe("MARKET_POINT", LabeledEdgePipe.Step.IN_OUT);
        // filter by time
        Pipe<Vertex, Vertex> timeFilter = new PropertyFilterPipe<Vertex, Long>("time", time, FilterPipe.Filter.EQUAL);
        Pipe<Vertex, Vertex> forecastFilter = new PropertyFilterPipe<Vertex, Boolean>("forecast", forecast,
                Filter.EQUAL);
        Pipeline<Vertex, Vertex> clearingPoint = new Pipeline<Vertex, Vertex>(clearingPoints, timeFilter,
                forecastFilter);

        return findAllByPipe(market, clearingPoint);
    }

    @Transactional
    public ClearingPoint createOrUpdateClearingPoint(DecarbonizationMarket abstractMarket, double price, double volume,
            long time, boolean forecast) {
        ClearingPoint point = null;
        if (findClearingPointsForMarketAndTime(abstractMarket, time, forecast).iterator().hasNext()) {
            point = findClearingPointsForMarketAndTime(abstractMarket, time, forecast).iterator().next();
        } else {
            point = new ClearingPoint().persist();
        }
        point.setAbstractMarket(abstractMarket);
        point.setPrice(price);
        point.setTime(time);
        point.setVolume(volume);
        point.setForecast(forecast);
        return point;
    }

    @Transactional
    public SegmentClearingPoint createOrUpdateSegmentClearingPoint(Segment segment,
            DecarbonizationMarket abstractMarket, double price, double volume, double interconnectorFlow, long time,
            boolean forecast) {
        SegmentClearingPoint point = null;
        // TODO make this a pipe
        List<SegmentClearingPoint> points = Utils.asCastedList(findClearingPointsForMarketAndTime(abstractMarket, time,
                forecast));
        for (SegmentClearingPoint onepoint : points) {
            if (onepoint.getSegment().equals(segment)) {
                point = onepoint;
            }
        }
        if (point == null) {
            point = new SegmentClearingPoint().persist();
        }
        point.setAbstractMarket(abstractMarket);
        point.setPrice(price);
        point.setTime(time);
        point.setVolume(volume);
        point.setSegment(segment);
        point.setForecast(forecast);
        point.setInterconnectorFlow(interconnectorFlow);
        return point;
    }

}
