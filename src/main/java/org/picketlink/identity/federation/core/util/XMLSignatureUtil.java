/*
 * JBoss, Home of Professional Open Source. Copyright 2008, Red Hat Middleware LLC, and individual contributors as
 * indicated by the @author tags. See the copyright.txt file in the distribution for a full listing of individual
 * contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this software; if not, write to
 * the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF site:
 * http://www.fsf.org.
 */
package org.picketlink.identity.federation.core.util;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.transfer.SignatureUtilTransferObject;
import org.picketlink.identity.federation.core.wstrust.WSTrustConstants;
import org.picketlink.identity.xmlsec.w3.xmldsig.DSAKeyValueType;
import org.picketlink.identity.xmlsec.w3.xmldsig.KeyValueType;
import org.picketlink.identity.xmlsec.w3.xmldsig.RSAKeyValueType;
import org.picketlink.identity.xmlsec.w3.xmldsig.SignatureType;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Utility for XML Signature <b>Note:</b> You can change the canonicalization method type by using the system property
 * "picketlink.xmlsig.canonicalization"
 *
 * @author Anil.Saldhana@redhat.com
 * @author alessio.soldano@jboss.com
 * @since Dec 15, 2008
 */
public class XMLSignatureUtil {
    // Set some system properties and Santuario providers. Run this block before any other class initialization.
    static {
        ProvidersUtil.ensure();
        SystemPropertiesUtil.ensure();
        String keyInfoProp = SecurityActions.getSystemProperty("picketlink.xmlsig.includeKeyInfo", null);
        if (StringUtil.isNotNull(keyInfoProp)) {
            includeKeyInfoInSignature = Boolean.parseBoolean(keyInfoProp);
        }
    };

    private static Logger log = Logger.getLogger(XMLSignatureUtil.class);

    private static boolean trace = log.isTraceEnabled();

    private static String canonicalizationMethodType = CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS;

    private static XMLSignatureFactory fac = getXMLSignatureFactory();

    /**
     * By default, we include the keyinfo in the signature
     */
    private static boolean includeKeyInfoInSignature = true;

    private static XMLSignatureFactory getXMLSignatureFactory() {
        XMLSignatureFactory xsf = null;

        try {
            xsf = XMLSignatureFactory.getInstance("DOM", "ApacheXMLDSig");
        } catch (NoSuchProviderException ex) {
            try {
                xsf = XMLSignatureFactory.getInstance("DOM");
            } catch (Exception err) {
                throw new RuntimeException(ErrorCodes.CANNOT_CREATE_INSTANCE + err.getLocalizedMessage());
            }
        }
        return xsf;
    }

    /**
     * Set the canonicalization method type
     *
     * @param canonical
     */
    public static void setCanonicalizationMethodType(String canonical) {
        if (canonical != null)
            canonicalizationMethodType = canonical;
    }

    /**
     * Use this method to not include the KeyInfo in the signature
     *
     * @param includeKeyInfoInSignature
     * @since v2.0.1
     */
    public static void setIncludeKeyInfoInSignature(boolean includeKeyInfoInSignature) {
        XMLSignatureUtil.includeKeyInfoInSignature = includeKeyInfoInSignature;
    }

    /**
     * Precheck whether the document that will be validated has the right signedinfo
     *
     * @param doc
     * @return
     */
    public static boolean preCheckSignedInfo(Document doc) {
        NodeList nl = doc.getElementsByTagNameNS(JBossSAMLURIConstants.XMLDSIG_NSURI.get(), "SignedInfo");
        return nl != null ? nl.getLength() > 0 : false;
    }

