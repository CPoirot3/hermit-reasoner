package org.semanticweb.HermiT.owl_wg_tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class FailingWGTestDebug {
    public static Test suite() throws Exception {
        WGTestRegistry wgTestRegistry=new WGTestRegistry();
        TestSuite suite=new TestSuite("OWL WG Non-Rejected Tests");
        for (WGTestDescriptor wgTestDescriptor : wgTestRegistry.getTestDescriptors())
            if (wgTestDescriptor.status==WGTestDescriptor.Status.APPROVED || wgTestDescriptor.status==WGTestDescriptor.Status.PROPOSED || wgTestDescriptor.status==null) {
                if (wgTestDescriptor.identifier.startsWith("New-Feature-Rational")
                ) {
                    wgTestDescriptor.addTestsToSuite(suite);
                }
            }
        return suite;
    }
}
