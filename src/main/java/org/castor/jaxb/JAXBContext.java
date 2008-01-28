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
package org.castor.jaxb;

import java.util.Map;

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.castor.jaxb.naming.JAXBXmlNaming;
import org.castor.jaxb.naming.JAXBJavaNaming;
import org.castor.jaxb.reflection.ClassDescriptorBuilder;
import org.castor.jaxb.reflection.ClassInfoBuilder;
import org.castor.jaxb.resolver.JAXBClassResolverCommand;
import org.castor.jaxb.resolver.JAXBPackageResolverCommand;
import org.castor.jaxb.resolver.JAXBResolverStrategy;
import org.castor.xml.InternalContext;
import org.castor.xml.JavaNaming;
import org.castor.xml.XMLNaming;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.ResolverException;
import org.exolab.castor.xml.XMLContext;

/**
 * The JAXB context to use Castor for JAXB. This is where all further JAXB work
 * starts. The characteristics of JAXBContext are:
 * <li>it is (more or less) immutable; once created with newInstance no classes
 * or packages can be added; as always there is an exception: a class migth get
 * added during (un-)marshalling as it is encounted</li>
 * <li>Marshaller and Unmarshaller can only be instatiated using a context</li>
 * <li>It is the center piece to start with JAXB</li>
 * 
 * @author Joachim Grueneis, jgrueneis AT codehaus DOT org
 * @version $Id$
 */
public final class JAXBContext extends javax.xml.bind.JAXBContext {
    /** Logger to use. */
    private static final Log LOG = LogFactory.getLog(JAXBContext.class);
    /** The Castor XML context. */
    private XMLContext _xmlContext;

    /**
     * A private constructor so no one outside can instantiate this class.
     */
    private JAXBContext() {
        super();
        _xmlContext = new XMLContext();
        InternalContext internalContext = _xmlContext.getInternalContext();

        JavaNaming javaNaming = new JAXBJavaNaming();
        XMLNaming xmlNaming = new JAXBXmlNaming();
        
        internalContext.setJavaNaming(javaNaming);
        internalContext.setXMLNaming(xmlNaming);
        
        ClassDescriptorBuilder cdb = new ClassDescriptorBuilder();
        cdb.setXMLNaming(xmlNaming);
        
        ClassInfoBuilder cib = new ClassInfoBuilder();
        cib.setJavaNaming(javaNaming);
        
        JAXBPackageResolverCommand packageResolverCommand = new JAXBPackageResolverCommand();
        
        JAXBClassResolverCommand classResolverCommand = new JAXBClassResolverCommand();
        classResolverCommand.setClassDescriptorBuilder(cdb);
        classResolverCommand.setClassInfoBuilder(cib);
        
        JAXBResolverStrategy resolverStrategy = new JAXBResolverStrategy();
        resolverStrategy.setClassResolverCommand(classResolverCommand);
        resolverStrategy.setPackageResolverCommand(packageResolverCommand);
        
        internalContext.setResolverStrategy(resolverStrategy);
    }

    /**
     * Creating a new instance of JAXBContext for a specific Java package. Be
     * aware that this kind of instance requires that this package was before
     * generated by schema compiler or is at least similar to the output of
     * schema compiler. A file jaxb.index has to exist.
     * 
     * @param contextPath
     *            a dot seperate package name as string
     * @return the JAXBContext initialized for the given package
     * @throws JAXBException
     *             creating the new context failed
     */
    public static javax.xml.bind.JAXBContext newInstance(final String contextPath) 
    throws JAXBException {
        if ((contextPath == null) || (contextPath.length() == 0)) {
            final String message = new StringBuffer()
                    .append("No context path specified - contextPath: ")
                    .append(contextPath).toString();
            LOG.warn(message);
            throw new IllegalArgumentException(message);
        }
        JAXBContext jc = new JAXBContext();
        jc.addPackage(contextPath);
        return jc;
    }

