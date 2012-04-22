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
package org.picketlink.identity.federation.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.constants.PicketLinkFederationConstants;

/**
 * Utility dealing with Strings
 * @author Anil.Saldhana@redhat.com
 * @since Oct 21, 2009
 */
public class StringUtil
{
   /**
    * Check whether the passed string is null or empty
    * @param str
    * @return
    */
   public static boolean isNotNull(String str)
   {
      return str != null && !"".equals(str.trim());
   }

   /**
    * Check whether the string is null or empty
    * @param str
    * @return
    */
   public static boolean isNullOrEmpty(String str)
   {
      return str == null || str.isEmpty();
   }

   /**
    * <p>
    * Get the system property value if the string is of the format ${sysproperty}
    * </p>
    * <p>
    * You can insert default value when the system property is not set, by
    * separating it at the beginning with ::
    * </p>
    * <p>
    * <b>Examples:</b>
    * </p>
    * 
    * <p>
    * ${idp} should resolve to a value if the system property "idp" is set.
    * </p>
    * <p>
    * ${idp::http://localhost:8080} will resolve to http://localhost:8080 if the system property "idp" is not set.
    * </p>
    * @param str
    * @return
    */
   public static String getSystemPropertyAsString(String str)
   {
      if (str == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "str");
      if (str.contains("${"))
      {
         Pattern pattern = Pattern.compile("\\$\\{([^}]+)}");
         Matcher matcher = pattern.matcher(str);

         StringBuffer buffer = new StringBuffer();
         String sysPropertyValue = null;

         while (matcher.find())
         {
            String subString = matcher.group(1);
            String defaultValue = "";

            //Look for default value
            if (subString.contains("::"))
            {
               int index = subString.indexOf("::");
               defaultValue = subString.substring(index + 2);
               subString = subString.substring(0, index);
            }
            sysPropertyValue = SecurityActions.getSystemProperty(subString, defaultValue);
            if (sysPropertyValue.isEmpty())
            {
               throw new IllegalArgumentException(ErrorCodes.SYSTEM_PROPERTY_MISSING + matcher.group(1));
            }
            matcher.appendReplacement(buffer, sysPropertyValue);
         }

         matcher.appendTail(buffer);
         str = buffer.toString();
      }
      return str;
   }

   /**
    * Match two strings else throw a {@link RuntimeException}
    * @param first
    * @param second
    */
   public static void match(String first, String second)
   {
      if (first.equals(second) == false)
         throw new RuntimeException(ErrorCodes.NOT_EQUAL + first + " and " + second);
   }

   /**
    * Given a comma separated string, get the tokens as a {@link List}
    * @param str
    * @return
    */
   public static List<String> tokenize(String str)
   {
      return tokenize(str, ",");
   }

   /**
    * Given a delimited string, get the tokens as a {@link List}
    * @param str
    * @param delimiter  the delimiter
    * @return
    */
   public static List<String> tokenize(String str, String delimiter)
   {
      List<String> list = new ArrayList<String>();
      StringTokenizer tokenizer = new StringTokenizer(str, delimiter);
      while (tokenizer.hasMoreTokens())
      {
         list.add(tokenizer.nextToken());
      }
      return list;
   }

   /**
    * Given a string that is comma delimited and contains key-value pairs
    * @param keyValuePairString
    * @return
    */
   public static Map<String, String> tokenizeKeyValuePair(String keyValuePairString)
   {
      Map<String, String> map = new HashMap<String, String>();

      List<String> tokens = tokenize(keyValuePairString);
      for (String token : tokens)
      {
         int location = token.indexOf('=');
         map.put(token.substring(0, location), token.substring(location + 1));
      }
      return map;
   }

   /**
    * Given a masked password {@link String}, decode it
    * @param maskedString a password string that is masked
    * @param salt Salt
    * @param iterationCount Iteration Count
    * @return Decoded String
    * @throws Exception
    */
   public static String decode(String maskedString, String salt, int iterationCount) throws Exception
   {
      String pbeAlgo = PicketLinkFederationConstants.PBE_ALGORITHM;
      if (maskedString.startsWith(PicketLinkFederationConstants.PASS_MASK_PREFIX))
      {
         // Create the PBE secret key 
         SecretKeyFactory factory = SecretKeyFactory.getInstance(pbeAlgo);

         char[] password = "somearbitrarycrazystringthatdoesnotmatter".toCharArray();
         PBEParameterSpec cipherSpec = new PBEParameterSpec(salt.getBytes(), iterationCount);
         PBEKeySpec keySpec = new PBEKeySpec(password);
         SecretKey cipherKey = factory.generateSecret(keySpec);

         maskedString = maskedString.substring(PicketLinkFederationConstants.PASS_MASK_PREFIX.length());
         String decodedValue = PBEUtils.decode64(maskedString, pbeAlgo, cipherKey, cipherSpec);

         maskedString = decodedValue;
      }
      return maskedString;
   }
}