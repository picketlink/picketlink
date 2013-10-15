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
package org.picketlink.identity.federation.ws.trust;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Java class for ParticipantsType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ParticipantsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Primary" type="{http://docs.oasis-open.org/ws-sx/ws-trust/200512/}ParticipantType"
 * minOccurs="0"/>
 *         &lt;element name="Participant" type="{http://docs.oasis-open.org/ws-sx/ws-trust/200512/}ParticipantType"
 * maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;any/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class ParticipantsType extends SimpleAnyType {

    protected ParticipantType primary;

    protected List<ParticipantType> participant = new ArrayList<ParticipantType>();

    /**
     * Gets the value of the primary property.
     *
     * @return possible object is {@link ParticipantType }
     */
    public ParticipantType getPrimary() {
        return primary;
    }

    /**
     * Sets the value of the primary property.
     *
     * @param value allowed object is {@link ParticipantType }
     */
    public void setPrimary(ParticipantType value) {
        this.primary = value;
    }

    public void add(ParticipantType p) {
        this.participant.add(p);
    }

    public boolean remove(ParticipantType p) {
        return this.participant.remove(p);
    }

    /**
     * Gets the value of the participant property.
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link ParticipantType }
     */
    public List<ParticipantType> getParticipant() {
        return Collections.unmodifiableList(this.participant);
    }
}