    /**
     * Creating a new instance of JAXBContext for a specific Java package. Be
     * aware that this kind of instance requires that this package was before
     * generated by schema compiler or is at least similar to the output of
     * schema compiler. A file jaxb.index has to exist.
     * 
     * @param contextPath
     *            a dot seperated package name as string
     * @param contextPathCL
     *            the class loader to use
     * @return the JAXBContext initialized for the given package
     * @throws JAXBException
     *             creating the new context failed
     */
    public static javax.xml.bind.JAXBContext newInstance(
            final String contextPath, final ClassLoader contextPathCL) 
    throws JAXBException {
        if ((contextPath == null) || (contextPath.length() == 0)) {
            String message = new StringBuffer().append("No context path specified - contextPath: ")
                    .append(contextPath).toString();
            LOG.warn(message);
            throw new IllegalArgumentException(message);
        }
        JAXBContext jc = new JAXBContext();
        jc.setClassLoader(contextPathCL);
        jc.addPackage(contextPath);
        return jc;
    }

    /**
     * Creating a new instance of JAXBContext for a specific Java package. Be
     * aware that this kind of instance requires that this package was before
     * generated by schema compiler or is at least similar to the output of
     * schema compiler. A file jaxb.index has to exist.
     * 
     * @param contextPath
     *            a dot seperated package name as string
     * @param contextPathCL
     *            the class loader to use
     * @param properties
     *            the properties to use
     * @return the JAXBContext initialized for the given package
     * @throws JAXBException
     * @throws JAXBException
     *             creating the new context failed
     */
    public static javax.xml.bind.JAXBContext newInstance(
            final String contextPath, 
            final ClassLoader contextPathCL, 
            final Map < String, ? > properties) 
    throws JAXBException {
        if ((contextPath == null) || (contextPath.length() == 0)) {
            String message = new StringBuffer().append("No context path specified - contextPath: ")
                    .append(contextPath).toString();
            LOG.warn(message);
            throw new IllegalArgumentException(message);
        }
        JAXBContext jc = new JAXBContext();
        jc.setProperties(properties);
        jc.setClassLoader(contextPathCL);
        jc.addPackage(contextPath);
        return jc;
    }

    /**
     * Creating a new instance of JAXBContext for a specific list of classes.
     * The given classes will be checked for JAXB specific annotations and the
     * mapping information found will be available for subsequent
     * (un-)marshalling actions.
     * 
     * @param classesToBeBound
     *            the classes to be read for mapping information
     * @return the JAXBContext initialized with the given classes
     * @throws JAXBException
     *             in case that checking the classes fails
     */
    public static javax.xml.bind.JAXBContext newInstance(
            final Class < ? > ... classesToBeBound)
    throws JAXBException {
        if (classesToBeBound == null) {
            String message = new StringBuffer().append("No classes specified - classesToBeBound: ")
                    .append(classesToBeBound).toString();
            LOG.warn(message);
            throw new IllegalArgumentException(message);
        }

        if (classesToBeBound.length == 0) {
            String message = new StringBuffer().append("No classes specified - classesToBeBound: ")
                    .append(classesToBeBound).toString();
            LOG.warn(message);
            throw new IllegalArgumentException(message);
        }

        JAXBContext jaxbContext = new JAXBContext();
        jaxbContext.addClasses(classesToBeBound);
        return jaxbContext;
    }

    /**
     * A new instance of JAXBContext initialized with an array of classes and a
     * Map of properties.
     * 
     * @param classesToBeBound
     *            array of classes to read mapping information from
     * @param properties
     *            the JAXB properties to use
     * @return a JAXBContext instance which knows how to map the given classes
     * @throws JAXBException
     *             in case that initialization fails
     */
    public static javax.xml.bind.JAXBContext newInstance(
            final Class < ? > [] classesToBeBound, 
            final Map < String, ? > properties)
    throws JAXBException {
        if (classesToBeBound == null) {
            String message = new StringBuffer().append("No classes specified - classesToBeBound: ")
                    .append(classesToBeBound).toString();
            LOG.warn(message);
            throw new IllegalArgumentException(message);
        }

        if (classesToBeBound.length == 0) {
            String message = new StringBuffer().append("No classes specified - classesToBeBound: ")
                    .append(classesToBeBound).toString();
            LOG.warn(message);
            throw new IllegalArgumentException(message);
        }

        JAXBContext jaxbContext = new JAXBContext();
        jaxbContext.setProperties(properties);
        jaxbContext.addClasses(classesToBeBound);
        return jaxbContext;
    }

