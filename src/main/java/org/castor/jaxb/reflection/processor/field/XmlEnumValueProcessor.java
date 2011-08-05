package org.castor.jaxb.reflection.processor.field;

import java.lang.annotation.Annotation;

import javax.xml.bind.annotation.XmlEnumValue;

import org.castor.core.nature.BaseNature;
import org.castor.jaxb.reflection.info.JaxbFieldNature;
import org.castor.jaxb.reflection.processor.BaseFieldProcessor;

/**
 * Annotation processor for XmlEnumValue.
 * 
 * @author Joachim Grueneis, jgrueneis_at_gmail_dot_com
 * @version $Id$
 */
public class XmlEnumValueProcessor extends BaseFieldProcessor {
    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.castor.annoproc.AnnotationProcessor#
     *      processAnnotation(org.castor.xml.introspection.BaseNature,
     *      java.lang.annotation.Annotation)
     */
    public final <I extends BaseNature, A extends Annotation> boolean processAnnotation(final I info, final A annotation) {
        if ((annotation instanceof XmlEnumValue) && (info instanceof JaxbFieldNature)) {
            XmlEnumValue xmlEnumValue = (XmlEnumValue) annotation;
            JaxbFieldNature fieldInfo = (JaxbFieldNature) info;
            this.annotationVisitMessage(xmlEnumValue);
            fieldInfo.setXmlEnumValue(true);
            fieldInfo.setEnumValue(xmlEnumValue.value());
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.castor.annoproc.EnumAnnotationProcessingService
     *      .AnnotationProcessor#forAnnotationClass()
     */
    public Class<? extends Annotation> forAnnotationClass() {
        return XmlEnumValue.class;
    }
}