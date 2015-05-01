/******************************************************************************
 *                                                                             *
 *  Copyright: (c) Syncleus, Inc.                                              *
 *                                                                             *
 *  You may redistribute and modify this source code under the terms and       *
 *  conditions of the Open Source Community License - Type C version 1.0       *
 *  or any later version as published by Syncleus, Inc. at www.syncleus.com.   *
 *  There should be a copy of the license included with this file. If a copy   *
 *  of the license is not included you are granted no right to distribute or   *
 *  otherwise use this file except through a legal and valid license. You      *
 *  should also contact Syncleus, Inc. at the information below if you cannot  *
 *  find a license:                                                            *
 *                                                                             *
 *  Syncleus, Inc.                                                             *
 *  2604 South 12th Street                                                     *
 *  Philadelphia, PA 19148                                                     *
 *                                                                             *
 ******************************************************************************/

/*
 * Part or all of this source file was forked from a third-party project, the details of which are listed below.
 *
 * Source Project: Totorom
 * Source URL: https://github.com/BrynCooke/totorom
 * Source License: Apache Public License v2.0
 * When: November, 20th 2014
 */
package com.syncleus.ferma;

import com.syncleus.ferma.traversals.GlobalVertexTraversal;
import com.syncleus.ferma.traversals.SimpleTraversal;
import com.syncleus.ferma.traversals.VertexTraversal;
import com.syncleus.ferma.traversals.EdgeTraversal;
import com.syncleus.ferma.framefactories.FrameFactory;
import com.syncleus.ferma.framefactories.DefaultFrameFactory;
import com.syncleus.ferma.typeresolvers.UntypedTypeResolver;
import com.syncleus.ferma.typeresolvers.TypeResolver;
import com.syncleus.ferma.typeresolvers.PolymorphicTypeResolver;
import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.syncleus.ferma.framefactories.annotation.AnnotationFrameFactory;
import com.tinkerpop.blueprints.*;
import com.tinkerpop.blueprints.util.wrappers.WrapperGraph;

import java.util.Collection;
import java.util.Iterator;

public class DelegatingFramedGraph<G extends Graph> implements WrapperFramedGraph<G> {

    private final G delegate;
    private final TypeResolver defaultResolver;
    private final TypeResolver untypedResolver;
    private final FrameFactory builder;
    private final ReflectionCache reflections;

    /**
     * Construct a framed graph.
     *
     * @param delegate
     *            The graph to wrap.
     * @param builder
     *            The builder that will construct frames.
     * @param defaultResolver
     *            The type defaultResolver that will decide the final frame type.
     */
    public DelegatingFramedGraph(final G delegate, final FrameFactory builder, final TypeResolver defaultResolver) {
        this.reflections = null;
        this.delegate = delegate;
        this.defaultResolver = defaultResolver;
        this.untypedResolver = new UntypedTypeResolver();
        this.builder = builder;
    }

    /**
     * Construct an untyped framed graph without annotation support
     *
     * @param delegate
     *            The graph to wrap.
     */
    public DelegatingFramedGraph(final G delegate) {
        this.reflections = new ReflectionCache();
        this.delegate = delegate;
        this.defaultResolver = new UntypedTypeResolver();
        this.untypedResolver = this.defaultResolver;
        this.builder = new DefaultFrameFactory();
    }

    /**
     * Construct an untyped framed graph without annotation support
     *
     * @param reflections
     * 			  A RefelctionCache used to determine reflection and hierarchy of classes.
     * @param delegate
     *            The graph to wrap.
     */
    public DelegatingFramedGraph(final G delegate, final ReflectionCache reflections) {
        this.reflections = reflections;
        this.delegate = delegate;
        this.defaultResolver = new UntypedTypeResolver();
        this.untypedResolver = this.defaultResolver;
        this.builder = new DefaultFrameFactory();
    }

    /**
     * Construct a framed graph without annotation support.
     *
     * @param delegate
     *            The graph to wrap.
     * @param defaultResolver
     *            The type defaultResolver that will decide the final frame type.
     */
    public DelegatingFramedGraph(final G delegate, final TypeResolver defaultResolver) {
        this(delegate, new DefaultFrameFactory(), defaultResolver);
    }

