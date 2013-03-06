/*******************************************************************************
 * Copyright 2013 the original author or authors.
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
package emlab.gen.domain;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.PowerGeneratingTechnologyNodeLimit;
import emlab.gen.domain.technology.PowerGridNode;
import emlab.gen.repository.PowerGeneratingTechnologyNodeLimitRepository;

/**
 * @author JCRichstein
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/emlab-gen-test-context.xml" })
@Transactional
public class PowerGeneratingTechnologyNodeLimitTest {

	Logger logger = Logger.getLogger(PowerGeneratingTechnologyNodeLimitTest.class);

	@Autowired
	Neo4jOperations template;

	@Autowired
	PowerGeneratingTechnologyNodeLimitRepository powerGeneratingTechnologyNodeLimitRepository;

	@Test
	public void testPowerGeneratingTechnologyNodeLimitClassFunctionality() {
		PowerGeneratingTechnology lignite = new PowerGeneratingTechnology();
		lignite.persist();
		PowerGridNode node = new PowerGridNode();
		node.persist();
		PowerGeneratingTechnologyNodeLimit ligniteNodeLimit = new PowerGeneratingTechnologyNodeLimit();
		ligniteNodeLimit.setPermanentUpperCapacityLimit(0);
		ligniteNodeLimit.setPowerGeneratingTechnology(lignite);
		ligniteNodeLimit.setPowerGridNode(node);
		ligniteNodeLimit.persist();

		PowerGeneratingTechnologyNodeLimit pgtLimit2 = powerGeneratingTechnologyNodeLimitRepository.findOneByTechnologyAndNode(lignite,
 node);
		
		assertEquals(0, pgtLimit2.getPermanentUpperCapacityLimit(), 0);

	}

}
