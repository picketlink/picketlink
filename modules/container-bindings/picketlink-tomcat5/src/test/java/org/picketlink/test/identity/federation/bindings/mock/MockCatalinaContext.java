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
package org.picketlink.test.identity.federation.bindings.mock;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.naming.directory.DirContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.catalina.Cluster;
import org.apache.catalina.Container;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Context;
import org.apache.catalina.Loader;
import org.apache.catalina.Manager;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Realm;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.catalina.deploy.ErrorPage;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.NamingResources;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.util.CharsetMapper;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.http.mapper.Mapper;

/**
 * Mock Catalina Context
 * @author Anil.Saldhana@redhat.com
 * @since Oct 20, 2009
 */
@SuppressWarnings(
{"unchecked", "rawtypes"})
public class MockCatalinaContext implements Context, Container, ServletContext
{
   private Realm realm;

   public void addChild(Container arg0)
   {
   }

   public void addContainerListener(ContainerListener arg0)
   {
   }

   public void addPropertyChangeListener(PropertyChangeListener arg0)
   {
   }

   public void backgroundProcess()
   {
   }

   public Container findChild(String arg0)
   {
      throw new RuntimeException("NYI");
   }

   public Container[] findChildren()
   {

      throw new RuntimeException("NYI");
   }

   public ContainerListener[] findContainerListeners()
   {

      throw new RuntimeException("NYI");
   }

   public int getBackgroundProcessorDelay()
   {

      return 0;
   }

   public Cluster getCluster()
   {

      throw new RuntimeException("NYI");
   }

   public String getInfo()
   {

      throw new RuntimeException("NYI");
   }

   public Loader getLoader()
   {

      throw new RuntimeException("NYI");
   }

   public Log getLogger()
   {

      throw new RuntimeException("NYI");
   }

   public Manager getManager()
   {

      throw new RuntimeException("NYI");
   }

   public Object getMappingObject()
   {

      throw new RuntimeException("NYI");
   }

   public String getName()
   {
      throw new RuntimeException("NYI");
   }

   public String getObjectName()
   {

      throw new RuntimeException("NYI");
   }

   public Container getParent()
   {
      return this;
   }

   public ClassLoader getParentClassLoader()
   {
      throw new RuntimeException("NYI");
   }

   public Pipeline getPipeline()
   {
      throw new RuntimeException("NYI");
   }

   public DirContext getResources()
   {
      throw new RuntimeException("NYI");
   }

   public void invoke(Request arg0, Response arg1) throws IOException, ServletException
   {
   }

   public void removeChild(Container arg0)
   {
   }

   public void removeContainerListener(ContainerListener arg0)
   {
   }

   public void removePropertyChangeListener(PropertyChangeListener arg0)
   {
   }

   public void setBackgroundProcessorDelay(int arg0)
   {
   }

   public void setCluster(Cluster arg0)
   {
   }

   public void setLoader(Loader arg0)
   {
   }

   public void setManager(Manager arg0)
   {
   }

   public void setName(String arg0)
   {
   }

   public void setParent(Container arg0)
   {
   }

   public void setParentClassLoader(ClassLoader arg0)
   {
   }

   public void setRealm(Realm arg0)
   {
      this.realm = arg0;
   }

   public void setResources(DirContext arg0)
   {
   }

   public void addApplicationListener(String arg0)
   {
   }

   public void addApplicationParameter(ApplicationParameter arg0)
   {
   }

   public void addConstraint(SecurityConstraint arg0)
   {
   }

   public void addErrorPage(ErrorPage arg0)
   {
   }

   public void addFilterDef(FilterDef arg0)
   {
   }

   public void addFilterMap(FilterMap arg0)
   {
   }

   public void addInstanceListener(String arg0)
   {
   }

   public void addJspMapping(String arg0)
   {
   }

   public void addLocaleEncodingMappingParameter(String arg0, String arg1)
   {
   }

   public void addMimeMapping(String arg0, String arg1)
   {
   }