    /**
     * Construct a framed graph with the specified typeResolution and annotation support
     *
     * @param delegate
     *            The graph to wrap.
     * @param typeResolution
     * 			  True if type resolution is to be automatically handled by default, false causes explicit typing by
     * @param annotationsSupported
     * 			  True if annotated classes will be supported, false otherwise.
     */
    public DelegatingFramedGraph(final G delegate, final boolean typeResolution, final boolean annotationsSupported) {
        this.reflections = new ReflectionCache();
        this.delegate = delegate;
        if (typeResolution) {
            this.defaultResolver = new PolymorphicTypeResolver(this.reflections);
            this.untypedResolver = new UntypedTypeResolver();
        }
        else {
            this.defaultResolver = new UntypedTypeResolver();
            this.untypedResolver = this.defaultResolver;
        }
        if (annotationsSupported)
            this.builder = new AnnotationFrameFactory(this.reflections);
        else
            this.builder = new DefaultFrameFactory();
    }

    /**
     * Construct a framed graph with the specified typeResolution and annotation support
     *
     * @param delegate
     *            The graph to wrap.
     * @param reflections
     * 			  A RefelctionCache used to determine reflection and hierarchy of classes.
     * @param typeResolution
     * 			  True if type resolution is to be automatically handled by default, false causes explicit typing by
     * @param annotationsSupported
     * 			  True if annotated classes will be supported, false otherwise.
     */
    public DelegatingFramedGraph(final G delegate, final ReflectionCache reflections, final boolean typeResolution, final boolean annotationsSupported) {
        this.reflections = reflections;
        this.delegate = delegate;
        if (typeResolution) {
            this.defaultResolver = new PolymorphicTypeResolver(this.reflections);
            this.untypedResolver = new UntypedTypeResolver();
        }
        else {
            this.defaultResolver = new UntypedTypeResolver();
            this.untypedResolver = this.defaultResolver;
        }
        if (annotationsSupported)
            this.builder = new AnnotationFrameFactory(this.reflections);
        else
            this.builder = new DefaultFrameFactory();
    }

    /**
     * Construct a Typed framed graph with the specified type resolution and with annotation support
     *
     * @param delegate
     *            The graph to wrap.
     * @param types
     *            The types to be consider for type resolution.
     */
    public DelegatingFramedGraph(final G delegate, final Collection<? extends Class<?>> types) {
        this.reflections = new ReflectionCache(types);
        this.delegate = delegate;
        this.defaultResolver = new PolymorphicTypeResolver(this.reflections);
        this.untypedResolver = new UntypedTypeResolver();
        this.builder = new AnnotationFrameFactory(this.reflections);
    }

    /**
     * Construct an framed graph with the specified type resolution and with annotation support
     *
     * @param delegate
     *            The graph to wrap.
     * @param typeResolution
     * 			  True if type resolution is to be automatically handled by default, false causes explicit typing by
     * 			  default.
     * @param types
     *            The types to be consider for type resolution.
     */
    public DelegatingFramedGraph(final G delegate, final boolean typeResolution, final Collection<? extends Class<?>> types) {
        this.reflections = new ReflectionCache(types);
        this.delegate = delegate;
        if (typeResolution) {
            this.defaultResolver = new PolymorphicTypeResolver(this.reflections);
            this.untypedResolver = new UntypedTypeResolver();
        }
        else {
            this.defaultResolver = new UntypedTypeResolver();
            this.untypedResolver = this.defaultResolver;
        }
        this.builder = new AnnotationFrameFactory(this.reflections);
    }

    @Override
    public G getBaseGraph() {
        return this.delegate;
    }

    @Override
    public Transaction tx() {
        if (delegate instanceof TransactionalGraph)
            return new Transaction((TransactionalGraph) delegate);
        else
            return new Transaction(null);
    }

    /**
     * Close the delegate graph.
     */
    @Override
    public void close() {
        delegate.shutdown();
    }

    @Override
    public <T> T frameElement(final Element e, final Class<T> kind ){
        if (e == null)
            return null;

        final Class<? extends T> frameType = (kind == TVertex.class || kind == TEdge.class) ? kind : defaultResolver.resolve(e, kind);

        final T frame = builder.create(e, frameType);
        ((AbstractElementFrame) frame).init(this, e);
        return frame;
    }

    @Override
    public <T> T frameNewElement(final Element e, final ClassInitializer<T> initializer) {
        final T frame = frameElement(e, initializer.getInitializationType());
        defaultResolver.init(e, initializer.getInitializationType());
        ((AbstractElementFrame) frame).init();
        initializer.initalize(frame);
        return frame;
    }
    
    @Override
    public <T> T frameNewElement(final Element e, final Class<T> kind) {
        return this.frameNewElement(e, new DefaultClassInitializer<>(kind));
    }

    @Override
    public <T> Iterator<? extends T> frame(final Iterator<? extends Element> pipeline, final Class<T> kind) {
        return Iterators.transform(pipeline, new Function<Element, T>() {

            @Override
            public T apply(final Element input) {
                return frameElement(input, kind);
            }

        });
    }

