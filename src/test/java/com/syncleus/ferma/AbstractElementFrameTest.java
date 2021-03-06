/**
 * Copyright 2004 - 2016 Syncleus, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.syncleus.ferma;

import java.util.function.Function;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import com.google.common.collect.Sets;
import javax.annotation.Nullable;
import java.util.Iterator;

public class AbstractElementFrameTest {

    private FramedGraph fg;
    private Person p1;
    private Knows e1;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        final Graph g = TinkerGraph.open();
        fg = new DelegatingFramedGraph(g);
        p1 = fg.addFramedVertex(Person.DEFAULT_INITIALIZER);
        final Person p2 = fg.addFramedVertex(Person.DEFAULT_INITIALIZER);
        p1.setName("Bryn");
        p2.setName("Julia");
        e1 = p1.addKnows(p2);
        e1.setYears(15);

    }

    @Test
    public void testGetId() {
        Assert.assertEquals((Long) 0L, (Long) p1.getId());
    }

    @Test
    public void testGetPropertyKeys() {
        Assert.assertEquals(Sets.newHashSet("name"), p1.getPropertyKeys());
    }

    @Test
    public void testGetProperty() {
        Assert.assertEquals("Bryn", p1.getProperty("name"));
    }

    @Test
    public void testSetProperty() {
        p1.setProperty("name", "Bryn Cooke");
        Assert.assertEquals("Bryn Cooke", p1.getProperty("name"));
    }

    @Test
    public void testSetPropertyNull() {
        p1.setProperty("name", null);
        Assert.assertNull(p1.getProperty("name"));
    }

    @Test
    public void testV() {
        final Long count = fg.getRawTraversal().V().count().next();
        Assert.assertEquals((Long) 2L, count);
    }

    @Test
    public void testE() {
        final Long count = fg.getRawTraversal().E().count().next();
        Assert.assertEquals((Long) 1L, count);
    }

    @Test
    public void testVById() {
        final Person person = fg.traverse(
            input -> input.V(p1.<Long>getId())).next(Person.class);
        Assert.assertEquals(p1, person);
    }

    @Test
    public void testEById() {
        final Knows knows = fg.traverse((GraphTraversalSource s) -> {return s.E(e1.<Long>getId());}).next(Knows.class);
//        final Knows knows = fg.traverse(new Function<GraphTraversalSource, GraphTraversal<?, ?>>() {
//            @Nullable
//            @Override
//            public GraphTraversal<?, ?> apply(@Nullable final GraphTraversalSource input) {
//                return input.E(e1.<Long>getId());
//            }
//        }).next(Knows.class);
        Assert.assertEquals(e1, knows);
    }

    @Test
    public void testVByIdExplicit() {
        final Person person = fg.traverse(
            input -> input.V(p1.<Long>getId())).nextExplicit(Person.class);
        Assert.assertEquals(p1, person);
    }

    @Test
    public void testEByIdExplicit() {
        final Knows knows = fg.traverse(
            input -> input.E(e1.<Long>getId())).nextExplicit(Knows.class);
        Assert.assertEquals(e1, knows);
    }

    @Test
    public void testRemove() {
        p1.remove();
        final Long count = fg.getRawTraversal().V().count().next();
        Assert.assertEquals((Long) 1L, count);
    }

    @Test
    public void testReframe() {
        final TVertex v1 = p1.reframe(TVertex.class);
        Assert.assertEquals((Long) p1.getId(), (Long) v1.getId());
    }

    @Test
    public void testReframeExplicit() {
        final TVertex v1 = p1.reframeExplicit(TVertex.class);
        Assert.assertEquals((Long) p1.getId(), (Long) v1.getId());
    }

    @Test
    public void testVNull() {
        final Long count = fg.getRawTraversal().V("noId").count().next();
        Assert.assertEquals((Long) 0L, count);
    }

}
