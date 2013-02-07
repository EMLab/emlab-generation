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

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import javax.script.ScriptEngine;

import org.neo4j.graphdb.traversal.TraversalDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.aspects.core.NodeBacked;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.stereotype.Repository;

import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
import com.tinkerpop.pipes.AbstractPipe;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.util.Pipeline;
import com.tinkerpop.pipes.util.SingleIterator;

import emlab.gen.util.Utils;

@Repository
public class GenericRepository {

    Logger logger = LoggerFactory.getLogger(GenericRepository.class);;

    ScriptEngine engine = null;

    @Autowired
    Neo4jTemplate template;

    private <T extends NodeBacked> GraphRepository<T> finder(Class<T> clazz) {
        return this.template.repositoryFor(clazz);
    }

    public <T extends NodeBacked> Iterable<T> findAllByPropertyValue(Class<T> clazz, String property, Object value) {
        return finder(clazz).findAllByPropertyValue(property, value);
    }

    public <T extends NodeBacked> T findByPropertyValue(Class<T> clazz, String property, Object value) {
        return finder(clazz).findByPropertyValue(property, value);
    }

    public <T extends NodeBacked> Iterable<T> findAll(Class<T> clazz) {
        return finder(clazz).findAll();
    }

    public <T extends NodeBacked> Iterable<T> findAllAtRandom(Class<T> clazz) {
        List<T> list = Utils.asList(findAll(clazz));
        Collections.shuffle(list, new Random());
        return list;
    }

    public <T extends NodeBacked> T findOneAtRandom(Class<T> clazz) {
        List<T> list = Utils.asList(findAll(clazz));
        Collections.shuffle(list, new Random());
        return list.get(0);
    }

    public <T extends NodeBacked> T findFirst(Class<T> clazz) {
        if (finder(clazz).findAll().iterator().hasNext()) {
            return finder(clazz).findAll().iterator().next();
        } else {
            return null;
        }
    }

    public <T extends NodeBacked> T findById(Class<T> clazz, long id) {
        return finder(clazz).findOne(id);
    }

    public <T extends NodeBacked, E extends NodeBacked> Iterable<T> findAllByTraversal(Class<T> clazz, E startNode,
            TraversalDescription traversalDescription) {
        return finder(clazz).findAllByTraversal(startNode, traversalDescription);
    }

    public <T extends NodeBacked, E extends NodeBacked> Iterable<T> findAllByPipe(Class<T> clazz, E startNode, Pipe<Vertex, Vertex> pipe) {
        Vertex startVertex = getVertex(startNode);
        Pipe<Vertex, T> typed = new MappingPipe<T>(clazz);
        Pipe<Vertex, T> emit = new Pipeline<Vertex, T>(pipe, typed);
        emit.setStarts(new SingleIterator<Vertex>(startVertex));
        return emit;
    }

    public <T extends NodeBacked> Vertex getVertex(T e) {
        return (new Neo4jGraph(template.getGraphDatabaseService())).getVertex(e.getNodeId());
    }

    class MappingPipe<T extends NodeBacked> extends AbstractPipe<Vertex, T> implements Pipe<Vertex, T> {

        Class<T> genericClass;

        public MappingPipe(Class<T> clazz) {
            super();
            genericClass = clazz;
        }

        @Override
        protected T processNextStart() throws NoSuchElementException {
            Vertex v = this.starts.next();
            return findById(genericClass, (Long) v.getId());
        }

    }

}
