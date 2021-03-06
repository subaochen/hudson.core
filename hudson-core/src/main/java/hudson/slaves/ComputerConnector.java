/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc.
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

package hudson.slaves;

import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.TaskListener;

import java.io.IOException;

/**
 * Factory of {@link ComputerLauncher}.
 *
 * When writing a {@link Cloud} implementation, one needs to dynamically create
 * {@link ComputerLauncher} by supplying a host name. This is the abstraction
 * for that.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.383
 * @see ComputerLauncher
 */
public abstract class ComputerConnector extends AbstractDescribableImpl<ComputerConnector> implements ExtensionPoint {

    /**
     * Creates a {@link ComputerLauncher} for connecting to the given host.
     *
     * @param host The host name / IP address of the machine to connect to.
     * @param listener If
     */
    public abstract ComputerLauncher launch(String host, TaskListener listener) throws IOException, InterruptedException;

    @Override
    public ComputerConnectorDescriptor getDescriptor() {
        return (ComputerConnectorDescriptor) super.getDescriptor();
    }
}
