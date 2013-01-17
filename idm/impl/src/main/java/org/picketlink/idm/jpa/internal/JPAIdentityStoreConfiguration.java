package org.picketlink.idm.jpa.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.internal.util.properties.Property;
import org.picketlink.idm.internal.util.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.idm.internal.util.properties.query.NamedPropertyCriteria;
import org.picketlink.idm.internal.util.properties.query.PropertyCriteria;
import org.picketlink.idm.internal.util.properties.query.PropertyQueries;
import org.picketlink.idm.internal.util.properties.query.PropertyQuery;
import org.picketlink.idm.internal.util.properties.query.TypedPropertyCriteria;
import org.picketlink.idm.jpa.annotations.IDMAttribute;
import org.picketlink.idm.jpa.annotations.IDMProperty;
import org.picketlink.idm.jpa.annotations.PropertyType;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;

/**
 * This interface defines the configuration parameters for a JPA based IdentityStore implementation.
 * 
 * @author Shane Bryzak
 * 
 */
public class JPAIdentityStoreConfiguration extends IdentityStoreConfiguration implements JPAStoreConfiguration {

    // Discriminator constants
    private static final String DEFAULT_USER_IDENTITY_DISCRIMINATOR = "USER";
    private static final String DEFAULT_ROLE_IDENTITY_DISCRIMINATOR = "ROLE";
    private static final String DEFAULT_GROUP_IDENTITY_DISCRIMINATOR = "GROUP";
    private static final String DEFAULT_AGENT_IDENTITY_DISCRIMINATOR = "AGENT";

    private String identityTypeAgent = DEFAULT_AGENT_IDENTITY_DISCRIMINATOR;
    private String identityTypeUser = DEFAULT_USER_IDENTITY_DISCRIMINATOR;
    private String identityTypeRole = DEFAULT_ROLE_IDENTITY_DISCRIMINATOR;
    private String identityTypeGroup = DEFAULT_GROUP_IDENTITY_DISCRIMINATOR;

    // Supported Attribute types
    private static final String ATTRIBUTE_TYPE_TEXT = "text";
    private static final String ATTRIBUTE_TYPE_BOOLEAN = "boolean";
    private static final String ATTRIBUTE_TYPE_DATE = "date";
    private static final String ATTRIBUTE_TYPE_INT = "int";
    private static final String ATTRIBUTE_TYPE_LONG = "long";
    private static final String ATTRIBUTE_TYPE_FLOAT = "float";
    private static final String ATTRIBUTE_TYPE_DOUBLE = "double";

    /**
     * <p>
     * Defines a map with all {@link IdentityTypeHandler} with the specific logic to handle the different {@link IdentityType}
     * types.
     * </p>
     */
    private Map<String, IdentityTypeHandler<? extends IdentityType>> identityTypeStores = new HashMap<String, IdentityTypeHandler<? extends IdentityType>>();

    /**
     * Defines the feature set for this IdentityStore
     */
    private FeatureSet featureSet = new FeatureSet();

    /**
     * Model properties
     */
    private Map<PropertyType, Property<Object>> modelProperties = new HashMap<PropertyType, Property<Object>>();

    /*
     * Attribute properties
     */
    private Map<String, MappedAttribute> attributeProperties = new HashMap<String, MappedAttribute>();

    /**
     * Entity classes
     */
    private Class<?> identityClass;
    private Class<?> attributeClass;
    private Class<?> credentialClass;
    private Class<?> credentialAttributeClass;
    private Class<?> relationshipClass;
    private Class<?> relationshipIdentityClass;
    private Class<?> relationshipAttributeClass;
    private Class<?> partitionClass;

    public Class<?> getIdentityClass() {
        return identityClass;
    }

    public void setIdentityClass(Class<?> identityClass) {
        this.identityClass = identityClass;
    }

    public Class<?> getCredentialClass() {
        return credentialClass;
    }

    public void setCredentialClass(Class<?> credentialClass) {
        this.credentialClass = credentialClass;
    }
    
    public Class<?> getCredentialAttributeClass() {
        return this.credentialAttributeClass;
    }
    
    public void setCredentialAttributeClass(Class<?> credentialAttributeClass) {
        this.credentialAttributeClass = credentialAttributeClass;
    }

    public Class<?> getRelationshipClass() {
        return this.relationshipClass;
    }
    
    public Class<?> getPartitionClass() {
        return this.partitionClass;
    }

    public void setRelationshipClass(Class<?> relationshipClass) {
        this.relationshipClass = relationshipClass;
    }
    
