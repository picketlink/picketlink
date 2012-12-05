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
import org.picketlink.idm.internal.util.properties.query.TypedPropertyCriteria;
import org.picketlink.idm.jpa.annotations.IDMProperty;
import org.picketlink.idm.jpa.annotations.PropertyType;

/**
 * Configuration for the JPA based partition store 
 * 
 * @author Shane Bryzak
 */
public class JPAPartitionStoreConfiguration extends PartitionStoreConfiguration {

    public static final String PROPERTY_PARTITION_ID = "PARTITION_ID";
    public static final String PROPERTY_PARTITION_NAME = "PARTITION_NAME";

    private Class<?> partitionClass;

    private Map<String, Property<Object>> partitionProperties = new HashMap<String, Property<Object>>();

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
    }

    protected void configurePartitions() throws SecurityConfigurationException {
        List<Property<Object>> props = PropertyQueries.createQuery(partitionClass)
                .addCriteria(new PropertyTypeCriteria(PropertyType.ID)).getResultList();

        if (props.size() == 1) {
            partitionProperties.put(PROPERTY_PARTITION_ID, props.get(0));
        } else if (props.size() > 1) {
            throw new SecurityConfigurationException("Ambiguous partition id property in partition class "
                    + partitionClass.getName());
        } else {
            Property<Object> p = findNamedProperty(partitionClass, "id", "identity");

            if (p != null) {
                partitionProperties.put(PROPERTY_PARTITION_ID, p);
            }
        }
    }

}
