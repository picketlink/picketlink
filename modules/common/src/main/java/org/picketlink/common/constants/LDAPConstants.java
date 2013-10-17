/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.common.constants;

/**
 * <p>Define commonly used ldap constants.</p>
 *
 * @author anil saldhana
 * @since Aug 31, 2012
 */
public interface LDAPConstants {

    String GIVENNAME = "givenname";
    String CN = "cn";
    String SN = "sn";
    String EMAIL = "mail";
    String MEMBER = "member";
    String MEMBER_OF = "memberOf";
    String OBJECT_CLASS = "objectclass";
    String UID = "uid";
    String GROUP_OF_NAMES = "groupOfNames";
    String GROUP_OF_ENTRIES = "groupOfEntries";
    String GROUP_OF_UNIQUE_NAMES = "groupOfUniqueNames";

    String COMMA = ",";
    String EQUAL = "=";
    String SPACE_STRING = " ";

    String CUSTOM_ATTRIBUTE_ENABLED = "enabled";
    String CUSTOM_ATTRIBUTE_CREATE_DATE = "createDate";
    String CUSTOM_ATTRIBUTE_EXPIRY_DATE = "expiryDate";
    String ENTRY_UUID = "entryUUID";
    String OBJECT_GUID = "objectGUID";
    String CREATE_TIMESTAMP = "createTimeStamp";

}