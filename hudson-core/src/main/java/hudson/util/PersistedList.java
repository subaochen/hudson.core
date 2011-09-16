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

package hudson.util;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import hudson.model.Describable;
import hudson.model.Saveable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Collection whose change is notified to the parent object for persistence.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.MULTISOURCE
 */
public class PersistedList<T> implements Iterable<T> {
    protected final CopyOnWriteList<T> data = new CopyOnWriteList<T>();
    protected Saveable owner = Saveable.NOOP;

    protected PersistedList() {
    }

    protected PersistedList(Collection<? extends T> initialList) {
        data.replaceBy(initialList);
    }

    public PersistedList(Saveable owner) {
        setOwner(owner);
    }

    public void setOwner(Saveable owner) {
        this.owner = owner;
    }

    public void add(T item) throws IOException {
        data.add(item);
        onModified();
    }

    public void addAll(Collection<? extends T> items) throws IOException {
        data.addAll(items);
        onModified();
    }

    public void replaceBy(Collection<? extends T> col) throws IOException {
        data.replaceBy(col);
        onModified();
    }

    public T get(int index) {
        return data.get(index);
    }

    public <U extends T> U get(Class<U> type) {
        for (T t : data)
            if(type.isInstance(t))
                return type.cast(t);
        return null;
    }

    /**
     * Gets all instances that matches the given type.
     */
    public <U extends T> List<U> getAll(Class<U> type) {
        List<U> r = new ArrayList<U>();
        for (T t : data)
            if(type.isInstance(t))
                r.add(type.cast(t));
        return r;
    }

    public int size() {
        return data.size();
    }

    /**
     * Removes an instance by its type.
     */
    public void remove(Class<? extends T> type) throws IOException {
        for (T t : data) {
            if(t.getClass()==type) {
                data.remove(t);
                onModified();
                return;
            }
        }
    }

    public boolean remove(T o) throws IOException {
        boolean b = data.remove(o);
        if (b)  onModified();
        return b;
    }

    public void removeAll(Class<? extends T> type) throws IOException {
        boolean modified=false;
        for (T t : data) {
            if(t.getClass()==type) {
                data.remove(t);
                modified=true;
            }
        }
        if(modified)
            onModified();
    }


    public void clear() {
        data.clear();
    }

    public Iterator<T> iterator() {
        return data.iterator();
    }

    /**
     * Called when a list is mutated.
     */
    protected void onModified() throws IOException {
        owner.save();
    }

    /**
     * Returns the snapshot view of instances as list.
     */
    public List<T> toList() {
        return data.getView();
    }

    /**
     * Gets all the {@link Describable}s in an array.
     */
    public T[] toArray(T[] array) {
        return data.toArray(array);
    }

    public void addAllTo(Collection<? super T> dst) {
        data.addAllTo(dst);
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * {@link Converter} implementation for XStream.
     *
     * Serializaion form is compatible with plain {@link List}.
     */
    public static class ConverterImpl extends AbstractCollectionConverter {
        CopyOnWriteList.ConverterImpl copyOnWriteListConverter;

        public ConverterImpl(Mapper mapper) {
            super(mapper);
            copyOnWriteListConverter = new CopyOnWriteList.ConverterImpl(mapper());
        }

        public boolean canConvert(Class type) {
            // handle subtypes in case the onModified method is overridden.
            return PersistedList.class.isAssignableFrom(type);
        }

        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
            for (Object o : (PersistedList) source)
                writeItem(o, context, writer);
        }

        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            CopyOnWriteList core = copyOnWriteListConverter.unmarshal(reader, context);

            try {
                PersistedList r = (PersistedList)context.getRequiredType().newInstance();
                r.data.replaceBy(core);
                return r;
            } catch (InstantiationException e) {
                InstantiationError x = new InstantiationError();
                x.initCause(e);
                throw x;
            } catch (IllegalAccessException e) {
                IllegalAccessError x = new IllegalAccessError();
                x.initCause(e);
                throw x;
            }
        }
    }
}

