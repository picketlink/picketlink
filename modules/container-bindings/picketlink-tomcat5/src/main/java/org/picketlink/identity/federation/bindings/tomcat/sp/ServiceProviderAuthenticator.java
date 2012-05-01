package org.picketlink.identity.federation.bindings.tomcat.sp;

import java.io.IOException;
import java.util.List;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Response;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.config.AuthPropertyType;
import org.picketlink.identity.federation.core.config.KeyProviderType;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyManager;
import org.picketlink.identity.federation.core.saml.v2.holders.DestinationInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.CoreConfigUtil;
import org.picketlink.identity.federation.core.util.StringUtil;
import org.picketlink.identity.federation.web.process.ServiceProviderBaseProcessor;
import org.picketlink.identity.federation.web.util.HTTPRedirectUtil;
import org.picketlink.identity.federation.web.util.PostBindingUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil.RedirectBindingUtilDestHolder;
import org.w3c.dom.Document;

/**
 * Unified Service Provider Authenticator
 * @author anil saldhana
 */
public class ServiceProviderAuthenticator extends AbstractSPFormAuthenticator {
    @Override
    protected boolean isPOSTBindingResponse() {
        return spConfiguration.isIdpUsesPostBinding();
    }

    @Override
    protected void sendRequestToIDP(String destination, Document samlDocument, String relayState, Response response,
            boolean willSendRequest) throws ProcessingException, ConfigurationException, IOException {
        String samlMessage = DocumentUtil.getDocumentAsString(samlDocument);
        
        if(spConfiguration.getBindingType().equalsIgnoreCase("POST")){ 
            samlMessage = PostBindingUtil.base64Encode(samlMessage);
            PostBindingUtil.sendPost(new DestinationInfoHolder(destination, samlMessage, relayState), response, willSendRequest);
        }else {
            String base64Request = RedirectBindingUtil.deflateBase64URLEncode(samlMessage.getBytes("UTF-8"));

            String destinationQuery = getDestinationQueryString(base64Request, relayState, willSendRequest);

            RedirectBindingUtilDestHolder holder = new RedirectBindingUtilDestHolder();
            holder.setDestination(destination).setDestinationQueryString(destinationQuery);

            String destinationURL = RedirectBindingUtil.getDestinationURL(holder);

            HTTPRedirectUtil.sendRedirectForRequestor(destinationURL, response);
        }
    }

    @Override
    protected String getBinding() {
        return spConfiguration.getBindingType();
    }
    
    @Override
    public void start() throws LifecycleException
    {
       super.start(); 

       if(doSupportSignature()){
           KeyProviderType keyProvider = this.spConfiguration.getKeyProvider();
           if (keyProvider == null)
              throw new LifecycleException(ErrorCodes.NULL_VALUE + "KeyProvider");
           try
           {
              String keyManagerClassName = keyProvider.getClassName();
              if (keyManagerClassName == null)
                 throw new RuntimeException(ErrorCodes.NULL_VALUE + "KeyManager class name");

              Class<?> clazz = SecurityActions.loadClass(getClass(), keyManagerClassName);
              if (clazz == null)
                 throw new RuntimeException(ErrorCodes.CLASS_NOT_LOADED + keyManagerClassName);

              this.keyManager = (TrustKeyManager) clazz.newInstance();

              List<AuthPropertyType> authProperties = CoreConfigUtil.getKeyProviderProperties(keyProvider);
              keyManager.setAuthProperties(authProperties);
              keyManager.setValidatingAlias(keyProvider.getValidatingAlias());

              /**
               * Since the user has explicitly configured the idp address, we need
               * to add an option on the keymanager such that users of keymanager
               * can choose the proper idp key for validation
               */
              if (StringUtil.isNotNull(idpAddress))
              {
                 keyManager.addAdditionalOption(ServiceProviderBaseProcessor.IDP_KEY, this.idpAddress);
              }
           }
           catch (Exception e)
           {
              log.error("Exception reading configuration:", e);
              throw new LifecycleException(e.getLocalizedMessage());
           }
           if (trace)
              log.trace("Key Provider=" + keyProvider.getClassName());   
       }
    }
}