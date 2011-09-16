/*******************************************************************************
 *
 * Copyright (c) 2004-2009, Oracle Corporation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *        
 *
 *******************************************************************************/ 

package hudson;

import hudson.model.Hudson;
import hudson.tasks.UserNameResolver;
import hudson.util.CopyOnWriteList;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;

/**
 * Compatibility layer for legacy manual registration of extension points.
 *
 * <p>
 * Instances of this class can be created statically as a singleton, but it provides the view
 * to {@link ExtensionList} of the current {@link Hudson}.
 * Write operations to this list will update the legacy instances on {@link ExtensionList}.
 *
 * <p>
 * Whereas we used to use some simple data structure to keep track of static singletons,
 * we can now use this instances, so that {@link ExtensionList} sees all the auto-registered
 * and manually registered instances.
 *
 * <p>
 * Similarly, the old list (such as {@link UserNameResolver#LIST} continues to show all
 * auto and manually registered instances, thus providing necessary bi-directional interoperability.
 *
 * @author Kohsuke Kawaguchi
 */
public class ExtensionListView {
    /**
     * Creates a plain {@link List} backed by the current {@link ExtensionList}.
     */
    public static <T> List<T> createList(final Class<T> type) {
        return new AbstractList<T>() {
            private ExtensionList<T> storage() {
                return Hudson.getInstance().getExtensionList(type);
            }

            @Override
            public Iterator<T> iterator() {
                return storage().iterator();
            }

            public T get(int index) {
                return storage().get(index);
            }

            public int size() {
                return storage().size();
            }

            @Override
            public boolean add(T t) {
                return storage().add(t);
            }

            @Override
            public void add(int index, T t) {
                // index ignored
                storage().add(t);
            }

            @Override
            public T remove(int index) {
                return storage().remove(index);
            }

            @Override
            public boolean remove(Object o) {
                return storage().remove(o);
            }
        };
    }

    /**
     * Creates a enhanced {@link CopyOnWriteList} that acts as a view to the current {@link ExtensionList}.
     */
    public static <T> CopyOnWriteList<T> createCopyOnWriteList(final Class<T> type) {
        return new CopyOnWriteList<T>() {
            private ExtensionList<T> storage() {
                return Hudson.getInstance().getExtensionList(type);
            }

            @Override
            public void add(T t) {
                storage().add(t);
            }

            @Override
            public boolean remove(T t) {
                return storage().remove(t);
            }

            @Override
            public Iterator<T> iterator() {
                return storage().iterator();
            }

            @Override
            public void replaceBy(CopyOnWriteList<? extends T> that) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void replaceBy(Collection<? extends T> that) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void replaceBy(T... that) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void clear() {
                throw new UnsupportedOperationException();
            }

            @Override
            public T[] toArray(T[] array) {
                return storage().toArray(array);
            }

            @Override
            public List<T> getView() {
                return storage();
            }

            @Override
            public void addAllTo(Collection<? super T> dst) {
                dst.addAll(storage());
            }

            @Override
            public boolean isEmpty() {
                return storage().isEmpty();
            }
        };
    }

    // TODO: we need a few more types whose implementations get uglier
}