    /**
     * To set the class loader to use with this context instance.
     * @param contextPathCL the class loader to use
     */
    private void setClassLoader(final ClassLoader contextPathCL) {
        LOG.warn("setClassLoader is not yet implemented!!");
        // TODO Auto-generated method stub
    }

    /**
     * Adds a package to the known classes of this context.
     * @param packageName the name (String) of the package to add
     * @throws JAXBException in case that adding the package fails
     */
    private void addPackage(final String packageName) throws JAXBException {
        try {
            _xmlContext.addPackage(packageName);
        } catch (ResolverException e) {
            String message = "Failed to add classes to context with exception: " + e;
            LOG.warn(message);
            throw new JAXBException(message, e);
        }
    }

    /**
     * TO add an array of classes to the known classes of this context.
     * @param classesToBeBound the classes to add
     * @throws JAXBException in case that the classes cannot be used
     */
    private void addClasses(final Class < ? > [] classesToBeBound) throws JAXBException {
        try {
            _xmlContext.addClasses(classesToBeBound);
        } catch (ResolverException e) {
            String message = "Failed to add classes to context with exception: " + e;
            LOG.warn(message);
            throw new JAXBException(message, e);
        }
    }
    
    /**
     * Loads all descriptors of the Castor mapping as known class-XML mappings.
     * @param mapping the Castor mappings to load
     * @throws JAXBException in case loading of mapping failed
     */
    public void loadMapping(final Mapping mapping) throws JAXBException {
        try {
            _xmlContext.addMapping(mapping);
        } catch (MappingException e) {
            String message = "Failed to load mapping with exception: " + e;
            LOG.warn(message);
            throw new JAXBException(message, e);
        }
    }

    /**
     * To set properties received by newinstance.
     * @param properties a Map of properties
     */
    private void setProperties(final Map < String, ? > properties) {
//        _xmlContext.setProperties(properties);
    }

    /**
     * {@inheritDoc}
     * @see javax.xml.bind.JAXBContext#createMarshaller()
     */
    @Override
    public Marshaller createMarshaller() throws JAXBException {
        org.exolab.castor.xml.Marshaller castorMarshaller = _xmlContext.createMarshaller();
        org.castor.jaxb.Marshaller m = new org.castor.jaxb.Marshaller(castorMarshaller);
        m.setInternalContext(_xmlContext.getInternalContext());
        return m;
    }

    /**
     * {@inheritDoc}
     * @see javax.xml.bind.JAXBContext#createUnmarshaller()
     */
    @Override
    public Unmarshaller createUnmarshaller() throws JAXBException {
        org.exolab.castor.xml.Unmarshaller castorUnmarshaller = _xmlContext.createUnmarshaller();
        org.castor.jaxb.Unmarshaller u = new org.castor.jaxb.Unmarshaller(castorUnmarshaller);
        u.setInternalContext(_xmlContext.getInternalContext());
        return u;
    }

    /**
     * {@inheritDoc}
     * @see javax.xml.bind.JAXBContext#createValidator()
     */
    @Override
    public Validator createValidator() throws JAXBException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * @see javax.xml.bind.JAXBContext#createJAXBIntrospector()
     */
    public JAXBIntrospector createJAXBIntrospector() {
        org.castor.jaxb.JAXBIntrospector ji = new org.castor.jaxb.JAXBIntrospector();
//        ji.setClassDescriptorResolver(_classDescriptorResolver);
        return ji;
    }

    /**
     * {@inheritDoc}
     * @see javax.xml.bind.JAXBContext#createBinder(java.lang.Class)
     */
    public < T > Binder < T  > createBinder(final Class < T > domType) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * @see javax.xml.bind.JAXBContext#createBinder()
     */
    public Binder < org.w3c.dom.Node > createBinder() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * @see javax.xml.bind.JAXBContext#generateSchema(javax.xml.bind.SchemaOutputResolver)
     */
    public void generateSchema(final SchemaOutputResolver schemaOutputResolver) {
        throw new UnsupportedOperationException();
    }

}
