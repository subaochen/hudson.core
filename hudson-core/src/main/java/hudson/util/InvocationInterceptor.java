/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 
 *    Kohsuke Kawaguchi
 *
 *
 *******************************************************************************/ 

package hudson.util;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;

/**
 * Interceptor around {@link InvocationHandler}.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.232
 */
public interface InvocationInterceptor {

    /**
     * This method can intercept the invocation of {@link InvocationHandler}
     * either before or after the invocation happens.
     *
     * <p> The general coding pattern is:
     *
     * <pre>
     * Object invoke(Object proxy, Method method, Object[] args, InvocationHandler delegate) {
     *   ... do pre-invocation work ...
     *   ret = delegate.invoke(proxy,method,args);
     *   ... do post-invocation work ...
     *   return ret;
     * }
     * </pre>
     *
     * <p> But the implementation may choose to skip calling the 'delegate'
     * object, alter arguments, and alter the return value.
     */
    public Object invoke(Object proxy, Method method, Object[] args, InvocationHandler delegate) throws Throwable;
}
