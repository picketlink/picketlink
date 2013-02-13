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
package org.picketlink.identity.federation.core.saml.v2.holders;

import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.sts.PicketLinkCoreSTS;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;

/**
 * Holds essential information about an IDP for creating saml messages.
 *
 * @author Anil.Saldhana@redhat.com
 * @since Dec 10, 2008
 */
public class IDPInfoHolder {
    private String subjectConfirmationMethod = JBossSAMLURIConstants.SUBJECT_CONFIRMATION_BEARER.get();
    private String nameIDFormat = JBossSAMLURIConstants.NAMEID_FORMAT_TRANSIENT.get();
    private String nameIDFormatValue;

    private AssertionType assertion;

    private int assertionValidityDuration = 5; // 5 Minutes

    public int getAssertionValidityDuration() {
        return assertionValidityDuration;
    }

    public void setAssertionValidityDuration(int assertionValidityDuration) {
        this.assertionValidityDuration = assertionValidityDuration;
    }

    public String getSubjectConfirmationMethod() {
        return subjectConfirmationMethod;
    }

    public void setSubjectConfirmationMethod(String subjectConfirmationMethod) {
        this.subjectConfirmationMethod = subjectConfirmationMethod;
    }

    public String getNameIDFormat() {
        return nameIDFormat;
    }

    public void setNameIDFormat(String nameIDFormat) {
        this.nameIDFormat = nameIDFormat;
    }

    public String getNameIDFormatValue() {
        return nameIDFormatValue;
    }

    public void setNameIDFormatValue(String nameIDFormatValue) {
        this.nameIDFormatValue = nameIDFormatValue;
    }

    public AssertionType getAssertion() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(PicketLinkCoreSTS.rte);
        return assertion;
    }

    public void setAssertion(AssertionType assertion) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(PicketLinkCoreSTS.rte);
        this.assertion = assertion;
    }
}