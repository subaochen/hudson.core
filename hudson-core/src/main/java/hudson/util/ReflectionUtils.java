/*******************************************************************************
 *
 * Copyright (c) 2004-2010, Oracle Corporation
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

import org.apache.commons.beanutils.PropertyUtils;
import org.kohsuke.stapler.ClassDescriptor;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility code for reflection.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.351
 */
public class ReflectionUtils extends org.springframework.util.ReflectionUtils {

    /**
     * Finds a public method of the given name, regardless of its parameter
     * definitions,
     */
    public static Method getPublicMethodNamed(Class c, String methodName) {
        for (Method m : c.getMethods()) {
            if (m.getName().equals(methodName)) {
                return m;
            }
        }
        return null;
    }

    /**
     * Returns an object-oriented view of parameters of each type.
     */
    public static List<Parameter> getParameters(Method m) {
        return new MethodInfo(m);
    }

    public static Object getPublicProperty(Object o, String p) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(o, p);
        if (pd == null) {
            // field?
            try {
                Field f = o.getClass().getField(p);
                return f.get(o);
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException("No such property " + p + " on " + o.getClass());
            }
        } else {
            return PropertyUtils.getProperty(o, p);
        }
    }

    /**
     * Most reflection operations give us properties of parameters in a batch,
     * so we use this object to store them, then {@link Parameter} will created
     * more object-oriented per-parameter view.
     */
    private static final class MethodInfo extends AbstractList<Parameter> {

        private final Method method;
        private final Class<?>[] types;
        private Type[] genericTypes;
        private Annotation[][] annotations;
        private String[] names;

        private MethodInfo(Method method) {
            this.method = method;
            types = method.getParameterTypes();
        }

        @Override
        public Parameter get(int index) {
            return new Parameter(this, index);
        }

        @Override
        public int size() {
            return types.length;
        }

        public Type[] genericTypes() {
            if (genericTypes == null) {
                genericTypes = method.getGenericParameterTypes();
            }
            return genericTypes;
        }

        public Annotation[][] annotations() {
            if (annotations == null) {
                annotations = method.getParameterAnnotations();
            }
            return annotations;
        }

        public String[] names() {
            if (names == null) {
                names = ClassDescriptor.loadParameterNames(method);
            }
            return names;
        }
    }

    public static final class Parameter {

        private final MethodInfo parent;
        private final int index;

        public Parameter(MethodInfo parent, int index) {
            this.parent = parent;
            this.index = index;
        }

        /**
         * 0-origin index of this parameter.
         */
        public int index() {
            return index;
        }

        /**
         * Gets the type of this parameter.
         */
        public Class<?> type() {
            return parent.types[index];
        }

        /**
         * Gets the unerased generic type of this parameter.
         */
        public Type genericType() {
            return parent.genericTypes()[index];
        }

        /**
         * Gets all the annotations on this parameter.
         */
        public Annotation[] annotations() {
            return parent.annotations()[index];
        }

        /**
         * Gets the specified annotation on this parameter or null.
         */
        public <A extends Annotation> A annotation(Class<A> type) {
            for (Annotation a : annotations()) {
                if (a.annotationType() == type) {
                    return type.cast(a);
                }
            }
            return null;
        }

        /**
         * Name of this parameter.
         *
         * If unknown, this method returns null.
         */
        public String name() {
            String[] names = parent.names();
            if (index < names.length) {
                return names[index];
            }
            return null;
        }
    }

    /**
     * Given the primitive type, returns the VM default value for that type in a
     * boxed form.
     */
    public static Object getVmDefaultValueForPrimitiveType(Class<?> type) {
        return defaultPrimitiveValue.get(type);
    }
    private static final Map<Class, Object> defaultPrimitiveValue = new HashMap<Class, Object>();

    static {
        defaultPrimitiveValue.put(boolean.class, false);
        defaultPrimitiveValue.put(int.class, 0);
        defaultPrimitiveValue.put(long.class, 0L);
    }
}
