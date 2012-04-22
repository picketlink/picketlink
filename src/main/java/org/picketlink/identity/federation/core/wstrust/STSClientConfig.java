/*
 * JBoss, Home of Professional Open Source Copyright 2009, Red Hat Middleware
 * LLC, and individual contributors by the @authors tag. See the copyright.txt
 * in the distribution for a full listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.picketlink.identity.federation.core.wstrust;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.constants.PicketLinkFederationConstants;
import org.picketlink.identity.federation.core.util.StringUtil;

/**
 * STSClientConfig has the ability to either programatically construct the configuration 
 * needed for {@link STSClient} or parse a file containing the configuration parameters.
 * <p/>
 * 
 * <h3>Configure programatically</h3>
 * Example:
 * <pre>{@code
 * Builder builder = new STSClientConfig.Builder();
 * builder.serviceName("PicketLinkSTS");
 * builder.portName("PicketLinkSTSPort");
 * ...
 * STSClientConfig config = builder.build();
 * }</pre>
 * 
 * <h3>Configure from file</h3>
 * Example:
 * <pre>{@code
 * STSClientConfig config = new STSClientConfig.Builder(configFile).build();
 * }</pre>
 * 
 * @author <a href="mailto:dbevenius@jboss.com">Daniel Bevenius</a>
 * @author Anil Saldhana
 */
public class STSClientConfig
{
   protected static Logger log = Logger.getLogger(STSClientConfig.class);

   protected static boolean trace = log.isTraceEnabled();

   public static final String DEFAULT_CONFIG_FILE = "sts-client.properties";

   public static final String SERVICE_NAME = "serviceName";

   public static final String PORT_NAME = "portName";

   public static final String ENDPOINT_ADDRESS = "endpointAddress";

   public static final String USERNAME = "username";

   public static final String PASSWORD = "password";

   public static final String TOKEN_TYPE = "tokenType";

   public static final String WSA_ISSUER = "wsaIssuer";

   public static final String WSP_APPLIES_TO = "wspAppliesTo";

   public static final String IS_BATCH = "isBatch";

   private final String serviceName;

   private final String portName;

   private final String endpointAddress;

   private final String username;

   private final String password;

   private final String wsaIssuer;

   private final String wspAppliesTo;

   private boolean isBatch = false; //Is the RST a batch request?

   private STSClientConfig(final Builder builder)
   {
      serviceName = builder.serviceName;
      portName = builder.portName;
      endpointAddress = builder.endpointAddress;
      username = builder.username;
      password = builder.password;
      isBatch = builder.isBatch;
      wsaIssuer = builder.wsaIssuer;
      wspAppliesTo = builder.wspAppliesTo;
   }

   public String getServiceName()
   {
      return serviceName;
   }

   public String getPortName()
   {
      return portName;
   }

   public String getEndPointAddress()
   {
      return endpointAddress;
   }

   public String getUsername()
   {
      return username;
   }

   public String getPassword()
   {
      return password;
   }

   public String getWsaIssuer()
   {
      return wsaIssuer;
   }

   public String getWspAppliesTo()
   {
      return wspAppliesTo;
   }

   public boolean isBatch()
   {
      return isBatch;
   }

   public String toString()
   {
      return getClass().getSimpleName() + "[serviceName=" + serviceName + ", portName=" + portName
            + ", endpointAddress=" + endpointAddress + "]";
   }

   public static class Builder
   {
      private String serviceName;

      private String portName;

      private String endpointAddress;

      private String username;

      private String password;

      private String wsaIssuer;

      private String wspAppliesTo;

      private boolean isBatch;

      public Builder()
      {
      }

      public Builder(final String configFile)
      {
         populate(configFile);
      }

      public Builder serviceName(final String serviceName)
      {
         this.serviceName = serviceName;
         return this;
      }

      public Builder portName(final String portName)
      {
         this.portName = portName;
         return this;
      }

      public Builder endpointAddress(final String address)
      {
         this.endpointAddress = address;
         return this;
      }

      public Builder username(final String username)
      {
         this.username = username;
         return this;
      }

      public Builder password(final String password)
      {
         this.password = password;
         return this;
      }

      public Builder wsaIssuer(final String wsa)
      {
         this.wsaIssuer = wsa;
         return this;
      }

