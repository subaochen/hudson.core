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
 *  Kohsuke Kawaguchi          
 *
 *******************************************************************************/ 

hudsonRules["IMG.treeview-fold-control"] = function(e) {
    e.onexpanded = function() {
        var img = this;
        var tr = findAncestor(img, "TR");
        var tail = tr.nextSibling;

        img.oncollapsed = function() {
            while (tr.nextSibling != tail)
                tr.nextSibling.remove();
        };

        // fetch the nested view and load it when it's ready
        new Ajax.Request(img.getAttribute("url"), {
            method : 'post',
            onComplete : function(x) {
                var cont = document.createElement("div");
                cont.innerHTML = x.responseText;
                var rows = $A(cont.firstChild.rows);
                var anim = { opacity: { from:0, to:1 } };
                rows.reverse().each(function(r) {
                    YAHOO.util.Dom.setStyle(r, 'opacity', 0); // hide
                    YAHOO.util.Dom.insertAfter(r, tr);
                    Behaviour.applySubtree(r);
                    new YAHOO.util.Anim(r, anim, 0.3).animate();
                });
            }
        });
    };
    e = null;
};
