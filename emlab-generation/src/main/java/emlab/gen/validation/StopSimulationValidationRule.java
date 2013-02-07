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

import agentspring.validation.AbstractValidationRule;
import agentspring.validation.ValidationException;
import agentspring.validation.ValidationRule;

public class StopSimulationValidationRule extends AbstractValidationRule implements ValidationRule {

    @Override
    public void validate() {
        if (getCurrentTick() >= 50) {
            throw new ValidationException("Reached tick 50. Enough!");
        }
    }

}
