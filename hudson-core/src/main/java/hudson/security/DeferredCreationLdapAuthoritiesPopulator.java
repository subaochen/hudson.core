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

package hudson.security;

import org.springframework.security.GrantedAuthority;
import org.springframework.ldap.core.ContextSource;
import org.springframework.security.ldap.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.populator.DefaultLdapAuthoritiesPopulator;
import hudson.security.SecurityRealm.SecurityComponents;
import org.springframework.ldap.core.DirContextOperations;

/**
 * Implementation of {@link LdapAuthoritiesPopulator} that defers creation of a
 * {@link DefaultLdapAuthoritiesPopulator} until one is needed. This is done to
 * ensure that the groupSearchBase property can be set.
 *
 * @author justinedelson
 * @deprecated as of 1.280 {@link SecurityComponents} are now created after
 * {@link SecurityRealm} is created, so the initialization order issue that this
 * code was trying to address no longer exists.
 */
public class DeferredCreationLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator {

    /**
     * A default role which will be assigned to all authenticated users if set.
     */
    private String defaultRole = null;
    /**
     * An initial context source is only required if searching for groups is
     * required.
     */
    private ContextSource contextSource;
    /**
     * Controls used to determine whether group searches should be performed
     * over the full sub-tree from the base DN.
     */
    private boolean searchSubtree = false;
    /**
     * The ID of the attribute which contains the role name for a group
     */
    private String groupRoleAttribute = "cn";
    /**
     * The base DN from which the search for group membership should be
     * performed
     */
    private String groupSearchBase = null;
    /**
     * The pattern to be used for the user search. {0} is the user's DN
     */
    private String groupSearchFilter = "(| (member={0}) (uniqueMember={0}) (memberUid={0}))";
    private String rolePrefix = "ROLE_";
    private boolean convertToUpperCase = true;

    /**
     * Constructor.
     *
     * @param initialDirContextFactory supplies the contexts used to search for
     * user roles.
     * @param groupSearchBase if this is an empty string the search will be
     * performed from the root DN of the context factory.
     */
    public DeferredCreationLdapAuthoritiesPopulator(
            ContextSource contextSource, String groupSearchBase) {
        this.contextSource = contextSource;
        this.setGroupSearchBase(groupSearchBase);
    }

    public GrantedAuthority[] getGrantedAuthorities(DirContextOperations user, String username) {
        return create().getGrantedAuthorities(user, username);
    }

    public void setConvertToUpperCase(boolean convertToUpperCase) {
        this.convertToUpperCase = convertToUpperCase;
    }

    public void setDefaultRole(String defaultRole) {
        this.defaultRole = defaultRole;
    }

    public void setGroupRoleAttribute(String groupRoleAttribute) {
        this.groupRoleAttribute = groupRoleAttribute;
    }

    public void setGroupSearchBase(String groupSearchBase) {
        this.groupSearchBase = groupSearchBase;
    }

    public void setGroupSearchFilter(String groupSearchFilter) {
        this.groupSearchFilter = groupSearchFilter;
    }

    public void setRolePrefix(String rolePrefix) {
        this.rolePrefix = rolePrefix;
    }

    public void setSearchSubtree(boolean searchSubtree) {
        this.searchSubtree = searchSubtree;
    }

    /**
     * Create a new DefaultLdapAuthoritiesPopulator object.
     *
     * @return a DefaultLdapAuthoritiesPopulator.
     */
    private DefaultLdapAuthoritiesPopulator create() {
        DefaultLdapAuthoritiesPopulator populator = new DefaultLdapAuthoritiesPopulator(
                contextSource, groupSearchBase);
        populator.setConvertToUpperCase(convertToUpperCase);
        if (defaultRole != null) {
            populator.setDefaultRole(defaultRole);
        }
        populator.setGroupRoleAttribute(groupRoleAttribute);
        populator.setGroupSearchFilter(groupSearchFilter);
        populator.setRolePrefix(rolePrefix);
        populator.setSearchSubtree(searchSubtree);
        return populator;
    }
}
