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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.castor.xml.InternalContext;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.util.DocumentHandlerAdapter;
import org.exolab.castor.xml.util.SAX2DOMHandler;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

/**
 * Wraps the Castor marshaller with a JAXB compliant API.
 * @see javax.xml.bind.Marshaller
 * @see org.exolab.castor.xml.Marshaller
 * 
 * @author Joachim Grueneis, jgrueneis AT codehaus DOT org
 * @version $Id$
 */
public class Marshaller implements javax.xml.bind.Marshaller {
    /** Logger to use. */
    private static final Log LOG = LogFactory.getLog(Marshaller.class);

    /**
     * The Castor XML Marshaller instance used for marshalling.
     */
    private org.exolab.castor.xml.Marshaller _castorMarshaller;

    /**
     * An adapter for proxying Castor's MarshalListener callbacks.
     */
    private ValidationEventHandlerAdapter _validationEventHandlerAdapter = null;

    /**
     * An adapter for proxying Castor's MarshalListener callbacks.
     */
    private MarshalListenerAdapter _marshalListener = null;

    /**
     * Validation event handler.
     */
    private ValidationEventHandler _validationEventHandler;

    /**
     * The XML schema to be used for validation post-marshalling, using a JAXP
     * 1.3 Validator instance.
     */
    private Schema _schema = null;

    /**
     * The XML context to use at marshalling.
     */
    private InternalContext _internalContext;

    /**
     * Only JAXBCopntext is allowed to instantiate Marshaller.
     */
    protected Marshaller(final org.exolab.castor.xml.Marshaller castorMarshaller) {
        _castorMarshaller = castorMarshaller;
    }

    /**
     * {@inheritDoc}
     * @see javax.xml.bind.Marshaller#getAdapter(java.lang.Class)
     */
    public < A extends XmlAdapter > A getAdapter(final Class < A > xmlAdapter) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * @see javax.xml.bind.Marshaller#getAttachmentMarshaller()
     */
    public AttachmentMarshaller getAttachmentMarshaller() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * @see javax.xml.bind.Marshaller#getEventHandler()
     */
    public ValidationEventHandler getEventHandler() throws JAXBException {
        return this._validationEventHandler;
    }

    /**
     * Returns the listener used... if no listener was previously set null
     * is returned.
     * @return the listner set or null
     * @see javax.xml.bind.Marshaller#getListener()
     */
    public Listener getListener() {
        return (_marshalListener == null) ? null : _marshalListener.getJAXBListener();
    }

    /**
     * {@inheritDoc}
     * @see javax.xml.bind.Marshaller#getNode(java.lang.Object)
     */
    public Node getNode(final Object node) throws JAXBException {
        throw new UnsupportedOperationException(
                "Not supported, as this method apparently is optional.");
    }

    /**
     * {@inheritDoc}
     * @see javax.xml.bind.Marshaller#getProperty(java.lang.String)
     */
    public Object getProperty(final String propertyName) throws PropertyException {
        return _internalContext.getProperty(propertyName);
    }

    /**
     * {@inheritDoc}
     * @see javax.xml.bind.Marshaller#getSchema()
     */
    public Schema getSchema() {
        return this._schema;
    }

    /**
     * {@inheritDoc}
     * @see javax.xml.bind.Marshaller#marshal(java.lang.Object, javax.xml.transform.Result)
     */
    public void marshal(
            final Object object, final Result result) 
    throws JAXBException {
        if (result == null) {
            String message = "Argument Result must not be null!";
            LOG.warn(message + " Throwing IllegalArgumentException.");
            throw new IllegalArgumentException(message);
        }
        if (result instanceof SAXResult) {
            SAXResult saxResult = (SAXResult) result;
            marshal(object, saxResult.getHandler());
        } else if (result instanceof DOMResult) {
            DOMResult domResult = (DOMResult) result;
            marshal(object, domResult.getNode());
        } else if (result instanceof StreamResult) {
            StreamResult streamResult = (StreamResult) result;
            // TODO: if (getWriter != null) {
            marshal(object, streamResult.getWriter());
            // TODO: if (getOutputStream() != null) {
        } else {
            throw new IllegalArgumentException(
                    "Illegal Result instance. Not soppurted by Castor");
        }
    }

    /**
     * {@inheritDoc}
     * @see javax.xml.bind.Marshaller#marshal(java.lang.Object, java.io.OutputStream)
     */
    public void marshal(
            final Object object, final OutputStream stream) 
    throws JAXBException {
        if (stream == null) {
            String message = "OutputStream must not be null!";
            LOG.warn(message + " Throwing IllegalArgumentException.");
            throw new IllegalArgumentException(message);
        }
        try {
            _castorMarshaller.setWriter(new OutputStreamWriter(stream));
            marshal(object);
            validation();
        } catch (IOException e) {
            String message = "Failed to wrap OutputStream into OutputStreamWriter with: " + e;
            LOG.warn(message);
            throw new JAXBException(message, e);
        }
    }

    /**
     * {@inheritDoc}
     * @see javax.xml.bind.Marshaller#marshal(java.lang.Object, java.io.Writer)
     */
    public void marshal(
            final Object object, final Writer writer) 
    throws JAXBException {
        if (writer == null) {
            String message = "Writer for marshalling must not be null!";
            LOG.warn(message + " Throwing IllegalArgumentException.");
            throw new IllegalArgumentException(message);
        }
        try {
            _castorMarshaller.setWriter(writer);
            marshal(object);
            validation();
        } catch (IOException e) {
            throw new JAXBException(
                    "Problem opening the java.util.Writer instance for marshalling " + object);
        }
    }