   public void addParameter(String arg0, String arg1)
   {
   }

   public void addRoleMapping(String arg0, String arg1)
   {
   }

   public void addSecurityRole(String arg0)
   {
   }

   public void addServletMapping(String arg0, String arg1)
   {
   }

   public void addTaglib(String arg0, String arg1)
   {
   }

   public void addWatchedResource(String arg0)
   {
   }

   public void addWelcomeFile(String arg0)
   {
   }

   public void addWrapperLifecycle(String arg0)
   {
   }

   public void addWrapperListener(String arg0)
   {
   }

   public Wrapper createWrapper()
   {
      throw new RuntimeException("NYI");
   }

   public String[] findApplicationListeners()
   {
      throw new RuntimeException("NYI");
   }

   public ApplicationParameter[] findApplicationParameters()
   {
      throw new RuntimeException("NYI");
   }

   public SecurityConstraint[] findConstraints()
   {
      throw new RuntimeException("NYI");
   }

   public ErrorPage findErrorPage(int arg0)
   {
      throw new RuntimeException("NYI");
   }

   public ErrorPage findErrorPage(String arg0)
   {
      throw new RuntimeException("NYI");
   }

   public ErrorPage[] findErrorPages()
   {
      throw new RuntimeException("NYI");
   }

   public FilterDef findFilterDef(String arg0)
   {
      throw new RuntimeException("NYI");
   }

   public FilterDef[] findFilterDefs()
   {
      throw new RuntimeException("NYI");
   }

   public FilterMap[] findFilterMaps()
   {
      throw new RuntimeException("NYI");
   }

   public String[] findInstanceListeners()
   {
      throw new RuntimeException("NYI");
   }

   public String findMimeMapping(String arg0)
   {
      throw new RuntimeException("NYI");
   }

   public String[] findMimeMappings()
   {
      throw new RuntimeException("NYI");
   }

   public String findParameter(String arg0)
   {
      throw new RuntimeException("NYI");
   }

   public String[] findParameters()
   {
      throw new RuntimeException("NYI");
   }

   public String findRoleMapping(String arg0)
   {
      throw new RuntimeException("NYI");
   }

   public boolean findSecurityRole(String arg0)
   {
      return false;
   }

   public String[] findSecurityRoles()
   {
      throw new RuntimeException("NYI");
   }

   public String findServletMapping(String arg0)
   {
      throw new RuntimeException("NYI");
   }

   public String[] findServletMappings()
   {
      throw new RuntimeException("NYI");
   }

   public String findStatusPage(int arg0)
   {
      throw new RuntimeException("NYI");
   }

   public int[] findStatusPages()
   {
      throw new RuntimeException("NYI");
   }

   public String findTaglib(String arg0)
   {
      throw new RuntimeException("NYI");
   }

   public String[] findTaglibs()
   {
      throw new RuntimeException("NYI");
   }

   public String[] findWatchedResources()
   {
      throw new RuntimeException("NYI");
   }

   public boolean findWelcomeFile(String arg0)
   {
      return false;
   }

   public String[] findWelcomeFiles()
   {
      throw new RuntimeException("NYI");
   }

   public String[] findWrapperLifecycles()
   {
      throw new RuntimeException("NYI");
   }

   public String[] findWrapperListeners()
   {
      throw new RuntimeException("NYI");
   }

   public String getAltDDName()
   {
      throw new RuntimeException("NYI");
   }

   public Object[] getApplicationEventListeners()
   {
      throw new RuntimeException("NYI");
   }

   public Object[] getApplicationLifecycleListeners()
   {
      throw new RuntimeException("NYI");
   }

   public boolean getAvailable()
   {
      return false;
   }

   public CharsetMapper getCharsetMapper()
   {
      throw new RuntimeException("NYI");
   }

   public String getConfigFile()
   {
      throw new RuntimeException("NYI");
   }

   public boolean getConfigured()
   {
      return false;
   }

