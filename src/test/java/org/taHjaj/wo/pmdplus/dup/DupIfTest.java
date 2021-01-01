/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package org.taHjaj.wo.pmdplus.dup;

import net.sourceforge.pmd.testframework.SimpleAggregatorTst;
import org.junit.Before;


public class DupIfTest extends SimpleAggregatorTst {

    private static final String RULESET = "java-dup";

    @Before
    public void setUp() {
        addRule(RULESET, "DupIf");
    }
    
    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(DupIfTest.class);
    }
}
