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

package hudson;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.util.Collections;
import java.util.List;

/**
 * Obtains the structured form data from {@link StaplerRequest}. See
 * http://wiki.hudson-ci.org/display/HUDSON/Structured+Form+Submission
 *
 * @author Kohsuke Kawaguchi
 */
public class StructuredForm {

    /**
     * @deprecated Use {@link StaplerRequest#getSubmittedForm()}. Since 1.238.
     */
    public static JSONObject get(StaplerRequest req) throws ServletException {
        return req.getSubmittedForm();
    }

    /**
     * Retrieves the property of the given object and returns it as a list of
     * {@link JSONObject}.
     *
     * <p> If the value doesn't exist, this method returns an empty list. If the
     * value is a {@link JSONObject}, this method will return a singleton list.
     * If it's a {@link JSONArray}, the contents will be returned as a list.
     *
     * <p> Because of the way structured form submission work, this is
     * convenient way of handling repeated multi-value entries.
     *
     * @since 1.233
     */
    public static List<JSONObject> toList(JSONObject parent, String propertyName) {
        Object v = parent.get(propertyName);
        if (v == null) {
            return Collections.emptyList();
        }
        if (v instanceof JSONObject) {
            return Collections.singletonList((JSONObject) v);
        }
        if (v instanceof JSONArray) {
            return (List) (JSONArray) v;
        }

        throw new IllegalArgumentException();
    }
}