   public boolean getCookies()
   {
      return false;
   }

   public boolean getCrossContext()
   {
      return false;
   }

   public String getDisplayName()
   {
      throw new RuntimeException("NYI");
   }

   public boolean getDistributable()
   {
      return false;
   }

   public String getDocBase()
   {
      throw new RuntimeException("NYI");
   }

   public String getEncodedPath()
   {
      throw new RuntimeException("NYI");
   }

   public boolean getIgnoreAnnotations()
   {
      return false;
   }

   public LoginConfig getLoginConfig()
   {
      LoginConfig loginConfig = new LoginConfig();
      loginConfig.setAuthMethod("BASIC");
      return loginConfig;
   }

   public Mapper getMapper()
   {
      throw new RuntimeException("NYI");
   }

   public NamingResources getNamingResources()
   {
      throw new RuntimeException("NYI");
   }

   public boolean getOverride()
   {
      return false;
   }

   public String getPath()
   {
      throw new RuntimeException("NYI");
   }

   public boolean getPrivileged()
   {
      return false;
   }

   public String getPublicId()
   {
      throw new RuntimeException("NYI");
   }

   public boolean getReloadable()
   {
      return false;
   }

   public ServletContext getServletContext()
   {
      return this;
   }

   public int getSessionTimeout()
   {
      return 0;
   }

   public boolean getSwallowOutput()
   {
      return false;
   }

   public boolean getTldNamespaceAware()
   {
      return false;
   }

   public boolean getTldValidation()
   {
      return false;
   }

   public String getWrapperClass()
   {
      throw new RuntimeException("NYI");
   }

   public boolean getXmlNamespaceAware()
   {
      return false;
   }

   public boolean getXmlValidation()
   {
      return false;
   }

   public void reload()
   {
   }

   public void removeApplicationListener(String arg0)
   {
   }

   public void removeApplicationParameter(String arg0)
   {
   }

   public void removeConstraint(SecurityConstraint arg0)
   {
   }

   public void removeErrorPage(ErrorPage arg0)
   {
   }

   public void removeFilterDef(FilterDef arg0)
   {
   }

   public void removeFilterMap(FilterMap arg0)
   {
   }

   public void removeInstanceListener(String arg0)
   {
   }

   public void removeMimeMapping(String arg0)
   {
   }

   public void removeParameter(String arg0)
   {
   }

   public void removeRoleMapping(String arg0)
   {
   }

   public void removeSecurityRole(String arg0)
   {
   }

   public void removeServletMapping(String arg0)
   {
   }

   public void removeTaglib(String arg0)
   {
   }

   public void removeWatchedResource(String arg0)
   {
   }

   public void removeWelcomeFile(String arg0)
   {
   }

   public void removeWrapperLifecycle(String arg0)
   {
   }

   public void removeWrapperListener(String arg0)
   {
   }

   public void setAltDDName(String arg0)
   {
   }

   public void setApplicationEventListeners(Object[] arg0)
   {
   }

   public void setApplicationLifecycleListeners(Object[] arg0)
   {
   }

   public void setAvailable(boolean arg0)
   {
   }

   public void setCharsetMapper(CharsetMapper arg0)
   {
   }

   public void setConfigFile(String arg0)
   {
   }

   public void setConfigured(boolean arg0)
   {
   }

   public void setCookies(boolean arg0)
   {
   }

   public void setCrossContext(boolean arg0)
   {
   }

   public void setDisplayName(String arg0)
   {
   }

   public void setDistributable(boolean arg0)
   {
   }

   public void setDocBase(String arg0)
   {
   }

   public void setIgnoreAnnotations(boolean arg0)
   {
   }

   public void setLoginConfig(LoginConfig arg0)
   {
   }

   public void setNamingResources(NamingResources arg0)
   {
   }

   public void setOverride(boolean arg0)
   {
   }

   public void setPath(String arg0)
   {
   }

   public void setPrivileged(boolean arg0)
   {
   }

