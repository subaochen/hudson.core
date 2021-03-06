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

package hudson.tools;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Hudson;

/**
 * Extensible property of {@link ToolInstallation}.
 *
 * <p> Plugins can contribute this extension point to add additional data or UI
 * actions to {@link ToolInstallation}. {@link ToolProperty}s show up in the
 * configuration screen of a tool, and they are persisted with the
 * {@link ToolInstallation} object.
 *
 *
 * <h2>Views</h2> <dl> <dt>config.jelly</dt> <dd>Added to the configuration page
 * of the tool. </dl>
 *
 * @param <T> {@link ToolProperty} can choose to only work with a certain
 * subtype of {@link ToolInstallation}, and this 'T' represents that type. Also
 * see {@link ToolPropertyDescriptor#isApplicable(Class)}.
 *
 * @since 1.303
 */
public abstract class ToolProperty<T extends ToolInstallation> implements Describable<ToolProperty<?>>, ExtensionPoint {

    protected transient T tool;

    protected void setTool(T tool) {
        this.tool = tool;
    }

    public ToolPropertyDescriptor getDescriptor() {
        return (ToolPropertyDescriptor) Hudson.getInstance().getDescriptorOrDie(getClass());
    }

    /**
     * What is your 'T'?
     */
    public abstract Class<T> type();

    /**
     * Lists up all the registered {@link ToolPropertyDescriptor}s in the
     * system.
     *
     * @see ToolDescriptor#getPropertyDescriptors()
     */
    public static DescriptorExtensionList<ToolProperty<?>, ToolPropertyDescriptor> all() {
        return (DescriptorExtensionList) Hudson.getInstance().getDescriptorList(ToolProperty.class);
    }
}
