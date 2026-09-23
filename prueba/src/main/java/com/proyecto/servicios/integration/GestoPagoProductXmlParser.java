package com.proyecto.servicios.integration;

import com.proyecto.servicios.exception.GestoPagoErrorCode;
import com.proyecto.servicios.exception.GestoPagoIntegrationException;
import com.proyecto.servicios.model.gestopago.product.GestoPagoProductListResponse;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;

@Component
public class GestoPagoProductXmlParser {

    private static final char XML_START = '<';

    private final JAXBContext jaxbContext;
    private final XMLInputFactory xmlInputFactory;

    public GestoPagoProductXmlParser() {
        this.jaxbContext = createJaxbContext();
        this.xmlInputFactory = createSecureXmlInputFactory();
    }

    public GestoPagoProductListResponse parse(byte[] body) {
        if (!isXml(body)) {
            throw new GestoPagoIntegrationException(GestoPagoErrorCode.INVALID_RESPONSE, "el cuerpo no es XML");
        }
        try {
            XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(new ByteArrayInputStream(body));
            return jaxbContext.createUnmarshaller()
                    .unmarshal(reader, GestoPagoProductListResponse.class)
                    .getValue();
        } catch (JAXBException | XMLStreamException e) {
            throw new GestoPagoIntegrationException(GestoPagoErrorCode.INVALID_RESPONSE, e);
        }
    }

    private boolean isXml(byte[] body) {
        if (body == null) {
            return false;
        }
        for (byte current : body) {
            if (!Character.isWhitespace(current)) {
                return current == XML_START;
            }
        }
        return false;
    }

    private static JAXBContext createJaxbContext() {
        try {
            return JAXBContext.newInstance(GestoPagoProductListResponse.class);
        } catch (JAXBException e) {
            throw new IllegalStateException("No fue posible inicializar JAXB para GestoPago", e);
        }
    }

    private static XMLInputFactory createSecureXmlInputFactory() {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        return factory;
    }
}