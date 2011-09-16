/*******************************************************************************
 *
 * Copyright (c) 2010-2011 Sonatype, Inc.
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

package org.eclipse.hudson.plugins.snapshotmonitor;

import org.eclipse.hudson.utils.plugin.ui.JellyAccessible;
import org.eclipse.hudson.maven.model.MavenCoordinatesDTO;
import hudson.model.Cause;

import java.util.Collection;

/**
 * Cause when an external SNAPSHOT dependency change triggers a build.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class DependenciesChangedCause
    extends Cause
{
    private final Collection<MavenCoordinatesDTO> dependencies;

    public DependenciesChangedCause(final Collection<MavenCoordinatesDTO> dependencies) {
        assert dependencies != null;
        this.dependencies = dependencies;
    }

    @JellyAccessible
    public Collection<MavenCoordinatesDTO> getDependencies() {
        return dependencies;
    }

    @Override
    public String getShortDescription() {
        // TODO: Use localizer
        return "External SNAPSHOT dependency change";
    }
}
