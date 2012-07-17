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
 *    Tom Huybrechts
 *
 *
 *******************************************************************************/ 

package hudson.slaves;

import hudson.Extension;
import hudson.model.Node;
import hudson.tools.PropertyDescriptor;

/**
 * Descriptor for {@link NodeProperty}.
 *
 * <p> Put {@link Extension} on your descriptor implementation to have it
 * auto-registered.
 *
 * @since 1.286
 * @see NodeProperty
 */
public abstract class NodePropertyDescriptor extends PropertyDescriptor<NodeProperty<?>, Node> {

    protected NodePropertyDescriptor(Class<? extends NodeProperty<?>> clazz) {
        super(clazz);
    }

    protected NodePropertyDescriptor() {
    }
}
