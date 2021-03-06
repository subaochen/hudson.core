/*******************************************************************************
 *
 * Copyright (c) 2011 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *    Anton Kozak
 *
 *******************************************************************************/
package hudson.model;

import hudson.model.labels.LabelAtom;
import hudson.model.labels.LabelExpression;
import java.io.Serializable;
import org.antlr.runtime.RecognitionException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Appointed node holder for {@link AbstractProject}.
 *
 * Date: 11/1/11
 */
public class AppointedNode implements Serializable {

    /**
     * If this project is configured to be only built on a certain label, this
     * value will be set to that label.
     */
    private String nodeName;
    /**
     * Node list dropdown or textfield.
     */
    private Boolean advancedAffinityChooser;
    /**
     * True if this project can be built on any node.
     */
    private volatile boolean canRoam;

    public AppointedNode() {
    }

    public AppointedNode(String nodeName, Boolean advancedAffinityChooser) {
        this.nodeName = nodeName;
        this.advancedAffinityChooser = advancedAffinityChooser;
        this.canRoam = nodeName == null;
    }

    /**
     * Returns the name of the node.
     *
     * @return the name of the node.
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * Sets the name of the node.
     *
     * @param nodeName the name of the node.
     */
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * Gets whether this project is using the advanced affinity chooser UI.
     *
     * @return true - advanced chooser, false - simple textfield.
     */
    public Boolean getAdvancedAffinityChooser() {
        if (null == advancedAffinityChooser) {
            advancedAffinityChooser = false;
        }
        return advancedAffinityChooser;
    }

    /**
     * Sets true if the node configured with advanced label expression and false
     * if with combobox.
     *
     * @param advancedAffinityChooser boolean.
     */
    public void setAdvancedAffinityChooser(Boolean advancedAffinityChooser) {
        this.advancedAffinityChooser = advancedAffinityChooser;
    }

    /**
     * If this project is configured to be always built on this node, return
     * that {@link Node}. Otherwise null.
     *
     * @return assigned label
     */
    public Label getAssignedLabel() {
        if (canRoam) {
            return null;
        }
        if (nodeName == null) {
            return Hudson.getInstance().getSelfLabel();
        }
        return Hudson.getInstance().getLabel(nodeName);
    }

    /**
     * Gets the textual representation of the assigned label as it was entered
     * by the user.
     *
     * @return assigned label as string
     */
    public String getAssignedLabelString() {
        if (canRoam || nodeName == null) {
            return null;
        }
        try {
            LabelExpression.parseExpression(nodeName);
            return nodeName;
        } catch (RecognitionException e) {
            // must be old label or host name that includes whitespace or other unsafe chars
            return LabelAtom.escape(nodeName);
        }
    }

    /**
     * Sets the assigned label.
     *
     * @param l Label
     */
    public void setAssignedLabel(Label l) {
        if (l == null) {
            canRoam = true;
            nodeName = null;
        } else {
            canRoam = false;
            nodeName = l == Hudson.getInstance().getSelfLabel() ? null : l.getExpression();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AppointedNode that = (AppointedNode) o;
        return new EqualsBuilder()
                .append(advancedAffinityChooser, that.advancedAffinityChooser)
                .append(nodeName, that.nodeName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(advancedAffinityChooser)
                .append(nodeName)
                .toHashCode();
    }
}
