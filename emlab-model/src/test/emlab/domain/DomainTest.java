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
package emlab.domain;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

//import emlab.domain.agent.Person;
import emlab.domain.agent.DecarbonizationAgent;
import emlab.domain.market.electricity.Segment;
import emlab.repository.SegmentRepository;

import static org.junit.Assert.*;

/**
 * @author JCRichstein
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/emlab-test-context.xml"})
@Transactional
public class DomainTest {

	@Autowired Neo4jOperations template;

	@Autowired
	SegmentRepository segmentRepository;
	
    @Before
    @Transactional
    public void setUp() throws Exception {
    	
    	

    }
    
    @Test
    public void createNodesWithTemplateSaveAndCheckNumber(){
    	
    	DecarbonizationAgent DecarbonizationAgent1 = new DecarbonizationAgent();
    	
    	template.save(DecarbonizationAgent1);
    	
    	assertEquals("Check if stuff are equal if template.createNodeAs: ", 1, template.count(DecarbonizationAgent.class));
    	
    }
    
    @Test
    public void findsTheExpectedNumberofSegments(){
    	
    	Segment segment1 = new Segment();
    	Segment segment2 = new Segment();
    	template.save(segment1);
    	template.save(segment2);
    	assertEquals("Check if segments are equal if template.save: ", 2, template.count(Segment.class));
    	assertEquals("Check if segments are equal with segmentRepository if template.createNodeAs: ", 2, segmentRepository.count());
    }
    
    

}	
