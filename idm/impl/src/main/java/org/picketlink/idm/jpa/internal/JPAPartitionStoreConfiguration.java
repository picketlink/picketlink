package org.picketlink.idm.jpa.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.PartitionStoreConfiguration;
import org.picketlink.idm.internal.util.properties.Property;
import org.picketlink.idm.internal.util.properties.query.NamedPropertyCriteria;
import org.picketlink.idm.internal.util.properties.query.PropertyCriteria;
import org.picketlink.idm.internal.util.properties.query.PropertyQueries;
import org.picketlink.idm.internal.util.properties.query.PropertyQuery;
import org.picketlink.idm.internal.util.properties.query.TypedPropertyCriteria;
import org.picketlink.idm.jpa.annotations.IDMProperty;
import org.picketlink.idm.jpa.annotations.PropertyType;

/**
 * Configuration for the JPA based partition store 
 * 
 * @author Shane Bryzak
 */
public class JPAPartitionStoreConfiguration extends PartitionStoreConfiguration implements JPAStoreConfiguration {

    private Class<?> partitionClass;

    private Map<PropertyType, Property<Object>> partitionProperties = new HashMap<PropertyType, Property<Object>>();

    private Class<?> identityClass;

    public Class<?> getPartitionClass() {
        return partitionClass;
    }

    public void setPartitionClass(Class<?> partitionClass) {
        this.partitionClass = partitionClass;
    }

    public class PropertyTypeCriteria implements PropertyCriteria {
        private PropertyType pt;

        public PropertyTypeCriteria(PropertyType pt) {
            this.pt = pt;
        }

        public boolean fieldMatches(Field f) {
            return f.isAnnotationPresent(IDMProperty.class) && f.getAnnotation(IDMProperty.class).value().equals(pt);
        }

        public boolean methodMatches(Method m) {
            return m.isAnnotationPresent(IDMProperty.class) && m.getAnnotation(IDMProperty.class).value().equals(pt);
        }
    }

    protected Property<Object> findNamedProperty(Class<?> targetClass, String... allowedNames) {
        List<Property<Object>> props = PropertyQueries.createQuery(targetClass)
                .addCriteria(new TypedPropertyCriteria(String.class))
                .addCriteria(new NamedPropertyCriteria(allowedNames)).getResultList();

        for (String name : allowedNames) {
            for (Property<Object> prop : props) {
                if (name.equals(prop.getName()))
                    return prop;
            }
        }

        return null;
    }

    @Override
    public void init() throws SecurityConfigurationException {
        if (partitionClass == null) {
            throw new SecurityConfigurationException("Error initializing JPAPartitionStore - partitionClass not set");
        }

        configurePartitions();
        configureIdentity();
    }
    
    protected void configureIdentity() throws SecurityConfigurationException {
        configureModelProperty(PropertyType.IDENTITY_DISCRIMINATOR, identityClass, null, 
                "discriminator", "identityType", "identityTypeName", "typeName", "type");
        configureModelProperty(PropertyType.IDENTITY_KEY, identityClass, null, "key");
        configureModelProperty(PropertyType.IDENTITY_ID, identityClass, null, "id", "identifier");
        configureModelProperty(PropertyType.IDENTITY_NAME, identityClass, null, "name");
        configureModelProperty(PropertyType.GROUP_PARENT, identityClass, null, "parentGroup", "parent");
        configureModelProperty(PropertyType.IDENTITY_ENABLED, identityClass, null, "enabled", "active");
        configureModelProperty(PropertyType.IDENTITY_CREATION_DATE, identityClass, null, false, "created", "creationDate");
        configureModelProperty(PropertyType.IDENTITY_EXPIRY_DATE, identityClass, null, false, "expires", "expiryDate");
        configureModelProperty(PropertyType.IDENTITY_PARTITION, identityClass, null, false, "partition");
        configureModelProperty(PropertyType.AGENT_LOGIN_NAME, identityClass, null, "loginName", "login");
    }

    protected void configurePartitions() {
        if (partitionClass == null) {
            return;
        }

        configureModelProperty(PropertyType.PARTITION_ID, partitionClass, null, "type", "id");
        configureModelProperty(PropertyType.PARTITION_TYPE, partitionClass, null, "type", "partitionType");
        configureModelProperty(PropertyType.PARTITION_NAME, partitionClass, null, "name");
        configureModelProperty(PropertyType.PARTITION_PARENT, partitionClass, null, "parent");
    }
    
    protected void configureModelProperty(PropertyType propertyType, Class<?> targetClass, Class<?> propertyClass, 
            String... possibleNames) {
        configureModelProperty(propertyType, targetClass, propertyClass, false, possibleNames);
    }
    
    protected void configureModelProperty(PropertyType propertyType, Class<?> targetClass, Class<?> propertyClass, 
            boolean optional, String... possibleNames) {
        PropertyQuery<Object> query = PropertyQueries.createQuery(targetClass);

        if (propertyType != null) {
            query.addCriteria(new PropertyTypeCriteria(propertyType));
        }

        if (propertyClass != null) {
            query.addCriteria(new TypedPropertyCriteria(propertyClass));
        }

        List<Property<Object>> props = query.getResultList();

        if (props.size() == 1) {
            partitionProperties.put(propertyType,  props.get(0));
        } else if (props.size() > 1) {
            throw new SecurityConfigurationException("Ambiguous " + propertyType.name() + " property in identity class [" +
                targetClass.getName() + "]");
        } else {
            if (possibleNames != null && possibleNames.length > 0) {
                Property<Object> p = findNamedProperty(targetClass, possibleNames);

                if (p != null) {
                    partitionProperties.put(propertyType, p);
                }
            }

            if (!optional) {
                throw new SecurityConfigurationException("Error configuring JPAIdentityStore - no " + 
                    propertyType.name() + " property found in identity class [" + targetClass.getName() + "]");
            }
        }
    }

    public Property<Object> getModelProperty(PropertyType propertyType) {
        return partitionProperties.get(propertyType);
    }

    @Override
    public Class<?> getIdentityClass() {
        return this.identityClass;
    }

    public void setIdentityClass(Class<?> identityClass) {
        this.identityClass = identityClass;
    }
}