    /**
     * Sign a node in a document
     *
     * @param doc Document
     * @param parentOfNodeToBeSigned Parent Node of the node to be signed
     * @param signingKey Private Key
     * @param certificate X509 Certificate holding the public key
     * @param digestMethod (Example: DigestMethod.SHA1)
     * @param signatureMethod (Example: SignatureMethod.DSA_SHA1)
     * @param referenceURI
     * @return Document that contains the signed node
     * @throws XMLSignatureException
     * @throws MarshalException
     * @throws GeneralSecurityException
     * @throws ParserConfigurationException
     */
    public static Document sign(Document doc, Node parentOfNodeToBeSigned, PrivateKey signingKey, X509Certificate certificate,
            String digestMethod, String signatureMethod, String referenceURI) throws ParserConfigurationException,
            GeneralSecurityException, MarshalException, XMLSignatureException {
        KeyPair keyPair = new KeyPair(certificate.getPublicKey(), signingKey);
        return sign(doc, parentOfNodeToBeSigned, keyPair, digestMethod, signatureMethod, referenceURI);
    }

    /**
     * Sign a node in a document
     *
     * @param doc
     * @param nodeToBeSigned
     * @param keyPair
     * @param publicKey
     * @param digestMethod
     * @param signatureMethod
     * @param referenceURI
     * @return
     * @throws ParserConfigurationException
     * @throws XMLSignatureException
     * @throws MarshalException
     * @throws GeneralSecurityException
     */
    public static Document sign(Document doc, Node nodeToBeSigned, KeyPair keyPair, String digestMethod,
            String signatureMethod, String referenceURI) throws ParserConfigurationException, GeneralSecurityException,
            MarshalException, XMLSignatureException {
        if (nodeToBeSigned == null)
            throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "Node to be signed");
        if (trace) {
            log.trace("Document to be signed=" + DocumentUtil.asString(doc));
        }

        Node parentNode = nodeToBeSigned.getParentNode();

        // Let us create a new Document
        Document newDoc = DocumentUtil.createDocument();
        // Import the node
        Node signingNode = newDoc.importNode(nodeToBeSigned, true);
        newDoc.appendChild(signingNode);

        if (!referenceURI.isEmpty()) {
            propagateIDAttributeSetup(nodeToBeSigned, newDoc.getDocumentElement());
        }
        newDoc = sign(newDoc, keyPair, digestMethod, signatureMethod, referenceURI);

        // if the signed element is a SAMLv2.0 assertion we need to move the signature element to the position
        // specified in the schema (before the assertion subject element).
        if (nodeToBeSigned.getLocalName().equals("Assertion")
                && WSTrustConstants.SAML2_ASSERTION_NS.equals(nodeToBeSigned.getNamespaceURI())) {
            Node signatureNode = DocumentUtil.getElement(newDoc, new QName(WSTrustConstants.DSIG_NS, "Signature"));
            Node subjectNode = DocumentUtil.getElement(newDoc, new QName(WSTrustConstants.SAML2_ASSERTION_NS, "Subject"));
            if (signatureNode != null && subjectNode != null) {
                newDoc.getDocumentElement().removeChild(signatureNode);
                newDoc.getDocumentElement().insertBefore(signatureNode, subjectNode);
            }
        }

        // Now let us import this signed doc into the original document we got in the method call
        Node signedNode = doc.importNode(newDoc.getFirstChild(), true);

        if (!referenceURI.isEmpty()) {
            propagateIDAttributeSetup(newDoc.getDocumentElement(), (Element) signedNode);
        }

        parentNode.replaceChild(signedNode, nodeToBeSigned);
        // doc.getDocumentElement().replaceChild(signedNode, nodeToBeSigned);

