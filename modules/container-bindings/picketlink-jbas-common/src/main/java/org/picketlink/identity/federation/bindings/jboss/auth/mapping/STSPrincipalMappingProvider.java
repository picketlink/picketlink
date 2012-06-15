package org.picketlink.identity.federation.bindings.jboss.auth.mapping;

import java.security.Principal;
import java.util.Map;

import org.jboss.security.SimplePrincipal;
import org.jboss.security.mapping.MappingResult;
import org.jboss.security.mapping.providers.principal.AbstractPrincipalMappingProvider;
import org.picketlink.identity.federation.PicketLinkLogger;
import org.picketlink.identity.federation.PicketLinkLoggerFactory;
import org.picketlink.identity.federation.core.wstrust.auth.AbstractSTSLoginModule;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.BaseIDAbstractType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.w3c.dom.Element;

/**
 * <p>
 * This mapping provider looks at the NameID in the Assertion and returns a corresponding JBoss Principal for insertion into the
 * Subject.
 * </p>
 *
 * <h3>Configuration</h3>
 *
 * <pre>
 * {@code
 * <application-policy name="saml-issue-token">
 *   <authentication>
 *     <login-module code="org.picketlink.identity.federation.core.wstrust.auth.STSIssuingLoginModule" flag="required">
 *       <module-option name="configFile">/sts-client.properties</module-option>
 *       <module-option name="password-stacking">useFirstPass</module-option>
 *     </login-module>
 *   </authentication>
 *   <mapping>
 *     <mapping-module code="org.picketlink.identity.federation.bindings.jboss.auth.mapping.STSPrincipalMappingProvider" type="principal"/>
 *     <mapping-module code="org.picketlink.identity.federation.bindings.jboss.auth.mapping.STSGroupMappingProvider" type="role"/>
 *   </mapping>
 * </application-policy>
 * }
 * </pre>
 *
 * @author <a href="mailto:Babak@redhat.com">Babak Mozaffari</a>
 */
public class STSPrincipalMappingProvider extends AbstractPrincipalMappingProvider {
    
    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    private MappingResult<Principal> result;

    public void init(Map<String, Object> contextMap) {
        // No initialization needed
    }

    public void performMapping(Map<String, Object> contextMap, Principal principal) {
        if (contextMap == null) {
            logger.mappingContextNull();
        }

        Object tokenObject = contextMap.get(AbstractSTSLoginModule.SHARED_TOKEN);
        if (!(tokenObject instanceof Element)) {
            // With Tomcat SSO Valves, mapping providers DO get called automatically, so there may be no tokens and errors
            // should be expected and handled
            logger.authSharedTokenNotFound(Element.class.getName(), AbstractSTSLoginModule.SHARED_TOKEN);
        }

        try {
            Element tokenElement = (Element) tokenObject;
            AssertionType assertion = SAMLUtil.fromElement(tokenElement);
            SubjectType subject = assertion.getSubject();
            if (subject != null) {
                BaseIDAbstractType baseID = subject.getSubType().getBaseID();
                if (baseID != null && baseID instanceof NameIDType) {
                    NameIDType nameID = (NameIDType) baseID;
                    Principal mappedPrincipal = new SimplePrincipal(nameID.getValue());
                    result.setMappedObject(mappedPrincipal);
                    logger.authMappedPrincipal(mappedPrincipal.toString());
                    return;
                }
            }
        } catch (Exception e) {
            logger.authSAMLAssertionPasingFailed(e);
        }
    }

    public void setMappingResult(MappingResult<Principal> mappingResult) {
        this.result = mappingResult;
    }
}
