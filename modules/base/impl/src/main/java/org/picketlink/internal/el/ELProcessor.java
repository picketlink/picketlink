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
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ResourceBundleELResolver;
import javax.el.ValueExpression;
import javax.el.VariableMapper;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

/**
 * <p>Provides EL processing.</p>
 *
 * @author Pedro Igor
 */
@ApplicationScoped
public class ELProcessor {

    private ExpressionFactory expressionFactory;
    private CompositeELResolver elResolver;

    @Inject
    private BeanManager beanManager;

    @Inject
    private Instance<Identity> identityInstance;

    @Inject
    private Instance<PartitionManager> partitionManager;

    @Inject
    private Instance<IdentityManager> identityManagerInstance;

    @Inject
    private Instance<RelationshipManager> relationshipManagerInstance;

    public <R> R eval(String expression) {
        PicketLinkELContext context = new PicketLinkELContext(this.elResolver);
        ValueExpression valueExpression = this.expressionFactory.createValueExpression(context, expression, Object.class);

        R value;

        try {
            createEvaluationContext();
            value = (R) valueExpression.getValue(context);
        } finally {
            releaseEvaluationContext();
        }

        return value;
    }

    @Inject
    private void initialize() {
        this.expressionFactory = this.beanManager.wrapExpressionFactory(ExpressionFactory.newInstance());

        this.elResolver = new CompositeELResolver();
        this.elResolver.add(new PicketLinkELResolver(this.beanManager.getELResolver()));
        this.elResolver.add(new ArrayELResolver(false));
        this.elResolver.add(new ListELResolver(false));
        this.elResolver.add(new MapELResolver(false));
        this.elResolver.add(new ResourceBundleELResolver());
        this.elResolver.add(new BeanELResolver(false));
    }

    private void createEvaluationContext() {
        ELEvaluationContext evaluationContext = ELEvaluationContext.get();

        evaluationContext.setIdentity(this.identityInstance.get());
        evaluationContext.setPartitionManager(this.partitionManager.get());
    }

    private void releaseEvaluationContext() {
        ELEvaluationContext.release();
    }

    private class PicketLinkELContext extends ELContext {

        private final ELResolver elResolver;

        public PicketLinkELContext(ELResolver elResolver) {
            this.elResolver = elResolver;
        }

        @Override
        public ELResolver getELResolver() {
            return this.elResolver;
        }

        @Override
        public FunctionMapper getFunctionMapper() {
            return new PicketLinkFunctionMapper();
        }

        @Override
        public VariableMapper getVariableMapper() {
            return null;
        }
    }
}