      public Builder wspAppliesTo(final String wsp)
      {
         this.wspAppliesTo = wsp;
         return this;
      }

      public String getServiceName()
      {
         return serviceName;
      }

      public String getPortName()
      {
         return portName;
      }

      public String getEndpointAddress()
      {
         return endpointAddress;
      }

      public String getUsername()
      {
         return username;
      }

      public String getPassword()
      {
         return password;
      }

      public boolean isBatch()
      {
         return isBatch;
      }

      public void setBatch(boolean isBatch)
      {
         this.isBatch = isBatch;
      }

      public STSClientConfig build()
      {
         validate(this);
         return new STSClientConfig(this);
      }

      private void populate(final String configFile)
      {
         InputStream in = null;

         try
         {
            in = getResource(configFile);
            if (in == null)
            {
               throw new IllegalStateException(ErrorCodes.NULL_VALUE + "properties file " + configFile);

            }
            final Properties properties = new Properties();
            properties.load(in);
            this.serviceName = properties.getProperty(SERVICE_NAME);
            this.portName = properties.getProperty(PORT_NAME);
            this.endpointAddress = properties.getProperty(ENDPOINT_ADDRESS);
            this.username = properties.getProperty(USERNAME);
            this.password = properties.getProperty(PASSWORD);
            this.wsaIssuer = properties.getProperty(WSA_ISSUER);
            this.wspAppliesTo = properties.getProperty(WSP_APPLIES_TO);
            String batchStr = properties.getProperty(IS_BATCH);
            this.isBatch = StringUtil.isNotNull(batchStr) ? Boolean.parseBoolean(batchStr) : false;

            if (this.password.startsWith(PicketLinkFederationConstants.PASS_MASK_PREFIX))
            {
               //password is masked
               String salt = properties.getProperty(PicketLinkFederationConstants.SALT);
               int iterationCount = Integer.parseInt(properties
                     .getProperty(PicketLinkFederationConstants.ITERATION_COUNT));
               try
               {
                  this.password = StringUtil.decode(password, salt, iterationCount);
               }
               catch (Exception e)
               {
                  throw new RuntimeException(ErrorCodes.PROCESSING_EXCEPTION + "Unable to decode password:"
                        + this.password);
               }
            }
         }
         catch (IOException e)
         {
            throw new IllegalStateException(ErrorCodes.PROCESSING_EXCEPTION + "Could not load properties from "
                  + configFile);
         }
         finally
         {
            try
            {
               if (in != null)
                  in.close();
            }
            catch (final IOException ignored)
            {
               ignored.printStackTrace();
            }
         }
      }

      private void validate(Builder builder)
      {
         if (trace)
         {
            log.trace("Checkin ServiceName:");
         }
         checkPropertyShowValue(serviceName, SERVICE_NAME);

         if (trace)
         {
            log.trace("Checkin portName:");
         }
         checkPropertyShowValue(portName, PORT_NAME);

         if (trace)
         {
            log.trace("Checkin endpointAddress:");
         }
         checkPropertyShowValue(endpointAddress, endpointAddress);

         if (trace)
         {
            log.trace("Checkin username:");
         }
         checkProperty(username, USERNAME);

         if (trace)
         {
            log.trace("password portName:");
         }
         checkProperty(password, PASSWORD);
      }

      private void checkPropertyShowValue(final String propertyName, final String propertyValue)
      {
         if (propertyValue == null || propertyValue.equals(""))
            throw new IllegalArgumentException(ErrorCodes.NULL_VALUE + propertyName + " : was:" + propertyValue);
      }

      private void checkProperty(final String propertyName, final String propertyValue)
      {
         if (propertyValue == null || propertyValue.equals(""))
            throw new IllegalArgumentException(ErrorCodes.NULL_VALUE + propertyValue);
      }
   }

   private static InputStream getResource(String resource) throws IOException
   {
      // Try it as a File resource...
      final File file = new File(resource);

      if (file.exists() && !file.isDirectory())
      {
         return new FileInputStream(file);
      }
      // Try it as a classpath resource ...
      URL url = SecurityActions.loadResource(STSClientConfig.class, resource);
      if (url != null)
      {
         final InputStream is = url.openStream();
         if (is != null)
         {
            return is;
         }
      }

      return null;
   }
}