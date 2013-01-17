package org.picketlink.identity.federation.core.parsers.config;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.picketlink.identity.federation.core.config.idm.IDMType;
import org.picketlink.identity.federation.core.config.PicketLinkType;
import org.picketlink.identity.federation.core.config.ProviderType;
import org.picketlink.identity.federation.core.config.STSType;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.handler.config.Handlers;
import org.picketlink.identity.federation.core.parsers.AbstractParser;
import org.picketlink.identity.federation.core.parsers.ParserNamespaceSupport;
import org.picketlink.identity.federation.core.parsers.sts.STSConfigParser;
import org.picketlink.identity.federation.core.parsers.util.StaxParserUtil;

/**
 * Parser to parse the consolidated picketlink.xml
 *
 * @author anil saldhana
 */
public class PicketLinkConfigParser extends AbstractParser {

    public static final String PICKETLINK = "PicketLink";

    public static final String ENABLE_AUDIT = "EnableAudit";

    @Override
    public Object parse(XMLEventReader xmlEventReader) throws ParsingException {
        PicketLinkType picketLinkType = new PicketLinkType();
        StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
        StaxParserUtil.validate(startElement, PICKETLINK);

        // parse and set the root element attributes.
        QName attributeQName = new QName("", ENABLE_AUDIT);
        Attribute attribute = startElement.getAttributeByName(attributeQName);
        if (attribute != null) {
            picketLinkType.setEnableAudit(Boolean.parseBoolean(StaxParserUtil.getAttributeValue(attribute)));
        }

        startElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
        String tag = StaxParserUtil.getStartElementName(startElement);
        while (xmlEventReader.hasNext()) {
            if (SAMLConfigParser.IDP.equals(tag)) {
                SAMLConfigParser samlConfigParser = new SAMLConfigParser();
                ProviderType idp = (ProviderType) samlConfigParser.parse(xmlEventReader);
                picketLinkType.setIdpOrSP(idp);
            } else if (SAMLConfigParser.SP.equals(tag)) {
                SAMLConfigParser samlConfigParser = new SAMLConfigParser();
                ProviderType sp = (ProviderType) samlConfigParser.parse(xmlEventReader);
                picketLinkType.setIdpOrSP(sp);
            } else if (SAMLConfigParser.HANDLERS.equals(tag)) {
                SAMLConfigParser samlConfigParser = new SAMLConfigParser();
                Handlers handlers = (Handlers) samlConfigParser.parse(xmlEventReader);
                picketLinkType.setHandlers(handlers);
            } else if (STSConfigParser.ROOT_ELEMENT.equals(tag)) {
                STSConfigParser samlConfigParser = new STSConfigParser();
                STSType sts = (STSType) samlConfigParser.parse(xmlEventReader);
                picketLinkType.setStsType(sts);
            } else if ("PicketLinkIDM".equals(tag)) {
                // TODO: we are using reflection because this class doesn't see IDMConfigParser at compile time. Needs to be fixed...
                try {
                    Class<?> idmParserClass = Class.forName("org.picketlink.config.idm.parsers.IDMConfigParser");
                    ParserNamespaceSupport parser = (ParserNamespaceSupport)idmParserClass.newInstance();
                    IDMType idmType = (IDMType)parser.parse(xmlEventReader);
                    picketLinkType.setIdmType(idmType);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // avoid infinite loop if unknown element is found
            else {
                throw logger.parserUnknownStartElement(tag, startElement.getLocation());
            }
            startElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
            if (startElement == null)
                break;
            tag = StaxParserUtil.getStartElementName(startElement);
        }
        return picketLinkType;
    }

    @Override
    public boolean supports(QName qname) {
        return false;
    }
}