    public void setPartitionClass(Class<?> partitionClass) {
        this.partitionClass = partitionClass;
    }


    public Class<?> getRelationshipIdentityClass() {
        return relationshipIdentityClass;
    }

    public void setRelationshipIdentityClass(Class<?> relationshipIdentityClass) {
        this.relationshipIdentityClass = relationshipIdentityClass;
    }

    public Class<?> getRelationshipAttributeClass() {
        return relationshipAttributeClass;
    }

    public void setRelationshipAttributeClass(Class<?> relationshipAttributeClass) {
        this.relationshipAttributeClass = relationshipAttributeClass;
    }

    public Class<?> getAttributeClass() {
        return attributeClass;
    }

    public void setAttributeClass(Class<?> attributeClass) {
        this.attributeClass = attributeClass;
    }

    public boolean isConfigured() {
        return identityClass != null;
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
            modelProperties.put(propertyType,  props.get(0));
        } else if (props.size() > 1) {
            throw new SecurityConfigurationException("Ambiguous " + propertyType.name() + " property in identity class [" +
                targetClass.getName() + "]");
        } else {
            if (possibleNames != null && possibleNames.length > 0) {
                Property<Object> p = findNamedProperty(targetClass, possibleNames);

                if (p != null) {
                    modelProperties.put(propertyType, p);
                }
            }

            if (!optional) {
                throw new SecurityConfigurationException("Error configuring JPAIdentityStore - no " + 
                    propertyType.name() + " property found in identity class [" + targetClass.getName() + "]");
            }
        }
    }

    protected Property<Object> findNamedProperty(Class<?> targetClass, String... allowedNames) {
        List<Property<Object>> props = PropertyQueries.createQuery(targetClass)
                .addCriteria(new TypedPropertyCriteria(String.class)).addCriteria(new NamedPropertyCriteria(allowedNames))
                .getResultList();

        for (String name : allowedNames) {
            for (Property<Object> prop : props) {
                if (name.equals(prop.getName()))
                    return prop;
            }
        }

        return null;
    }

    /**
     * Maps attributes to properties that are spread across the object model
     * 
     */
    public class MappedAttribute {
        /**
         * The property of the IdentityObject class that references the object that contains the attribute property
         */
        private Property<Object> identityProperty;

        /**
         * The property of the mapped object that contains the attribute value
         */
        private Property<Object> attributeProperty;

        public MappedAttribute(Property<Object> identityProperty, Property<Object> attributeProperty) {
            this.identityProperty = identityProperty;
            this.attributeProperty = attributeProperty;
        }

        public Property<Object> getIdentityProperty() {
            return identityProperty;
        }

        public Property<Object> getAttributeProperty() {
            return attributeProperty;
        }
    }

    public Property<Object> getModelProperty(PropertyType propertyType) {
        return modelProperties.get(propertyType);
    }

    public boolean isModelPropertySet(PropertyType propertyType) {
        return modelProperties.containsKey(propertyType);
    }

    public Map<String, MappedAttribute> getAttributeProperties() {
        return attributeProperties;
    }

    @Override
    public FeatureSet getFeatureSet() {
        return featureSet;
    }

    @Override
    public void init() throws SecurityConfigurationException {
        if (identityClass == null) {
            throw new SecurityConfigurationException("Error initializing JpaIdentityStore - identityClass not set");
        }

        configureIdentityTypeHandlers();
        configureIdentity();
        configurePartitions();
        configureUserProperties();
        configureRelationships();
        configureAttributes();
        configureCredentials();

        this.featureSet.addSupportedFeature(Feature.all);
        // Support all relationship types
        this.featureSet.addSupportedRelationship(Relationship.class); 
    }

    private void configureIdentityTypeHandlers() {
        this.identityTypeStores.put(getIdentityTypeDiscriminator(User.class), new UserHandler(this));
        this.identityTypeStores.put(getIdentityTypeDiscriminator(Agent.class), new AgentHandler(this));
        this.identityTypeStores.put(getIdentityTypeDiscriminator(Role.class), new RoleHandler(this));
        this.identityTypeStores.put(getIdentityTypeDiscriminator(Group.class), new GroupHandler(this));
    }

    private void configureCredentials() {
        if (this.credentialClass != null) {
            configureModelProperty(PropertyType.CREDENTIAL_TYPE, credentialClass, null);
            configureModelProperty(PropertyType.CREDENTIAL_VALUE, credentialClass, null);
            configureModelProperty(PropertyType.CREDENTIAL_IDENTITY, credentialClass, null);
            configureModelProperty(PropertyType.CREDENTIAL_EFFECTIVE_DATE, credentialClass, null);
            configureModelProperty(PropertyType.CREDENTIAL_EXPIRY_DATE, credentialClass, null);

            if (this.credentialAttributeClass != null) {
                configureModelProperty(PropertyType.CREDENTIAL_ATTRIBUTE_NAME, credentialAttributeClass, String.class);
                configureModelProperty(PropertyType.CREDENTIAL_ATTRIBUTE_VALUE, credentialAttributeClass, null);
                configureModelProperty(PropertyType.CREDENTIAL_ATTRIBUTE_CREDENTIAL, credentialAttributeClass, credentialClass);
            }
        }
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

        configureModelProperty(PropertyType.PARTITION_ID, partitionClass, null, "id", "id");
        configureModelProperty(PropertyType.PARTITION_TYPE, partitionClass, null, "type", "partitionType");
        configureModelProperty(PropertyType.PARTITION_NAME, partitionClass, null, "name");
        configureModelProperty(PropertyType.PARTITION_PARENT, partitionClass, null, "parent");
    }

    /**
     * Scan for various optional user properties, such as first name, last name and e-mail address.
     * 
     * @throws SecurityConfigurationException
     */
    protected void configureUserProperties() throws SecurityConfigurationException {
        configureModelProperty(PropertyType.USER_FIRST_NAME, identityClass, null, false, "firstName");
        configureModelProperty(PropertyType.USER_LAST_NAME, identityClass, null, false, "lastName");
        configureModelProperty(PropertyType.USER_EMAIL, identityClass, null, false, "email");
    }

    /*
     * Configures properties for reading and writing identity relationships. As this is an optional feature, the specified
     * relationshipClass property may be left as null in which case no configuration will occur.
     */
    protected void configureRelationships() throws SecurityConfigurationException {
        if (relationshipClass == null) {
            return;
        } else if (relationshipIdentityClass == null || relationshipAttributeClass == null) {
                throw new SecurityConfigurationException("Invalid JPAIdentityStoreConfiguration - " +
                        "Both relationshipIdentityClass and relationshipAttributeClass properties must be set " +
                        "if relationships are configured");
        }

        configureModelProperty(PropertyType.RELATIONSHIP_ID, relationshipClass, null, "id");
        configureModelProperty(PropertyType.RELATIONSHIP_CLASS, relationshipClass, null, "relationshipClass");

        configureModelProperty(PropertyType.RELATIONSHIP_IDENTITY, relationshipIdentityClass, null, "identity");
        configureModelProperty(PropertyType.RELATIONSHIP_DESCRIPTOR, relationshipIdentityClass, null, "descriptor");
        configureModelProperty(PropertyType.RELATIONSHIP_IDENTITY_RELATIONSHIP, relationshipIdentityClass, relationshipClass);

        configureModelProperty(PropertyType.RELATIONSHIP_ATTRIBUTE_NAME, relationshipAttributeClass, null, "attributeName", "name");
        configureModelProperty(PropertyType.RELATIONSHIP_ATTRIBUTE_VALUE, relationshipAttributeClass, null, "attributeValue", "value");
        configureModelProperty(PropertyType.RELATIONSHIP_ATTRIBUTE_RELATIONSHIP, relationshipAttributeClass, null);
    }

    /**
     * Configures the identity store for reading and writing attribute values
     * 
     * @throws SecurityConfigurationException
     */
    protected void configureAttributes() throws SecurityConfigurationException {
        // If an attribute class has been configured, scan it for attribute properties
        if (attributeClass == null) {
            return;
        }

        configureModelProperty(PropertyType.ATTRIBUTE_IDENTITY, attributeClass, identityClass);
        configureModelProperty(PropertyType.ATTRIBUTE_NAME, attributeClass, null, "attributeName", "name");
        configureModelProperty(PropertyType.ATTRIBUTE_TYPE, attributeClass, null, "attributeType", "type");
        configureModelProperty(PropertyType.ATTRIBUTE_VALUE, attributeClass, null, "attributeValue", "value");

        // Scan for attribute properties in the identity class
        List<Property<Object>> props = PropertyQueries.createQuery(identityClass)
                .addCriteria(new AnnotatedPropertyCriteria(IDMAttribute.class)).getResultList();

        for (Property<Object> p : props) {
            String attribName = p.getAnnotatedElement().getAnnotation(IDMAttribute.class).name();

            if (attributeProperties.containsKey(attribName)) {
                Property<Object> other = attributeProperties.get(attribName).getAttributeProperty();

                throw new SecurityConfigurationException("Multiple properties defined for attribute [" + attribName + "] - "
                        + "Property: " + other.getDeclaringClass().getName() + "." + other.getAnnotatedElement().toString()
                        + ", Property: " + p.getDeclaringClass().getName() + "." + p.getAnnotatedElement().toString());
            }

            attributeProperties.put(attribName, new MappedAttribute(null, p));
        }

        // scan any entity classes referenced by the identity class also
        // props = PropertyQueries.createQuery(identityClass).getResultList();
        //
        // for (Property<Object> p : props) {
        // if (!p.isReadOnly() && p.getJavaClass().isAnnotationPresent(Entity.class)) {
        //
        // List<Property<Object>> pp = PropertyQueries.createQuery(p.getJavaClass())
        // .addCriteria(new AnnotatedPropertyCriteria(IDMAttribute.class)).getResultList();
        //
        // for (Property<Object> attributeProperty : pp) {
        // String attribName = attributeProperty.getAnnotatedElement().getAnnotation(IDMAttribute.class)
        // .name();
        //
        // if (attributeProperties.containsKey(attribName)) {
        // Property<Object> other = attributeProperties.get(attribName).getAttributeProperty();
        //
        // throw new SecurityConfigurationException("Multiple properties defined for attribute ["
        // + attribName + "] - " + "Property: " + other.getDeclaringClass().getName() + "."
        // + other.getAnnotatedElement().toString() + ", Property: "
        // + attributeProperty.getDeclaringClass().getName() + "."
        // + attributeProperty.getAnnotatedElement().toString());
        // }
        //
        // attributeProperties.put(attribName, new MappedAttribute(p, attributeProperty));
        // }
        // }
        // }
    }

    public String getIdentityTypeUser() {
        return identityTypeUser;
    }

    public void setIdentityTypeUser(String identityTypeUser) {
        this.identityTypeUser = identityTypeUser;
    }

    public String getIdentityTypeGroup() {
        return identityTypeGroup;
    }

    public void setIdentityTypeGroup(String identityTypeGroup) {
        this.identityTypeGroup = identityTypeGroup;
    }

    public String getIdentityTypeRole() {
        return identityTypeRole;
    }

    public void setIdentityTypeRole(String identityTypeRole) {
        this.identityTypeRole = identityTypeRole;
    }

    public String getIdentityTypeAgent() {
        return identityTypeAgent;
    }

    public void setIdentityTypeAgent(String identityTypeAgent) {
        this.identityTypeAgent = identityTypeAgent;
    }

    protected String getIdentityTypeDiscriminator(Class<? extends IdentityType> identityType) {
        String discriminator = null;

        if (User.class.isAssignableFrom(identityType)) {
            discriminator = getIdentityTypeUser();
        } else if (Agent.class.isAssignableFrom(identityType)) {
            discriminator = getIdentityTypeAgent();
        } else if (Role.class.isAssignableFrom(identityType)) {
            discriminator = getIdentityTypeRole();
        } else if (Group.class.isAssignableFrom(identityType)) {
            discriminator = getIdentityTypeGroup();
        } else if (Agent.class.isAssignableFrom(identityType)) {
            discriminator = getIdentityTypeAgent();
        } else {
            throw new IdentityManagementException("No discriminator could be determined for type [" + identityType.getClass()
                    + "]");
        }

        return discriminator;
    }

    public Map<String, IdentityTypeHandler<? extends IdentityType>> getIdentityTypeStores() {
        return identityTypeStores;
    }

    @SuppressWarnings("unchecked")
    IdentityTypeHandler<IdentityType> getHandler(Class<? extends IdentityType> identityTypeClass) {
        IdentityTypeHandler<IdentityType> identityTypeManager = (IdentityTypeHandler<IdentityType>) getIdentityTypeStores()
                .get(getIdentityDiscriminator(identityTypeClass));
        return identityTypeManager;
    }

    IdentityTypeHandler<IdentityType> getHandler(String discriminator) {
        return (IdentityTypeHandler<IdentityType>) getIdentityTypeStores().get(discriminator);
    }

    String getIdentityDiscriminator(Class<? extends IdentityType> identityType) {
        return getIdentityTypeDiscriminator(identityType);
    }

}