   public void setPublicId(String arg0)
   {
   }

   public void setReloadable(boolean arg0)
   {
   }

   public void setSessionTimeout(int arg0)
   {
   }

   public void setSwallowOutput(boolean arg0)
   {
   }

   public void setTldNamespaceAware(boolean arg0)
   {
   }

   public void setTldValidation(boolean arg0)
   {
   }

   public void setWrapperClass(String arg0)
   {
   }

   public void setXmlNamespaceAware(boolean arg0)
   {
   }

   public void setXmlValidation(boolean arg0)
   {
   }

   public Realm getRealm()
   {
      return realm;
   }

   //Copied from MockServletContext
   private final Map params = new HashMap();

   private final Map attribs = new HashMap();

   public Object getAttribute(String arg0)
   {
      return attribs.get(arg0);
   }

   public Enumeration getAttributeNames()
   {
      return new Enumeration()
      {
         private final Iterator iter = attribs.entrySet().iterator();

         public boolean hasMoreElements()
         {
            return iter.hasNext();
         }

         public Object nextElement()
         {
            Entry<String, Object> entry = (Entry<String, Object>) iter.next();
            return entry.getValue();
         }
      };
   }

   public ServletContext getContext(String arg0)
   {
      throw new RuntimeException("NYI");
   }

   public String getContextPath()
   {
      throw new RuntimeException("NYI");
   }

   public String getInitParameter(String arg0)
   {
      return (String) params.get(arg0);
   }

   public Enumeration getInitParameterNames()
   {
      return new Enumeration()
      {
         private final Iterator iter = params.entrySet().iterator();

         public boolean hasMoreElements()
         {
            return iter.hasNext();
         }

         public Object nextElement()
         {
            Entry<String, Object> entry = (Entry<String, Object>) iter.next();
            return entry.getKey();
         }
      };
   }

   public int getMajorVersion()
   {
      return 0;
   }

   public String getMimeType(String arg0)
   {
      throw new RuntimeException("NYI");
   }

   public int getMinorVersion()
   {
      return 0;
   }

   public RequestDispatcher getNamedDispatcher(String arg0)
   {
      throw new RuntimeException("NYI");
   }

   public String getRealPath(String arg0)
   {
      return null;
   }

   public RequestDispatcher getRequestDispatcher(final String path)
   {
      return new RequestDispatcher()
      {

         public void include(ServletRequest arg0, ServletResponse arg1) throws ServletException, IOException
         {
         }

         public void forward(ServletRequest arg0, ServletResponse arg1) throws ServletException, IOException
         {
            if (arg0 instanceof MockCatalinaRequest) {
               MockCatalinaRequest mockRequest = (MockCatalinaRequest) arg0;
               
               mockRequest.setForwardPath(path);
            }
         }
      };
   }

   public URL getResource(String arg0) throws MalformedURLException
   {
      throw new RuntimeException("NYI");
   }

   public InputStream getResourceAsStream(String arg0)
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      return tcl.getResourceAsStream(arg0);
   }

   public Set getResourcePaths(String arg0)
   {
      throw new RuntimeException("NYI");
   }

   public String getServerInfo()
   {
      throw new RuntimeException("NYI");
   }

   public Servlet getServlet(String arg0) throws ServletException
   {
      throw new RuntimeException("NYI");
   }

   public String getServletContextName()
   {
      throw new RuntimeException("NYI");
   }

   public Enumeration getServletNames()
   {
      throw new RuntimeException("NYI");
   }

   public Enumeration getServlets()
   {
      throw new RuntimeException("NYI");
   }

   public void log(String arg0)
   {
   }

   public void log(Exception arg0, String arg1)
   {
   }

   public void log(String arg0, Throwable arg1)
   {
   }

   public void removeAttribute(String arg0)
   {
      this.attribs.remove(arg0);
   }

   public void setAttribute(String arg0, Object arg1)
   {
      this.attribs.put(arg0, arg1);
   }
}