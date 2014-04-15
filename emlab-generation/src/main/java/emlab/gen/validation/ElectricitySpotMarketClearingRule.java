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
package emlab.gen.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;

import agentspring.validation.AbstractValidationRule;
import agentspring.validation.ValidationException;
import agentspring.validation.ValidationRule;
import emlab.gen.domain.market.ClearingPoint;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.Segment;
import emlab.gen.domain.market.electricity.SegmentLoad;
import emlab.gen.repository.ClearingPointRepositoryOld;

public class ElectricitySpotMarketClearingRule extends AbstractValidationRule implements ValidationRule {
    @Autowired
    ClearingPointRepositoryOld clearingPointRepositoryOld;

    @Autowired
    Neo4jTemplate template;

    @Override
    public void validate() {
        for (ElectricitySpotMarket market : template.findAll(ElectricitySpotMarket.class)) {
            for (SegmentLoad segmentload : market.getLoadDurationCurve()) {
                Segment segment = segmentload.getSegment();
                ClearingPoint point = clearingPointRepositoryOld.findClearingPointForSegmentAndTime(segment,
                        getCurrentTick(), false);

                if (point == null) {
                    throw new ValidationException(market.toString() + " " + segment.toString() + " failed to clear");
                }
            }
        }
    }

}
