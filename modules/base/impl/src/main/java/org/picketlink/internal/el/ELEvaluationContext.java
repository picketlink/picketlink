/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.picketlink.internal.el;

import org.picketlink.Identity;
import org.picketlink.authentication.levels.LevelFactory;
import org.picketlink.idm.PartitionManager;

/**
 * <p>{@link java.lang.ThreadLocal} used to share a execution context when invoking EL functions defined by {@link ELFunctionMethods}.</p>
 *
 * TODO: check if EL does not provide something similar so we can avoid this.
 *
 * @author Pedro Igor
 */
class ELEvaluationContext {

    private static final ThreadLocal<ELEvaluationContext> evaluationContext = new ThreadLocal<ELEvaluationContext>() {
        @Override
        protected ELEvaluationContext initialValue() {
            return new ELEvaluationContext();
        }
    };

    private Identity identity;
    private PartitionManager partitionManager;
    private LevelFactory levelFactory;

    static ELEvaluationContext get() {
        return evaluationContext.get();
    }

    static void release() {
        evaluationContext.remove();
    }

    void setIdentity(Identity identity) {
        this.identity = identity;
    }

    Identity getIdentity() {
        return identity;
    }

    void setPartitionManager(PartitionManager partitionManager) {
        this.partitionManager = partitionManager;
    }

    PartitionManager getPartitionManager() {
        return this.partitionManager;
    }

    void setLevelFactory(LevelFactory factory){
        levelFactory = factory;
    }

    LevelFactory getLevelFactory(){
        return levelFactory;
    }
}
