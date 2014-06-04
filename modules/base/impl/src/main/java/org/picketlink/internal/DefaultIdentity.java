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

/**
 * <p>Default {@link org.picketlink.Identity} implementation.</p>
 *
 * <p>By default, authentication state is session scoped. In this case, state is shared between different requests from the same user.</p>
 *
 * @author Shane Bryzak
 * @author Pedro Igor
 */
public class DefaultIdentity extends AbstractIdentity {
    private static final long serialVersionUID = 7402039728808874024L;
}
