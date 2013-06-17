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

package org.picketlink.idm.jpa.internal;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.config.JPAIdentityStoreConfiguration;
import org.picketlink.idm.config.JPAIdentityStoreConfiguration.PropertyType;
import org.picketlink.idm.event.AbstractBaseEvent;
import org.picketlink.idm.event.GroupCreatedEvent;
import org.picketlink.idm.event.GroupDeletedEvent;
import org.picketlink.idm.event.GroupUpdatedEvent;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupMembership;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.spi.SecurityContext;
import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class GroupHandler extends IdentityTypeHandler<Group> {

    public GroupHandler() {
        getSortParametersMapping().put(Group.NAME, PropertyType.IDENTITY_NAME);
        // TODO: Parameter for parent group should be likely added as well...
    }

    @Override
    protected void doPopulateIdentityInstance(SecurityContext context, Object toIdentity, Group fromGroup,
            JPAIdentityStore store) {
        JPAIdentityStoreConfiguration jpaConfig = store.getConfig();

        jpaConfig.setModelPropertyValue(toIdentity, PropertyType.IDENTITY_PARTITION,
                store.lookupAndCreatePartitionObject(context, context.getPartition()), true);
        jpaConfig.setModelPropertyValue(toIdentity, PropertyType.IDENTITY_NAME, fromGroup.getName(), true);
        jpaConfig.setModelPropertyValue(toIdentity, PropertyType.GROUP_PATH, fromGroup.getPath(), true);

        if (fromGroup.getParentGroup() != null) {
            Object parentIdentity = store.lookupIdentityObjectById(context, fromGroup.getParentGroup().getId());

            if (parentIdentity == null) {
                parentIdentity = store.lookupIdentityObjectById(context, fromGroup.getParentGroup().getId());
            }

            jpaConfig.setModelPropertyValue(toIdentity, PropertyType.GROUP_PARENT, parentIdentity, true);
        }
    }

    @Override
    void remove(SecurityContext context, Object identity, Group identityType, JPAIdentityStore store) {
        disassociateChildren(context, identityType, store);
    }

    @Override
    protected AbstractBaseEvent raiseCreatedEvent(Group fromIdentityType) {
        return new GroupCreatedEvent(fromIdentityType);
    }

    @Override
    protected AbstractBaseEvent raiseUpdatedEvent(Group fromIdentityType) {
        return new GroupUpdatedEvent(fromIdentityType);
    }

    @Override
    protected AbstractBaseEvent raiseDeletedEvent(Group fromIdentityType) {
        return new GroupDeletedEvent(fromIdentityType);
    }

    /**
     * <p>
     * Disassociates the given {@link Group} from its children.
     * </p>
     *
     * @param group
     */
    private void disassociateChildren(SecurityContext context, Group group, JPAIdentityStore store) {
        EntityManager em = store.getEntityManager(context);
        JPAIdentityStoreConfiguration jpaConfig = store.getConfig();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(jpaConfig.getIdentityClass());
        Root<?> root = criteria.from(jpaConfig.getIdentityClass());
        List<Predicate> predicates = new ArrayList<Predicate>();

        Join<?, ?> join = root.join(jpaConfig.getModelProperty(PropertyType.GROUP_PARENT).getName());

        predicates.add(builder.equal(join.get(jpaConfig.getModelProperty(PropertyType.IDENTITY_NAME).getName()),
                group.getName()));

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        List<?> resultList = em.createQuery(criteria).getResultList();

        for (Object object : resultList) {
            jpaConfig.getModelProperty(PropertyType.GROUP_PARENT).setValue(object, null);
            em.merge(object);
        }
    }

    @Override
    protected Group doCreateIdentityType(SecurityContext context, Object identity, JPAIdentityStore store) {
        SimpleGroup group = null;

        JPAIdentityStoreConfiguration jpaConfig = store.getConfig();

        Object parentInstance = jpaConfig.getModelPropertyValue(Object.class, identity, PropertyType.GROUP_PARENT);

        String name = jpaConfig.getModelPropertyValue(String.class, identity, PropertyType.IDENTITY_NAME);

        if (parentInstance != null) {
            String groupPath = jpaConfig.getModelPropertyValue(String.class, parentInstance, PropertyType.GROUP_PATH);

            group = new SimpleGroup(name, store.getGroup(context, groupPath));
        } else {
            group = new SimpleGroup(name);
        }

        return group;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List<Predicate> getPredicate(SecurityContext context, JPACriteriaQueryBuilder criteria, JPAIdentityStore store) {
        List<Predicate> predicates = super.getPredicate(context, criteria, store);
        CriteriaBuilder builder = criteria.getBuilder();
        JPAIdentityStoreConfiguration jpaConfig = store.getConfig();

        Object[] parameterValues = criteria.getIdentityQuery().getParameter(Group.NAME);

        if (parameterValues != null) {
            predicates.add(builder.equal(
                    criteria.getRoot().get(jpaConfig.getModelProperty(PropertyType.IDENTITY_NAME).getName()),
                    parameterValues[0]));
        }

        parameterValues = criteria.getIdentityQuery().getParameter(Group.PATH);

        if (parameterValues != null) {
            predicates.add(builder.equal(criteria.getRoot().get(jpaConfig.getModelProperty(PropertyType.GROUP_PATH).getName()),
                    parameterValues[0]));
        }

        parameterValues = criteria.getIdentityQuery().getParameter(Group.PARENT);

        if (parameterValues != null) {
            Join<Object, Object> join = criteria.getRoot()
                    .join(jpaConfig.getModelProperty(PropertyType.GROUP_PARENT).getName());

            predicates.add(builder.equal(join.get(jpaConfig.getModelProperty(PropertyType.IDENTITY_NAME).getName()),
                    parameterValues[0]));
        }

        parameterValues = criteria.getIdentityQuery().getParameter(Group.HAS_MEMBER);

        if (parameterValues != null) {
            for (Object object : parameterValues) {
                if (Agent.class.isInstance(object)) {
                    RelationshipQuery<GroupMembership> query = context.getIdentityManager().createRelationshipQuery(
                            GroupMembership.class);

                    query.setParameter(GroupMembership.MEMBER, object);

                    List<GroupMembership> resultList = query.getResultList();

                    if (!resultList.isEmpty()) {
                        List<String> relIds = new ArrayList<String>();

                        for (GroupMembership memberships : resultList) {
                            relIds.add(memberships.getGroup().getId());
                        }

                        predicates.add(criteria.getRoot().get(jpaConfig.getModelProperty(PropertyType.IDENTITY_ID).getName())
                                .in(relIds));
                    } else {
                        predicates.add(criteria.getBuilder().equal(
                                criteria.getRoot().get(jpaConfig.getModelProperty(PropertyType.IDENTITY_ID).getName()), "-1"));
                    }
                } else if (Group.class.isInstance(object)) {
                    Group childGroup = (Group) object;

                    if (childGroup != null && childGroup.getParentGroup() != null) {
                        Object childObject = null;

                        try {
                            childObject = store.lookupIdentityObjectById(context, childGroup.getId());

                            List<Object> parents = getParentGroups(criteria, store, builder, childObject);

                            predicates.add(criteria.getRoot().in(parents));
                        } catch (IdentityManagementException ime) {
                            // the type may not exists
                        }
                    } else {
                        predicates.add(criteria.getBuilder().equal(
                                criteria.getRoot().get(jpaConfig.getModelProperty(PropertyType.IDENTITY_ID).getName()), "-1"));
                    }
                } else {
                    throw MESSAGES.queryUnsupportedParameterValue("Group.HAS_MEMBER", object);
                }
            }
        }

        return predicates;
    }

    private List<Object> getParentGroups(JPACriteriaQueryBuilder criteria, JPAIdentityStore store, CriteriaBuilder builder,
            Object childGroup) {
        List<Object> parents = new ArrayList<Object>();
        Object parent = store.getConfig().getModelProperty(PropertyType.GROUP_PARENT).getValue(childGroup);

        if (parent != null) {
            parents.add(parent);
            parents.addAll(getParentGroups(criteria, store, builder, parent));
        }

        return parents;
    }

}
