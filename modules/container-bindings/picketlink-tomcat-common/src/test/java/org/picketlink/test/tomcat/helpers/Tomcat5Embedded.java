/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors. 
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketlink.test.tomcat.helpers;

import java.net.InetAddress;

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Embedded;
import org.apache.tomcat.util.IntrospectionUtils;

/**
 * Embedded Tomcat Service
 * @author Anil.Saldhana@redhat.com
 * @since Nov 1, 2008
 */
public class Tomcat5Embedded
{
   Embedded embedded = null;
   Host host;
   
   String homePath = null; 
   
   StartStopLifeCycleListener listener = new StartStopLifeCycleListener();
   
   public void setHomePath( String path)
   {
      this.homePath = path;
   }
   
   public void createContext(String contextPath)
   {
      Context context = embedded.createContext(homePath, contextPath);
      context.setPrivileged(true);
      host.addChild(context);
   }
   
   public void startServer() throws Exception
   {
      if(this.homePath == null)
         throw new IllegalStateException("Catalina path not set");
       
      embedded = new Embedded();
      embedded.setCatalinaHome(homePath);
      
      //Create an Engine
      Engine engine = embedded.createEngine();
      engine.setDefaultHost("localhost");
      
      // Create a default virtual host
      this.host = this.embedded.createHost("localhost", this.homePath
              + "/webapps");
      engine.addChild(this.host);

      //Add the engine
      embedded.addEngine(engine);
      
      /*
       * embedded.createConnector(...)
       * seems to be broken.. it always returns a null connector.
       * see work around below
       */
      InetAddress address = null;
      Connector connector = null;
      try 
      {
          connector = new Connector();  
          connector.setScheme("http");
          connector.setSecure(false);
          address = InetAddress.getLocalHost();
          if (address != null) 
          {
              IntrospectionUtils.setProperty(connector, "address", ""
                      + address);
          }
          IntrospectionUtils.setProperty(connector, "port", "" + 8080);
          
      } 
      catch (Exception ex) 
      {
          ex.printStackTrace();
      }
      connector.setEnableLookups(false);

      embedded.addConnector(connector);
      embedded.addLifecycleListener(listener);
      // Start the embedded server
      embedded.start();
   }
   
   public boolean hasStarted()
   {
      return this.listener.isStarted();
   }
   
   public boolean hasStopped()
   {
      return this.listener.hasStopped();
   }
   
   public void stopServer() throws Exception
   {
      embedded.stop();
   }
   
   class StartStopLifeCycleListener implements LifecycleListener
   {
      private boolean started = false;
      private boolean stopped = true;
      
      public void lifecycleEvent(LifecycleEvent lifecycleEvent)
      {
         if(lifecycleEvent.getType().equals("start"))
         {
            started = true;
            stopped = false; 
         }
         else
            if(lifecycleEvent.getType().equals("stop"))
            {
               started = false;
               stopped = true; 
            }
      } 
      
      public boolean isStarted()
      {
         return started;
      }
      
      public boolean hasStopped()
      {
         return stopped;
      }
   } 
}