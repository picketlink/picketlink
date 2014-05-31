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

package org.picketlink.internal;

import org.picketlink.annotations.StatelessIdentity;

/**
 * <p>{@link org.picketlink.Identity} implementation providing a stateless behavior to the authentication process.</p>
 *
 * <p>Basically, this implementation is an alternative to the default behavior which uses the session scope to share the authentication
 * state for an user between different interactions or requests.</p>
 *
 * @see org.picketlink.annotations.StatelessIdentity
 * @see org.picketlink.internal.DefaultIdentity
 *
 * @author Pedro Igor
 */
@StatelessIdentity
public class DefaultStatelessIdentity extends AbstractIdentity {
    private static final long serialVersionUID = 7698208680810910473L;
}
