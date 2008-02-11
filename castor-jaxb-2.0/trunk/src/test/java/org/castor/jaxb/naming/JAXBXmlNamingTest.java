/*
 * Copyright 2008 Joachim Grueneis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.castor.jaxb.naming;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author Joachim Grueneis, jgrueneis AT codehaus DOT org
 * @version $Id$
 *
 */
public class JAXBXmlNamingTest extends TestCase {
    /** 
     * Logger to be used.
     */
    private static final Log LOG = LogFactory.getLog(JAXBXmlNamingTest.class);
    /** Object to test. */
    private JAXBXmlNaming _xmlNaming;
    /**
     * {@inheritDoc}
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() {
        _xmlNaming = new JAXBXmlNaming();
    }
    /**
     * The createXMLName is not supported in the JAXB implementation...
     * so we expect to get {@link UnsupportedOperationException} in any
     * case.
     */
    public void testCreateXMLName() {
        try {
            _xmlNaming.createXMLName(null);
            Assert.fail("Must not complete successfully!");
        } catch (UnsupportedOperationException e) {
            // expected exception -> fine!
        }
        try {
            _xmlNaming.createXMLName(String.class);
            Assert.fail("Must not complete successfully!");
        } catch (UnsupportedOperationException e) {
            // expected exception -> fine!
        }
        try {
            _xmlNaming.createXMLName(JAXBXmlNaming.class);
            Assert.fail("Must not complete successfully!");
        } catch (UnsupportedOperationException e) {
            // expected exception -> fine!
        }
    }
    public void testToXMLName() {
        Assert.assertNull("Null in -> null out", _xmlNaming.toXMLName(null));
        Assert.assertEquals("x", _xmlNaming.toXMLName("x"));
        Assert.assertEquals("JAXBXmlNamingTest", _xmlNaming.toXMLName("JAXBXmlNamingTest"));
        Assert.assertEquals("hugo", _xmlNaming.toXMLName("Hugo"));
        Assert.assertEquals("hugoFranz", _xmlNaming.toXMLName("HugoFranz"));
        Assert.assertEquals("a", _xmlNaming.toXMLName("A"));
    }
}