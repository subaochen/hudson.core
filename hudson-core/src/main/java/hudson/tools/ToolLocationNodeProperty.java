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

package hudson.tools;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.slaves.NodeSpecific;
import java.io.IOException;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * {@link NodeProperty} that allows users to specify different locations for
 * {@link ToolInstallation}s.
 *
 * @since 1.286
 */
public class ToolLocationNodeProperty extends NodeProperty<Node> {

    /**
     * Override locations. Never null.
     */
    private final List<ToolLocation> locations;

    @DataBoundConstructor
    public ToolLocationNodeProperty(List<ToolLocation> locations) {
        if (locations == null) {
            throw new IllegalArgumentException();
        }
        this.locations = locations;
    }

    public ToolLocationNodeProperty(ToolLocation... locations) {
        this(Arrays.asList(locations));
    }

    public List<ToolLocation> getLocations() {
        return Collections.unmodifiableList(locations);
    }

    public String getHome(ToolInstallation installation) {
        for (ToolLocation location : locations) {
            if (location.getName().equals(installation.getName()) && location.getType() == installation.getDescriptor()) {
                return location.getHome();
            }
        }
        return null;
    }

    /**
     * Checks if the location of the tool is overridden for the given node, and
     * if so, return the node-specific home directory. Otherwise return
     * {@code installation.getHome()}
     *
     * <p> This is the core logic behind {@link NodeSpecific#forNode(Node)} for
     * {@link ToolInstallation}.
     *
     * @return never null.
     * @deprecated since 2009-04-09. Use
     * {@link ToolInstallation#translateFor(Node,TaskListener)}
     */
    public static String getToolHome(Node node, ToolInstallation installation, TaskListener log) throws IOException, InterruptedException {
        String result = null;

        // node-specific configuration takes precedence
        ToolLocationNodeProperty property = node.getNodeProperties().get(ToolLocationNodeProperty.class);
        if (property != null) {
            result = property.getHome(installation);
        }
        if (result != null) {
            return result;
        }

        // consult translators
        for (ToolLocationTranslator t : ToolLocationTranslator.all()) {
            result = t.getToolHome(node, installation, log);
            if (result != null) {
                return result;
            }
        }

        // fall back is no-op
        return installation.getHome();
    }

    @Extension
    public static class DescriptorImpl extends NodePropertyDescriptor {

        public String getDisplayName() {
            return Messages.ToolLocationNodeProperty_displayName();
        }

        public DescriptorExtensionList<ToolInstallation, ToolDescriptor<?>> getToolDescriptors() {
            return ToolInstallation.all();
        }

        public String getKey(ToolInstallation installation) {
            return installation.getDescriptor().getClass().getName() + "@" + installation.getName();
        }

        @Override
        public boolean isApplicable(Class<? extends Node> nodeType) {
            return nodeType != Hudson.class;
        }
    }

    public static final class ToolLocation {

        private final String type;
        private final String name;
        private final String home;
        private transient volatile ToolDescriptor descriptor;

        public ToolLocation(ToolDescriptor type, String name, String home) {
            this.descriptor = type;
            this.type = type.getClass().getName();
            this.name = name;
            this.home = home;
        }

        @DataBoundConstructor
        public ToolLocation(String key, String home) {
            this.type = key.substring(0, key.indexOf('@'));
            this.name = key.substring(key.indexOf('@') + 1);
            this.home = home;
        }

        public String getName() {
            return name;
        }

        public String getHome() {
            return home;
        }

        public ToolDescriptor getType() {
            if (descriptor == null) {
                descriptor = (ToolDescriptor) Descriptor.find(type);
            }
            return descriptor;
        }

        public String getKey() {
            return type + "@" + name;
        }
    }
}