    /**
     * {@inheritDoc}
     * @see javax.xml.bind.Marshaller#marshal(java.lang.Object, org.xml.sax.ContentHandler)
     */
    public void marshal(
            final Object object, final ContentHandler contentHandler)
    throws JAXBException {
        if (contentHandler == null) {
            String message = "ContentHandler for marshalling must not be null!";
            LOG.warn(message + " Throwing IllegalArgumentException.");
            throw new IllegalArgumentException(message);
        }
        _castorMarshaller.setContentHandler(contentHandler);
        marshal(object);
        validation();
    }

    /**
     * {@inheritDoc}
     * @see javax.xml.bind.Marshaller#marshal(java.lang.Object, org.w3c.dom.Node)
     */
    public void marshal(
            final Object object, 
            final Node node) 
    throws JAXBException {
        if (node == null) {
            String message = "Argument 'node' is null.";
            LOG.warn(message + " Throwing IllegalArgumentException.");
            throw new IllegalArgumentException(message);
        }
        ContentHandler contentHandler = new DocumentHandlerAdapter(new SAX2DOMHandler(node));
        _castorMarshaller.setContentHandler(contentHandler);
        marshal(object);
        validation();
    }

    /**
     * {@inheritDoc}
     * @see javax.xml.bind.Marshaller#marshal(java.lang.Object, javax.xml.stream.XMLStreamWriter)
     */
    public void marshal(
            final Object object, final XMLStreamWriter xmlStreamWriter)
    throws JAXBException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * @see javax.xml.bind.Marshaller#marshal(java.lang.Object, javax.xml.stream.XMLEventWriter)
     */
    public void marshal(
            final Object object, final XMLEventWriter xmlEventWriter) 
    throws JAXBException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * @see javax.xml.bind.Marshaller#marshal(java.lang.Object, java.io.File)
     */
    public void marshal(
            final Object object, final File file) 
    throws JAXBException {
        if (file == null) {
            String message = "Argument 'file' is null.";
            LOG.warn(message + " Throwing IllegalArgumentException.");
            throw new IllegalArgumentException(message);
        }
        try {
            _castorMarshaller.setWriter(new FileWriter(file));
            marshal(object);
            validation();
        } catch (IOException e) {
            String message = "Failed to wrap File into FileWriter with: " + e;
            LOG.warn(message);
            throw new JAXBException(message, e);
        }
    }
    
    /**
     * A private marshal method to have the marshalling exception handling only once.
     * @param object the object to marshal
     * @throws JAXBException if marshalling fails fatally
     */
    private void marshal(final Object object) throws JAXBException {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Starting to marshal object: " + object);
            }
            _castorMarshaller.marshal(object);
        } catch (MarshalException e) {
            String message = "Failed to marshal object: " + object + " with exception: " + e;
            LOG.warn(message);
            throw new JAXBException(message, e);
        } catch (ValidationException e) {
            String message = "Failed to marshal object: " + object + " with exception: " + e;
            LOG.warn(message);
            throw new JAXBException(message, e);
        }
    }

    private void validation() {
//        if (_schema != null) {
//            // TODO: refactor hack !!!
//            Validator validator = _schema.newValidator();
//            ValidationEventHandlerAdapter errorHandler = new ValidationEventHandlerAdapter();
//            errorHandler.setHandler(_validationEventHandler);
//            validator.setErrorHandler(errorHandler);
//            String content = writer.toString();
//            validator.validate(new StreamSource(new StringReader(content)));
//        }
    }

    /**
     * {@inheritDoc}
     * @see javax.xml.bind.Marshaller#setAdapter(javax.xml.bind.annotation.adapters.XmlAdapter)
     */
    public void setAdapter(final XmlAdapter arg0) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * @see javax.xml.bind.Marshaller#setAdapter(java.lang.Class, javax.xml.bind.annotation.adapters.XmlAdapter)
     */
    public < A extends XmlAdapter > void setAdapter(
            final Class < A > arg0, final A arg1) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * @see javax.xml.bind.Marshaller#setAttachmentMarshaller(javax.xml.bind.attachment.AttachmentMarshaller)
     */
    public void setAttachmentMarshaller(final AttachmentMarshaller arg0) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * @see javax.xml.bind.Marshaller#setEventHandler(javax.xml.bind.ValidationEventHandler)
     */
    public void setEventHandler(final ValidationEventHandler validationEventHandler)
            throws JAXBException {
        this._validationEventHandler = validationEventHandler;
    }

    /**
     * {@inheritDoc}
     * @see javax.xml.bind.Marshaller#setListener(javax.xml.bind.Marshaller.Listener)
     */
    public void setListener(final Listener listener) {
        _marshalListener = new MarshalListenerAdapter();
        _marshalListener.setJAXBListener(listener);
        _castorMarshaller.setMarshalListener(_marshalListener);
    }

    /**
     * {@inheritDoc}
     * @see javax.xml.bind.Marshaller#setProperty(java.lang.String, java.lang.Object)
     */
    public void setProperty(final String property, final Object value)
            throws PropertyException {
        _internalContext.setProperty(property, value);
    }

    /**
     * {@inheritDoc}
     * @see javax.xml.bind.Marshaller#setSchema(javax.xml.validation.Schema)
     */
    public void setSchema(final Schema schema) {
        this._schema = schema;
    }

    /**
     * To set the Castor XML context to use.
     * @param internalContext the Castor XML Context to use
     */
    public void setInternalContext(final InternalContext internalContext) {
        _internalContext = internalContext;
        _castorMarshaller.setInternalContext(_internalContext);
    }
}
