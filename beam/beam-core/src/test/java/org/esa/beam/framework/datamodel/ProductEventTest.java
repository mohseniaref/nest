/*
 * $Id: ProductEventTest.java,v 1.1 2009-04-28 14:39:33 lveci Exp $
 *
 * Copyright (C) 2002 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.esa.beam.framework.datamodel;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ProductEventTest extends TestCase {

    public ProductEventTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(ProductEventTest.class);
    }

    /**
     * Tests the functionality for the constructor
     */
    public void testRsProductEvent() {
        try {
            new ProductNodeEvent(null, 0);
            fail("ProductNodeEvent construction not allowed with null argument");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Tests the functionality of getNamedNode.
     */
    public void testGetNamedNode() {
        ProductNodeEvent event;
        MetadataElement testNode;

        testNode = new MetadataElement("test_me");
        event = new ProductNodeEvent(testNode, 0);
        assertSame(testNode, event.getSourceNode());
    }
}