/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package org.taHjaj.wo.pmdplus.dup;

import net.sourceforge.pmd.testframework.SimpleAggregatorTst;

public class DupTest extends SimpleAggregatorTst {

    private static final String RULESET = "java-dup";

    @Override
    public void setUp() {
        addRule(RULESET, "Dup");
    }
}