        return doc;
    }

    /**
     * Setup the ID attribute into <code>destElement</code> depending on the <code>isId</code> flag of an attribute of
     * <code>sourceNode</code>.
     *
     * @param sourceNode
     * @param destDocElement
     */
    public static void propagateIDAttributeSetup(Node sourceNode, Element destElement) {
        NamedNodeMap nnm = sourceNode.getAttributes();
        for (int i = 0; i < nnm.getLength(); i++) {
            Attr attr = (Attr) nnm.item(i);
            if (attr.isId()) {
                destElement.setIdAttribute(attr.getName(), true);
                break;
            }
        }
    }

    /**
     * Sign the root element
     *
     * @param doc
     * @param signingKey
     * @param publicKey
     * @param digestMethod
     * @param signatureMethod
     * @param referenceURI
     * @return
     * @throws GeneralSecurityException
     * @throws XMLSignatureException
     * @throws MarshalException
     */
    public static Document sign(Document doc, KeyPair keyPair, String digestMethod, String signatureMethod, String referenceURI)
            throws GeneralSecurityException, MarshalException, XMLSignatureException {
        if (trace) {
            log.trace("Document to be signed=" + DocumentUtil.asString(doc));
        }
        PrivateKey signingKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        DOMSignContext dsc = new DOMSignContext(signingKey, doc.getDocumentElement());
        dsc.setDefaultNamespacePrefix("dsig");

        DigestMethod digestMethodObj = fac.newDigestMethod(digestMethod, null);
        Transform transform1 = fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null);
        Transform transform2 = fac.newTransform("http://www.w3.org/2001/10/xml-exc-c14n#", (TransformParameterSpec) null);

        List<Transform> transformList = new ArrayList<Transform>();
        transformList.add(transform1);
        transformList.add(transform2);

        Reference ref = fac.newReference(referenceURI, digestMethodObj, transformList, null, null);

        CanonicalizationMethod canonicalizationMethod = fac.newCanonicalizationMethod(canonicalizationMethodType,
                (C14NMethodParameterSpec) null);

        List<Reference> referenceList = Collections.singletonList(ref);
        SignatureMethod signatureMethodObj = fac.newSignatureMethod(signatureMethod, null);
        SignedInfo si = fac.newSignedInfo(canonicalizationMethod, signatureMethodObj, referenceList);

        KeyInfoFactory kif = fac.getKeyInfoFactory();
        KeyValue kv = kif.newKeyValue(publicKey);
        KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kv));

        if (!includeKeyInfoInSignature) {
            ki = null;
        }
        XMLSignature signature = fac.newXMLSignature(si, ki);

        signature.sign(dsc);

        return doc;
    }

    /**
     * Sign the root element
     *
     * @param doc
     * @param signingKey
     * @param publicKey
     * @param digestMethod
     * @param signatureMethod
     * @param referenceURI
     * @return
     * @throws GeneralSecurityException
     * @throws XMLSignatureException
     * @throws MarshalException
     */
    public static Document sign(SignatureUtilTransferObject dto) throws GeneralSecurityException, MarshalException,
    XMLSignatureException {
        Document doc = dto.getDocumentToBeSigned();
        KeyPair keyPair = dto.getKeyPair();
        Node nextSibling = dto.getNextSibling();
        String digestMethod = dto.getDigestMethod();
        String referenceURI = dto.getReferenceURI();
        String signatureMethod = dto.getSignatureMethod();

        if (trace) {
            log.trace("Document to be signed=" + DocumentUtil.asString(doc));
        }
        PrivateKey signingKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        DOMSignContext dsc = new DOMSignContext(signingKey, doc.getDocumentElement(), nextSibling);
        dsc.setDefaultNamespacePrefix("dsig");

        DigestMethod digestMethodObj = fac.newDigestMethod(digestMethod, null);
        Transform transform1 = fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null);
        Transform transform2 = fac.newTransform("http://www.w3.org/2001/10/xml-exc-c14n#", (TransformParameterSpec) null);

        List<Transform> transformList = new ArrayList<Transform>();
        transformList.add(transform1);
        transformList.add(transform2);

        Reference ref = fac.newReference(referenceURI, digestMethodObj, transformList, null, null);

        CanonicalizationMethod canonicalizationMethod = fac.newCanonicalizationMethod(canonicalizationMethodType,
                (C14NMethodParameterSpec) null);

        List<Reference> referenceList = Collections.singletonList(ref);
        SignatureMethod signatureMethodObj = fac.newSignatureMethod(signatureMethod, null);
        SignedInfo si = fac.newSignedInfo(canonicalizationMethod, signatureMethodObj, referenceList);

        KeyInfoFactory kif = fac.getKeyInfoFactory();
        KeyValue kv = kif.newKeyValue(publicKey);
        KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kv));

        if (!includeKeyInfoInSignature) {
            ki = null;
        }
        XMLSignature signature = fac.newXMLSignature(si, ki);

        signature.sign(dsc);

        return doc;
    }

    /**
     * Validate a signed document with the given public key
     *
     * @param signedDoc
     * @param publicKey
     * @return
     * @throws MarshalException
     * @throws XMLSignatureException
     */
    @SuppressWarnings("unchecked")
    public static boolean validate(Document signedDoc, Key publicKey) throws MarshalException, XMLSignatureException {
        if (signedDoc == null)
            throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "Signed Document");

        propagateIDAttributeSetup(signedDoc.getDocumentElement(), signedDoc.getDocumentElement());

        NodeList nl = signedDoc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (nl == null || nl.getLength() == 0) {
            throw new IllegalArgumentException(ErrorCodes.NULL_VALUE + "Cannot find Signature element");
        }
        if (publicKey == null)
            throw new IllegalArgumentException(ErrorCodes.NULL_VALUE + "Public Key");

        DOMValidateContext valContext = new DOMValidateContext(publicKey, nl.item(0));
        XMLSignature signature = fac.unmarshalXMLSignature(valContext);

        boolean coreValidity = signature.validate(valContext);

        if (trace && !coreValidity) {
            boolean sv = signature.getSignatureValue().validate(valContext);
            log.trace("Signature validation status: " + sv);

            List<Reference> references = signature.getSignedInfo().getReferences();
            for (Reference ref : references) {
                log.trace("[Ref id=" + ref.getId() + ":uri=" + ref.getURI() + "]validity status:" + ref.validate(valContext));
            }
        }
        return coreValidity;
    }

    /**
     * Marshall a SignatureType to output stream
     *
     * @param signature
     * @param os
     * @throws SAXException
     * @throws JAXBException
     */
    public static void marshall(SignatureType signature, OutputStream os) throws JAXBException, SAXException {
        throw new RuntimeException("NYI");
        /*
         * JAXBElement<SignatureType> jsig = objectFactory.createSignature(signature); Marshaller marshaller =
         * JAXBUtil.getValidatingMarshaller(pkgName, schemaLocation); marshaller.marshal(jsig, os);
         */
    }

    /**
     * Marshall the signed document to an output stream
     *
     * @param signedDocument
     * @param os
     * @throws TransformerException
     */
    public static void marshall(Document signedDocument, OutputStream os) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer();
        trans.transform(DocumentUtil.getXMLSource(signedDocument), new StreamResult(os));
    }

    /**
     * Given the X509Certificate in the keyinfo element, get a {@link X509Certificate}
     *
     * @param certificateString
     * @return
     * @throws ProcessingException
     */
    public static X509Certificate getX509CertificateFromKeyInfoString(String certificateString) throws ProcessingException {
        X509Certificate cert = null;
        StringBuilder builder = new StringBuilder();
        builder.append("-----BEGIN CERTIFICATE-----\n").append(certificateString).append("\n-----END CERTIFICATE-----");

        String derFormattedString = builder.toString();

        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            ByteArrayInputStream bais = new ByteArrayInputStream(derFormattedString.getBytes());

            while (bais.available() > 0) {
                cert = (X509Certificate) cf.generateCertificate(bais);
            }
        } catch (java.security.cert.CertificateException e) {
            throw new ProcessingException(e);
        }
        return cert;
    }

    /**
     * Given a dsig:DSAKeyValue element, return {@link DSAKeyValueType}
     * @param element
     * @return
     * @throws ProcessingException
     */
    public static DSAKeyValueType getDSAKeyValue(Element element) throws ParsingException {
        DSAKeyValueType dsa = new DSAKeyValueType();
        NodeList nl  = element.getChildNodes();
        int length = nl.getLength();

        for(int i = 0; i < length; i++){
            Node node  = nl.item(i);
            if(node instanceof Element){
                Element childElement = (Element) node;
                String tag = childElement.getLocalName();
                
                byte[] text = childElement.getTextContent().getBytes();
                
                if(WSTrustConstants.XMLDSig.P.equals(tag)){
                    dsa.setP(text);
                } else if(WSTrustConstants.XMLDSig.Q.equals(tag)){
                    dsa.setQ(text);
                } else if(WSTrustConstants.XMLDSig.G.equals(tag)){
                    dsa.setG(text);
                } else if(WSTrustConstants.XMLDSig.Y.equals(tag)){
                    dsa.setY(text);
                } else if(WSTrustConstants.XMLDSig.SEED.equals(tag)){
                    dsa.setSeed(text);
                } else if(WSTrustConstants.XMLDSig.PGEN_COUNTER.equals(tag)){
                    dsa.setPgenCounter(text);
                }
            }
        }

        return dsa;
    }
    
    /**
     * Given a dsig:DSAKeyValue element, return {@link DSAKeyValueType}
     * @param element
     * @return
     * @throws ProcessingException
     */
    public static RSAKeyValueType getRSAKeyValue(Element element) throws ParsingException {
        RSAKeyValueType rsa = new RSAKeyValueType();
        NodeList nl  = element.getChildNodes();
        int length = nl.getLength();

        for(int i = 0; i < length; i++){
            Node node  = nl.item(i);
            if(node instanceof Element){
                Element childElement = (Element) node;
                String tag = childElement.getLocalName();
                
                byte[] text = childElement.getTextContent().getBytes();
                
                if(WSTrustConstants.XMLDSig.MODULUS.equals(tag)){
                    rsa.setModulus(text);
                } else if(WSTrustConstants.XMLDSig.EXPONENT.equals(tag)){
                    rsa.setExponent(text);
                }
            }
        }

        return rsa;
    }

    /**
     * <p>
     * Creates a {@code KeyValueType} that wraps the specified public key. This method supports DSA and RSA keys.
     * </p>
     *
     * @param key the {@code PublicKey} that will be represented as a {@code KeyValueType}.
     * @return the constructed {@code KeyValueType} or {@code null} if the specified key is neither a DSA nor a RSA key.
     */
    public static KeyValueType createKeyValue(PublicKey key) {
        if (key instanceof RSAPublicKey) {
            RSAPublicKey pubKey = (RSAPublicKey) key;
            byte[] modulus = pubKey.getModulus().toByteArray();
            byte[] exponent = pubKey.getPublicExponent().toByteArray();

            RSAKeyValueType rsaKeyValue = new RSAKeyValueType();
            rsaKeyValue.setModulus(Base64.encodeBytes(modulus).getBytes());
            rsaKeyValue.setExponent(Base64.encodeBytes(exponent).getBytes());
            return rsaKeyValue;
        } else if (key instanceof DSAPublicKey) {
            DSAPublicKey pubKey = (DSAPublicKey) key;
            byte[] P = pubKey.getParams().getP().toByteArray();
            byte[] Q = pubKey.getParams().getQ().toByteArray();
            byte[] G = pubKey.getParams().getG().toByteArray();
            byte[] Y = pubKey.getY().toByteArray();

            DSAKeyValueType dsaKeyValue = new DSAKeyValueType();
            dsaKeyValue.setP(Base64.encodeBytes(P).getBytes());
            dsaKeyValue.setQ(Base64.encodeBytes(Q).getBytes());
            dsaKeyValue.setG(Base64.encodeBytes(G).getBytes());
            dsaKeyValue.setY(Base64.encodeBytes(Y).getBytes());
            return dsaKeyValue;
        }
        throw new RuntimeException(ErrorCodes.UNSUPPORTED_TYPE);
    }
}