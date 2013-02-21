package org.picketlink.authentication;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.picketlink.Identity;
import org.picketlink.common.util.Base64;
import org.picketlink.common.util.StringUtil;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Digest;
import org.picketlink.idm.credential.DigestValidationException;

/**
 * 
 * @author Shane Bryzak
 *
 */
@ApplicationScoped
public class AuthenticationFilter implements Filter {

    @Inject Instance<Identity> identityInstance;
    @Inject Instance<DefaultLoginCredentials> credentials;

    public enum AuthType {BASIC, DIGEST}

    private AuthType authType = AuthType.BASIC;
    private String realm;

    public void setAuthType(String value) {
        authType = AuthType.valueOf(value);
    }

    public String getAuthType() {
        return authType.toString();
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        if (!HttpServletRequest.class.isInstance(request)) {
            throw new ServletException("This filter can only process HttpServletRequest requests.");
        }
        
        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Force session creation
        httpRequest.getSession();
        
        if (authType.equals(AuthType.BASIC)) {
            processBasicAuth(httpRequest, httpResponse, chain);
        } else if (authType.equals(AuthType.DIGEST)) {
            processDigestAuth(httpRequest, httpResponse, chain);
        }
    }
    
    private void processBasicAuth(HttpServletRequest request, 
            HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException
   {
      Identity identity = identityInstance.get();
      DefaultLoginCredentials creds = credentials.get();

      if (identity == null)
      {
         throw new ServletException("Identity not found - please ensure that the Identity component is created on startup.");
      }

      boolean requireAuth = false;

      String header = request.getHeader("Authorization");
      if (header != null && header.startsWith("Basic "))
      {
         String base64Token = header.substring(6);
         String token = new String(Base64.decode(base64Token));

         String username = "";
         String password = "";
         int delim = token.indexOf(":");

         if (delim != -1) 
         {
             username = token.substring(0, delim);
             password = token.substring(delim + 1);
         }

         creds.setUserId(username);

         // Only reauthenticate if username doesn't match Identity.username and user isn't authenticated
         if (!username.equals(creds.getUserId()) || !identity.isLoggedIn()) 
         {
            try
            {
               creds.setPassword(password);
               identity.login();
            }         
            catch (Exception ex)
            {
               //log.warn("Error authenticating: " + ex.getMessage());
               requireAuth = true;
            }  
         }
      }

      if (!identity.isLoggedIn())
      {
         requireAuth = true;
      }

      try
      {
         if (!requireAuth)
         {
            chain.doFilter(request, response);
            return;
         }
      }
      catch (SecurityException ex) 
      {
         requireAuth = true;
      }
      
      if ((requireAuth && !identity.isLoggedIn()))
      {
         response.addHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
         response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not authorized");         
      }               
   }
    
    private void processDigestAuth(HttpServletRequest request, 
            HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException
   {
      Identity identity = identityInstance.get();
      
      if (identity == null)
      {
         throw new ServletException("Identity not found - please ensure that the Identity component is created on startup.");
      }      
      
      DefaultLoginCredentials creds = credentials.get();
      
      boolean requireAuth = false;    
      boolean nonceExpired = false;
      
      String header = request.getHeader("Authorization");      
      if (header != null && header.startsWith("Digest "))
      {        
         String section212response = header.substring(7);

         String[] headerEntries = section212response.split(",");
         Map<String,String> headerMap = new HashMap<String,String>();
         for (String entry : headerEntries)
         {
            String[] vals = StringUtil.split(entry, "=");
            headerMap.put(vals[0].trim(), vals[1].replace("\"", "").trim());
         }

         Digest digest = new Digest();
         //digestRequest.setHttpMethod(request.getMethod());
         digest.setRealm(headerMap.get("realm"));         
         digest.setKey(key);
         digest.setNonce(headerMap.get("nonce"));
         digest.setUri(headerMap.get("uri"));
         digest.setClientDigest(headerMap.get("response"));
         digest.setQop(headerMap.get("qop"));
         digest.setNonceCount(headerMap.get("nc"));
         digest.setClientNonce(headerMap.get("cnonce"));

         try
         {
            digest.validate();
            request.getSession().setAttribute(DigestRequest.DIGEST_REQUEST, digestRequest);
            authenticate( request, headerMap.get("username") );
         }
         catch (DigestValidationException ex)
         {
            log.warn(String.format("Digest validation failed, header [%s]: %s",
                     section212response, ex.getMessage()));
            requireAuth = true;
            
            if (ex.isNonceExpired()) nonceExpired = true;
         }            
         catch (Exception ex)
         {
            log.warn("Error authenticating: " + ex.getMessage());
            requireAuth = true;
         }
      }   

      if (!identity.isLoggedIn() && !credentials.isSet())
      {
         requireAuth = true;
      }
      
      try
      {
         if (!requireAuth)
         {
            chain.doFilter(request, response);
            return;
         }
      }
      catch (SecurityException ex) 
      {
         requireAuth = true;
      }
      
      if ((requireAuth && !identity.isLoggedIn()))
      {      
         long expiryTime = System.currentTimeMillis() + (nonceValiditySeconds * 1000);
         
         String signatureValue = DigestUtils.md5Hex(expiryTime + ":" + key);
         String nonceValue = expiryTime + ":" + signatureValue;
         String nonceValueBase64 = Base64.encodeBytes(nonceValue.getBytes());

         // qop is quality of protection, as defined by RFC 2617.
         // we do not use opaque due to IE violation of RFC 2617 in not
         // representing opaque on subsequent requests in same session.
         String authenticateHeader = "Digest realm=\"" + realm + "\", " + "qop=\"auth\", nonce=\""
             + nonceValueBase64 + "\"";

         if (nonceExpired) authenticateHeader = authenticateHeader + ", stale=\"true\"";

         response.addHeader("WWW-Authenticate", authenticateHeader);
         response.sendError(HttpServletResponse.SC_UNAUTHORIZED);      
      }             
   }
    
    @Override
    public void destroy() {
        // TODO Auto-generated method stub
        
    }





}
