/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
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

import junit.framework.TestCase;
import hudson.MarkupText.SubText;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Kohsuke Kawaguchi
 */
public class MarkupTextTest extends TestCase {
    public void test1() {
        MarkupText t = new MarkupText("I fixed issue #155. The rest is trick text: xissue #155 issue #123x");
        for (SubText st : t.findTokens(pattern)) {
            assertEquals(1, st.groupCount());
            st.surroundWith("<$1>","<$1>");
        }

        assertEquals("I fixed <155>issue #155<155>. The rest is trick text: xissue #155 issue #123x", t.toString(false));
    }

    public void testBoundary() {
        MarkupText t = new MarkupText("issue #155---issue #123");
        for (SubText st : t.findTokens(pattern))
            st.surroundWith("<$1>","<$1>");

        assertEquals("<155>issue #155<155>---<123>issue #123<123>", t.toString(false));
    }

    public void testFindTokensOnSubText() {
        MarkupText t = new MarkupText("Fixed 2 issues in this commit, fixing issue 155, 145");
        List<SubText> tokens = t.findTokens(Pattern.compile("issue .*"));
        assertEquals("Expected one token", 1, tokens.size());
        assertEquals("Expected single token was incorrect", "issue 155, 145", tokens.get(0).group(0));
        for (SubText st : tokens.get(0).findTokens(Pattern.compile("([0-9]+)")))
            st.surroundWith("<$1>","<$1>");

        assertEquals("Fixed 2 issues in this commit, fixing issue <155>155<155>, <145>145<145>", t.toString(false));
    }

    public void testLiteralTextSurround() {
        MarkupText text = new MarkupText("AAA test AAA");
        for(SubText token : text.findTokens(Pattern.compile("AAA"))) {
            token.surroundWithLiteral("$9","$9");
        }
        assertEquals("$9AAA$9 test $9AAA$9",text.toString(false));
    }

    /**
     * Start/end tag nesting should be correct regardless of the order tags are added.
     */
    public void testAdjacent() {
        MarkupText text = new MarkupText("abcdef");
        text.addMarkup(0,3,"$","$");
        text.addMarkup(3,6,"#","#");
        assertEquals("$abc$#def#",text.toString(false));

        text = new MarkupText("abcdef");
        text.addMarkup(3,6,"#","#");
        text.addMarkup(0,3,"$","$");
        assertEquals("$abc$#def#",text.toString(false));
    }

    public void testEscape() {
        MarkupText text = new MarkupText("&&&");
        assertEquals("&amp;&amp;&amp;",text.toString(false));

        text.addMarkup(1,"<foo>");
        text.addMarkup(2,"&nbsp;");
        assertEquals("&amp;<foo>&amp;&nbsp;&amp;",text.toString(false));
    }

    public void testPreEscape() {
        MarkupText text = new MarkupText("Line\n2   & 3\n<End>\n");
        assertEquals("Line\n2   &amp; 3\n&lt;End>\n", text.toString(true));
        text.addMarkup(4, "<hr/>");
        assertEquals("Line<hr/>\n2   &amp; 3\n&lt;End>\n", text.toString(true));
    }

    /* @Bug(6252) */
    public void testSubTextSubText() {
        MarkupText text = new MarkupText("abcdefgh");
        SubText sub = text.subText(2, 7);
        assertEquals("cdefg", sub.getText());
        sub = sub.subText(1, 4);
        assertEquals("def", sub.getText());

        // test negative end
        sub = text.subText(2, -3);
        assertEquals("cdef", sub.getText());
        sub = sub.subText(1, -2);
        assertEquals("de", sub.getText());
    }

    private static final Pattern pattern = Pattern.compile("issue #([0-9]+)");
}