    @Override
    public <T> T frameElementExplicit(final Element e, final Class<T> kind) {
        if (e == null)
            return null;

        final Class<? extends T> frameType = this.untypedResolver.resolve(e, kind);

        final T frame = builder.create(e, frameType);
        ((AbstractElementFrame) frame).init(this, e);
        return frame;
    }

    @Override
    public <T> T frameNewElementExplicit(final Element e, final ClassInitializer<T> initializer) {
        final T frame = frameElement(e, initializer.getInitializationType());
        this.untypedResolver.init(e, initializer.getInitializationType());
        ((AbstractElementFrame) frame).init();
        initializer.initalize(frame);
        return frame;
    }
    
    @Override
    public <T> T frameNewElementExplicit(final Element e, final Class<T> kind) {
        return this.frameNewElementExplicit(e, new DefaultClassInitializer<>(kind));
    }

    @Override
    public <T> Iterator<? extends T> frameExplicit(final Iterator<? extends Element> pipeline, final Class<T> kind) {
        return Iterators.transform(pipeline, new Function<Element, T>() {

            @Override
            public T apply(final Element input) {
                return frameElementExplicit(input, kind);
            }

        });
    }

    @Override
    public <T> T addFramedVertex(final ClassInitializer<T> initializer) {
        final T framedVertex = frameNewElement(delegate.addVertex(null), initializer);
        return framedVertex;
    }
    
    @Override
    public <T> T addFramedVertex(final Class<T> kind) {
        return this.addFramedVertex(new DefaultClassInitializer<>(kind));
    }

    @Override
    public <T> T addFramedVertexExplicit(final ClassInitializer<T> initializer) {
        final T framedVertex = frameNewElementExplicit(delegate.addVertex(null), initializer);
        return framedVertex;
    }
    
    @Override
    public <T> T addFramedVertexExplicit(final Class<T> kind) {
        return this.addFramedVertexExplicit(new DefaultClassInitializer<>(kind));
    }

    @Override
    public TVertex addFramedVertex() {

        return addFramedVertex(TVertex.DEFAULT_INITIALIZER);
    }

    @Override
    public TVertex addFramedVertexExplicit() {

        return addFramedVertexExplicit(TVertex.DEFAULT_INITIALIZER);
    }

    @Override
    public <T> T addFramedEdge(final VertexFrame source, final VertexFrame destination, final String label, final ClassInitializer<T> initializer) {
        final T framedEdge = frameNewElement(this.delegate.addEdge(null, source.getElement(), destination.getElement(), label), initializer);
        return framedEdge;
    }
    
    @Override
    public <T> T addFramedEdge(final VertexFrame source, final VertexFrame destination, final String label, final Class<T> kind) {
        return this.addFramedEdge(source, destination, label, new DefaultClassInitializer<>(kind));
    }

    @Override
    public <T> T addFramedEdgeExplicit(final VertexFrame source, final VertexFrame destination, final String label, final ClassInitializer<T> initializer) {
        final T framedEdge = frameNewElementExplicit(this.delegate.addEdge(null, source.getElement(), destination.getElement(), label), initializer);
        return framedEdge;
    }
    
    @Override
    public <T> T addFramedEdgeExplicit(final VertexFrame source, final VertexFrame destination, final String label, final Class<T> kind) {
        return this.addFramedEdgeExplicit(source, destination, label, new DefaultClassInitializer<>(kind));
    }

    @Override
    public TEdge addFramedEdge(final VertexFrame source, final VertexFrame destination, final String label) {

        return addFramedEdge(source, destination, label, TEdge.DEFAULT_INITIALIZER);
    }

    @Override
    public TEdge addFramedEdgeExplicit(final VertexFrame source, final VertexFrame destination, final String label) {

        return addFramedEdgeExplicit(source, destination, label, TEdge.DEFAULT_INITIALIZER);
    }

    @Override
    public VertexTraversal<?, ?, ?> v() {
        return new GlobalVertexTraversal(this, delegate);
    }

    @Override
    public EdgeTraversal<?, ?, ?> e() {
        return new SimpleTraversal(this, delegate).e();
    }

    @Override
    public <F> Iterable<? extends F> getFramedVertices(final Class<F> kind) {
        return new FramingVertexIterable<>(this, this.getVertices(), kind);
    }

    @Override
    public <F> Iterable<? extends F> getFramedVertices(final String key, final Object value, final Class<F> kind) {
        return new FramingVertexIterable<>(this, this.getVertices(key, value), kind);
    }

