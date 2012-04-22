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
package org.picketlink.test.identity.federation.core.parser.saml;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.io.StringReader;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;

import org.picketlink.identity.federation.core.parsers.util.StaxParserUtil;

/**
 * Base class for the parser unit tests
 * @author Anil.Saldhana@redhat.com
 * @since Jun 30, 2011
 */
public class AbstractParserTest
{
   public void validateSchema(String value) throws Exception
   {
      System.setProperty("jaxp.debug", "true");
      Validator validator = StaxParserUtil.getSchemaValidator();
      assertNotNull(validator);
      validator.validate(new StreamSource(new StringReader(value)));
   }

   public void validateSchema(InputStream is) throws Exception
   {
      System.setProperty("jaxp.debug", "true");
      Validator validator = StaxParserUtil.getSchemaValidator();
      assertNotNull(validator);
      validator.validate(new StreamSource(is));
   }
}