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

import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import javax.script.ScriptEngine;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.aspects.core.NodeBacked;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.support.Neo4jTemplate;

import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
import com.tinkerpop.pipes.AbstractPipe;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.util.Pipeline;
import com.tinkerpop.pipes.util.SingleIterator;

import emlab.gen.util.Utils;

public abstract class AbstractRepository<T extends NodeBacked> {

    Logger logger = Logger.getLogger(AbstractRepository.class);

    ScriptEngine engine = null;

    @Autowired
    private Neo4jTemplate template;

    private GraphRepository<T> finder() {
        return this.template.repositoryFor(getActualType());
    }

    /*
     * Finder methods
     */

    public Iterable<T> findAllByPropertyValue(String property, Object value) {
        return finder().findAllByPropertyValue(property, value);
    }

    public T findByPropertyValue(String property, Object value) {
        return finder().findByPropertyValue(property, value);
    }

    public Iterable<T> findAll() {
        return finder().findAll();
    }

    public Iterable<T> findAllAtRandom() {
        List<T> list = Utils.asList(findAll());
        Collections.shuffle(list, new Random());
        return list;
    }

    public T findById(long id) {
        return finder().findOne(id);
    }

    public long count() {
        return finder().count();
    }

    public <E extends NodeBacked> Iterable<T> findAllByTraversal(E startNode, TraversalDescription traversalDescription) {
        return finder().findAllByTraversal(startNode, traversalDescription);
    }

    public <E extends NodeBacked> Iterable<T> findAllByPipe(E startNode, Pipe<Vertex, Vertex> pipe) {
        Vertex startVertex = getVertex(startNode);
        Pipe<Vertex, T> typed = new MappingPipe();
        Pipe<Vertex, T> emit = new Pipeline<Vertex, T>(pipe, typed);
        emit.setStarts(new SingleIterator<Vertex>(startVertex));
        return emit;
    }

    @SuppressWarnings("unchecked")
    public Class<T> getActualType() {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<T>) parameterizedType.getActualTypeArguments()[0];
    }

    public <E extends NodeBacked> Vertex getVertex(E e) {
        return (new Neo4jGraph(template.getGraphDatabaseService())).getVertex(e.getNodeId());
    }

    class MappingPipe extends AbstractPipe<Vertex, T> implements Pipe<Vertex, T> {
        @Override
        protected T processNextStart() throws NoSuchElementException {
            Vertex v = this.starts.next();
            return finder().findOne((Long) v.getId());
        }
    }

}