    @Override
    public <F> Iterable<? extends F> getFramedVerticesExplicit(final Class<F> kind) {
        return new FramingVertexIterable<>(this, this.getVertices(), kind, true);
    }

    @Override
    public <F> Iterable<? extends F> getFramedVerticesExplicit(final String key, final Object value, final Class<F> kind) {
        return new FramingVertexIterable<>(this, this.getVertices(key, value), kind, true);
    }

    @Override
    public <F> Iterable<? extends F> getFramedEdges(final Class<F> kind) {
        return new FramingEdgeIterable<>(this, this.getEdges(), kind);
    }

    @Override
    public <F> Iterable<? extends F> getFramedEdges(final String key, final Object value, final Class<F> kind) {
        return new FramingEdgeIterable<>(this, this.getEdges(key, value), kind);
    }

    @Override
    public <F> Iterable<? extends F> getFramedEdgesExplicit(final Class<F> kind) {
        return new FramingEdgeIterable<>(this, this.getEdges(), kind, true);
    }

    @Override
    public <F> Iterable<? extends F> getFramedEdgesExplicit(final String key, final Object value, final Class<F> kind) {
        return new FramingEdgeIterable<>(this, this.getEdges(key, value), kind, true);
    }

    @Override
    public VertexTraversal<?, ?, ?> v(final Collection<?> ids) {
        return new SimpleTraversal(this, Iterators.transform(ids.iterator(), new Function<Object, Vertex>() {

            @Override
            public Vertex apply(final Object input) {
                return delegate.getVertex(input);
            }

        })).castToVertices();
    }

    @Override
    public VertexTraversal<?, ?, ?> v(final Object... ids) {
        return new SimpleTraversal(this, Iterators.transform(Iterators.forArray(ids), new Function<Object, Vertex>() {

            @Override
            public Vertex apply(final Object input) {
                return delegate.getVertex(input);
            }

        })).castToVertices();
    }

    @Override
    public EdgeTraversal<?, ?, ?> e(final Object... ids) {
        return new SimpleTraversal(this, Iterators.transform(Iterators.forArray(ids), new Function<Object, Edge>() {

            @Override
            public Edge apply(final Object input) {
                return delegate.getEdge(input);
            }

        })).castToEdges();
    }

    @Override
    public EdgeTraversal<?, ?, ?> e(final Collection<?> ids) {
        return new SimpleTraversal(this, Iterators.transform(ids.iterator(), new Function<Object, Edge>() {

            @Override
            public Edge apply(final Object input) {
                return delegate.getEdge(input);
            }

        })).castToEdges();
    }

    @Override
    public TypeResolver getTypeResolver() {
        return this.defaultResolver;
    }

    @Override
    public Features getFeatures() {
        return this.delegate.getFeatures();
    }

    @Override
    public Vertex addVertex(final Object id) {
        final VertexFrame framedVertex = frameNewElement(delegate.addVertex(null), TVertex.DEFAULT_INITIALIZER);
        return framedVertex.getElement();
    }

    @Override
    public Vertex addVertexExplicit(final Object id) {
        return delegate.addVertex(null);
    }

    @Override
    public Vertex getVertex(final Object id) {
        return this.delegate.getVertex(id);
    }

    @Override
    public void removeVertex(final Vertex vertex) {
        this.delegate.removeVertex(vertex);
    }

    @Override
    public Iterable<Vertex> getVertices() {
        return this.delegate.getVertices();
    }

    @Override
    public Iterable<Vertex> getVertices(final String key, final Object value) {
        return this.delegate.getVertices(key, value);
    }

    @Override
    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        final EdgeFrame framedEdge = frameNewElement(this.delegate.addEdge(id, outVertex, inVertex, label), TEdge.DEFAULT_INITIALIZER);
        return framedEdge.getElement();
    }

    @Override
    public Edge addEdgeExplicit(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        return this.delegate.addEdge(id, outVertex, inVertex, label);
    }

    @Override
    public Edge getEdge(final Object id) {
        return this.delegate.getEdge(id);
    }

    @Override
    public void removeEdge(final Edge edge) {
        this.delegate.removeEdge(edge);
    }

    @Override
    public Iterable<Edge> getEdges() {
        return this.delegate.getEdges();
    }

    @Override
    public Iterable<Edge> getEdges(final String key, final Object value) {
        return this.delegate.getEdges(key, value);
    }

    @Override
    public GraphQuery query() {
        return this.delegate.query();
    }

    @Override
    public void shutdown() {
        this.delegate.shutdown();
    }

    protected Graph getDelegate() {
        return delegate;
    }
}
