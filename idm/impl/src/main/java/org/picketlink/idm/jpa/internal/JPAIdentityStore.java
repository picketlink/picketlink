package org.picketlink.idm.jpa.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EntityManager;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.credential.Credential;
import org.picketlink.idm.internal.util.properties.Property;
import org.picketlink.idm.internal.util.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.idm.internal.util.properties.query.NamedPropertyCriteria;
import org.picketlink.idm.internal.util.properties.query.PropertyCriteria;
import org.picketlink.idm.internal.util.properties.query.PropertyQueries;
import org.picketlink.idm.internal.util.properties.query.TypedPropertyCriteria;
import org.picketlink.idm.jpa.annotations.IDMAttribute;
import org.picketlink.idm.jpa.annotations.IDMProperty;
import org.picketlink.idm.jpa.annotations.PropertyType;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Membership;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.GroupQuery;
import org.picketlink.idm.query.MembershipQuery;
import org.picketlink.idm.query.Range;
import org.picketlink.idm.query.RoleQuery;
import org.picketlink.idm.query.UserQuery;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;
import org.picketlink.idm.spi.JPAIdentityStoreConfiguration;
import org.picketlink.idm.spi.JPAIdentityStoreSession;

/**
 * Implementation of IdentityStore that stores its state in a relational
 * database.
 * 
 * @author Shane Bryzak
 */
public class JPAIdentityStore implements IdentityStore {

    private static final String DEFAULT_USER_IDENTITY_DISCRIMINATOR = "USER";
    private static final String DEFAULT_ROLE_IDENTITY_DISCRIMINATOR = "ROLE";
    private static final String DEFAULT_GROUP_IDENTITY_DISCRIMINATOR = "GROUP";

    // Property keys

    // Properties common to all IdentityTypes
    private static final String PROPERTY_IDENTITY_DISCRIMINATOR = "IDENTITY_DISCRIMINATOR";
    private static final String PROPERTY_IDENTITY_KEY = "IDENTITY_KEY";
    private static final String PROPERTY_IDENTITY_ENABLED = "IDENTITY_ENABLED";
    private static final String PROPERTY_IDENTITY_CREATED = "IDENTITY_CREATED";
    private static final String PROPERTY_IDENTITY_EXPIRES = "IDENTITY_EXPIRES";

    // Properties specific to Users
    private static final String PROPERTY_USER_FIRST_NAME = "USER_FIRST_NAME";
    private static final String PROPERTY_USER_LAST_NAME = "USER_LAST_NAME";
    private static final String PROPERTY_USER_EMAIL = "USER_EMAIL";

    // Properties common to Users and Groups
    private static final String PROPERTY_IDENTITY_ID = "IDENTITY_ID";

    // Properties common to Groups and Roles
    private static final String PROPERTY_IDENTITY_NAME = "IDENTITY_NAME";

    // Properties for Groups only
    private static final String PROPERTY_PARENT_GROUP = "PARENT_GROUP";

    // Identity membership properties
    private static final String PROPERTY_MEMBERSHIP_MEMBER = "MEMBERSHIP_MEMBER";
    private static final String PROPERTY_MEMBERSHIP_ROLE = "MEMBERSHIP_ROLE";
    private static final String PROPERTY_MEMBERSHIP_GROUP = "MEMBERSHIP_GROUP";

    // Credential properties
    private static final String PROPERTY_CREDENTIAL_VALUE = "CREDENTIAL_VALUE";
    private static final String PROPERTY_CREDENTIAL_TYPE = "CREDENTIAL_TYPE";
    private static final String PROPERTY_CREDENTIAL_TYPE_NAME = "CREDENTIAL_TYPE_NAME";
    private static final String PROPERTY_CREDENTIAL_IDENTITY = "CREDENTIAL_IDENTITY";

    // Attribute properties
    private static final String PROPERTY_ATTRIBUTE_NAME = "ATTRIBUTE_NAME";
    private static final String PROPERTY_ATTRIBUTE_VALUE = "ATTRIBUTE_VALUE";
    private static final String PROPERTY_ATTRIBUTE_IDENTITY = "ATTRIBUTE_IDENTITY";
    private static final String PROPERTY_ATTRIBUTE_TYPE = "ATTRIBUTE_TYPE";

    private static final String ATTRIBUTE_TYPE_TEXT = "text";
    private static final String ATTRIBUTE_TYPE_BOOLEAN = "boolean";
    private static final String ATTRIBUTE_TYPE_DATE = "date";
    private static final String ATTRIBUTE_TYPE_INT = "int";
    private static final String ATTRIBUTE_TYPE_LONG = "long";
    private static final String ATTRIBUTE_TYPE_FLOAT = "float";
    private static final String ATTRIBUTE_TYPE_DOUBLE = "double";

    /**
     * Defines the feature set for this IdentityStore
     */
    private Set<Feature> featureSet = new HashSet<Feature>();

    // Entity classes
    private Class<?> identityClass;
    private Class<?> membershipClass;
    private Class<?> credentialClass;
    private Class<?> attributeClass;

    /**
     * Model properties
     */
    private Map<String, Property<Object>> modelProperties = new HashMap<String, Property<Object>>();

    /*
     * Attribute properties
     */
    private Map<String, MappedAttribute> attributeProperties = new HashMap<String, MappedAttribute>();

    private String identityTypeUser = DEFAULT_USER_IDENTITY_DISCRIMINATOR;
    private String identityTypeRole = DEFAULT_ROLE_IDENTITY_DISCRIMINATOR;
    private String identityTypeGroup = DEFAULT_GROUP_IDENTITY_DISCRIMINATOR;

    private class PropertyTypeCriteria implements PropertyCriteria {
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

    /**
     * Maps attributes to properties that are spread across the object model
     * 
     */
    private class MappedAttribute {
        /**
         * The property of the IdentityObject class that references the object
         * that contains the attribute property
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

    public void bootstrap(JPAIdentityStoreConfiguration config) throws SecurityConfigurationException {

        identityClass = config.getIdentityClass();

        if (identityClass == null) {
            throw new SecurityConfigurationException("Error initializing JpaIdentityStore - identityClass not set");
        }

        credentialClass = config.getCredentialClass();
        membershipClass = config.getMembershipClass();
        attributeClass = config.getAttributeClass();

        configureIdentityDiscriminator();
        configureIdentityKey();
        configureIdentityId();
        configureIdentityName();
        configureIdentityParentGroup();
        configureIdentityEnabled();
        configureIdentityCreationDate();
        configureIdentityExpiryDate();

        configureUserProperties();

        configureMemberships();

        configureAttributes();

        // configureCredentials();

        // if (namedRelationshipsSupported) {
        // configureRoleTypeName();
        // }

        // featuresMetaData = new FeaturesMetaDataImpl(
        // configurationContext.getStoreConfigurationMetaData(),
        // new HashSet<IdentityObjectSearchCriteriaType>(),
        // false,
        // namedRelationshipsSupported,
        // new HashSet<String>()
        // );
    }

    protected void configureIdentityDiscriminator() throws SecurityConfigurationException {
        List<Property<Object>> props = PropertyQueries.createQuery(identityClass)
                .addCriteria(new PropertyTypeCriteria(PropertyType.DISCRIMINATOR)).getResultList();

        if (props.size() == 1) {
            modelProperties.put(PROPERTY_IDENTITY_DISCRIMINATOR, props.get(0));
        } else if (props.size() > 1) {
            throw new SecurityConfigurationException("Ambiguous identity discriminator property in identity class "
                    + identityClass.getName());
        } else {
            Property<Object> p = findNamedProperty(identityClass, "discriminator", "identityType", "identityTypeName",
                    "typeName", "type");

            if (p != null) {
                modelProperties.put(PROPERTY_IDENTITY_DISCRIMINATOR, p);
            }
        }
    }

    protected void configureIdentityKey() throws SecurityConfigurationException {
        List<Property<Object>> props = PropertyQueries.createQuery(identityClass)
                .addCriteria(new PropertyTypeCriteria(PropertyType.KEY)).getResultList();

        if (props.size() == 1) {
            modelProperties.put(PROPERTY_IDENTITY_KEY, props.get(0));
        } else if (props.size() > 1) {
            throw new SecurityConfigurationException("Ambiguous identity key property in identity class "
                    + identityClass.getName());
        } else {
            props = PropertyQueries.createQuery(identityClass).addCriteria(new NamedPropertyCriteria("key"))
                    .getResultList();

            if (!props.isEmpty()) {
                modelProperties.put(PROPERTY_IDENTITY_KEY, props.get(0));
            } else {
                throw new SecurityConfigurationException(
                        "Error initializing JPAIdentityStore - no key property found in identity class "
                                + identityClass.getName());
            }
        }
    }

    protected void configureIdentityId() throws SecurityConfigurationException {
        List<Property<Object>> props = PropertyQueries.createQuery(identityClass)
                .addCriteria(new PropertyTypeCriteria(PropertyType.ID)).getResultList();

        if (props.size() == 1) {
            modelProperties.put(PROPERTY_IDENTITY_ID, props.get(0));
        } else if (props.size() > 1) {
            throw new SecurityConfigurationException("Ambiguous identity id property in identity class "
                    + identityClass.getName());
        } else {
            throw new SecurityConfigurationException(
                    "Error initializing JPAIdentityStore - no id property found in identity class "
                            + identityClass.getName());
        }
    }

    protected void configureIdentityName() throws SecurityConfigurationException {
        List<Property<Object>> props = PropertyQueries.createQuery(identityClass)
                .addCriteria(new PropertyTypeCriteria(PropertyType.NAME)).getResultList();

        if (props.size() == 1) {
            modelProperties.put(PROPERTY_IDENTITY_NAME, props.get(0));
        } else if (props.size() > 1) {
            throw new SecurityConfigurationException("Ambiguous identity name property in identity class "
                    + identityClass.getName());
        } else {
            Property<Object> prop = findNamedProperty(identityClass, "name");

            if (prop != null) {
                modelProperties.put(PROPERTY_IDENTITY_NAME, prop);
            } else {
                throw new SecurityConfigurationException(
                        "Error initializing JPAIdentityStore - no name property found in identity class "
                                + identityClass.getName());
            }
        }
    }

    protected void configureIdentityParentGroup() throws SecurityConfigurationException {
        List<Property<Object>> props = PropertyQueries.createQuery(identityClass)
                .addCriteria(new PropertyTypeCriteria(PropertyType.PARENT_GROUP)).getResultList();

        if (props.size() == 1) {
            modelProperties.put(PROPERTY_PARENT_GROUP, props.get(0));
        } else if (props.size() > 1) {
            throw new SecurityConfigurationException("Ambiguous identity parent group property in identity class "
                    + identityClass.getName());
        } else {
            Property<Object> prop = findNamedProperty(identityClass, "parentGroup", "parent");

            if (prop != null) {
                modelProperties.put(PROPERTY_PARENT_GROUP, prop);
            } else {
                throw new SecurityConfigurationException(
                        "Error initializing JPAIdentityStore - no parent group property found in identity class "
                                + identityClass.getName());
            }
        }
    }

    /**
     * This is an optional property, we don't throw an exception if it's not
     * present.
     * 
     * @throws SecurityConfigurationException
     */
    protected void configureIdentityEnabled() throws SecurityConfigurationException {
        List<Property<Object>> props = PropertyQueries.createQuery(identityClass)
                .addCriteria(new PropertyTypeCriteria(PropertyType.ENABLED)).getResultList();

        if (props.size() == 1) {
            modelProperties.put(PROPERTY_IDENTITY_ENABLED, props.get(0));
        } else if (props.size() > 1) {
            throw new SecurityConfigurationException("Ambiguous identity enabled property in identity class "
                    + identityClass.getName());
        } else {
            Property<Object> prop = findNamedProperty(identityClass, "enabled", "active");

            if (prop != null) {
                modelProperties.put(PROPERTY_IDENTITY_ENABLED, props.get(0));
            }
        }
    }

    /**
     * This is an optional property, we don't throw an exception if it's not
     * present.
     * 
     * @throws SecurityConfigurationException
     */
    protected void configureIdentityCreationDate() throws SecurityConfigurationException {
        List<Property<Object>> props = PropertyQueries.createQuery(identityClass)
                .addCriteria(new PropertyTypeCriteria(PropertyType.CREATION_DATE)).getResultList();

        if (props.size() == 1) {
            modelProperties.put(PROPERTY_IDENTITY_CREATED, props.get(0));
        } else if (props.size() > 1) {
            throw new SecurityConfigurationException("Ambiguous identity creation date property in identity class "
                    + identityClass.getName());
        } else {
            Property<Object> prop = findNamedProperty(identityClass, "created", "creationDate");

            if (prop != null) {
                modelProperties.put(PROPERTY_IDENTITY_CREATED, prop);
            }
        }
    }

    /**
     * This is an optional property, we don't throw an exception if it's not
     * present.
     * 
     * @throws SecurityConfigurationException
     */
    protected void configureIdentityExpiryDate() throws SecurityConfigurationException {
        List<Property<Object>> props = PropertyQueries.createQuery(identityClass)
                .addCriteria(new PropertyTypeCriteria(PropertyType.EXPIRY_DATE)).getResultList();

        if (props.size() == 1) {
            modelProperties.put(PROPERTY_IDENTITY_EXPIRES, props.get(0));
        } else if (props.size() > 1) {
            throw new SecurityConfigurationException("Ambiguous identity expiry date property in identity class "
                    + identityClass.getName());
        } else {
            Property<Object> prop = findNamedProperty(identityClass, "expires", "expiryDate");

            if (prop != null) {
                modelProperties.put(PROPERTY_IDENTITY_EXPIRES, prop);
            }
        }
    }

    /**
     * Scan for various optional user properties, such as first name, last name
     * and e-mail address.
     * 
     * @throws SecurityConfigurationException
     */
    protected void configureUserProperties() throws SecurityConfigurationException {
        // Determine the first name property
        List<Property<Object>> props = PropertyQueries.createQuery(identityClass)
                .addCriteria(new PropertyTypeCriteria(PropertyType.FIRST_NAME)).getResultList();

        if (props.size() == 1) {
            modelProperties.put(PROPERTY_USER_FIRST_NAME, props.get(0));
        } else if (props.size() > 1) {
            throw new SecurityConfigurationException("Ambiguous first name property in identity class "
                    + identityClass.getName());
        } else {
            Property<Object> prop = findNamedProperty(identityClass, "firstName");

            if (prop != null) {
                modelProperties.put(PROPERTY_USER_FIRST_NAME, prop);
            }
        }

        // Determine the last name property
        props = PropertyQueries.createQuery(identityClass)
                .addCriteria(new PropertyTypeCriteria(PropertyType.LAST_NAME)).getResultList();

        if (props.size() == 1) {
            modelProperties.put(PROPERTY_USER_LAST_NAME, props.get(0));
        } else if (props.size() > 1) {
            throw new SecurityConfigurationException("Ambiguous last name property in identity class "
                    + identityClass.getName());
        } else {
            Property<Object> prop = findNamedProperty(identityClass, "lastName");

            if (prop != null) {
                modelProperties.put(PROPERTY_USER_LAST_NAME, prop);
            }
        }

        // Determine the e-mail address property
        props = PropertyQueries.createQuery(identityClass).addCriteria(new PropertyTypeCriteria(PropertyType.EMAIL))
                .getResultList();

        if (props.size() == 1) {
            modelProperties.put(PROPERTY_USER_EMAIL, props.get(0));
        } else if (props.size() > 1) {
            throw new SecurityConfigurationException("Ambiguous e-mail property in identity class "
                    + identityClass.getName());
        } else {
            Property<Object> prop = findNamedProperty(identityClass, "email");

            if (prop != null) {
                modelProperties.put(PROPERTY_USER_EMAIL, prop);
            }
        }
    }

    /*
     * Configures properties for reading and writing identity memberships. As
     * this is an optional feature, the specified membershipClass property may
     * be left as null in which case no configuration will occur.
     */
    protected void configureMemberships() throws SecurityConfigurationException {
        if (membershipClass == null) {
            return;
        }

        // First determine the member property
        List<Property<Object>> props = PropertyQueries.createQuery(membershipClass)
                .addCriteria(new TypedPropertyCriteria(identityClass))
                .addCriteria(new PropertyTypeCriteria(PropertyType.MEMBER)).getResultList();

        if (props.size() == 1) {
            modelProperties.put(PROPERTY_MEMBERSHIP_MEMBER, props.get(0));
        } else if (props.size() > 1) {
            throw new SecurityConfigurationException("Ambiguous member property in membership class "
                    + membershipClass.getName());
        } else {
            Property<Object> p = findNamedProperty(membershipClass, "member");

            if (p != null) {
                modelProperties.put(PROPERTY_MEMBERSHIP_MEMBER, p);
            } else {
                throw new SecurityConfigurationException(
                        "Error initializing JPAIdentityStore - no member property found in membership class "
                                + membershipClass.getName());
            }
        }

        // Determine the group property
        props = PropertyQueries.createQuery(membershipClass).addCriteria(new TypedPropertyCriteria(identityClass))
                .addCriteria(new PropertyTypeCriteria(PropertyType.GROUP)).getResultList();

        if (props.size() == 1) {
            modelProperties.put(PROPERTY_MEMBERSHIP_GROUP, props.get(0));
        } else if (props.size() > 1) {
            throw new SecurityConfigurationException("Ambiguous group property in membership class "
                    + membershipClass.getName());
        } else {
            Property<Object> p = findNamedProperty(membershipClass, "group");

            if (p != null) {
                modelProperties.put(PROPERTY_MEMBERSHIP_GROUP, p);
            } else {
                throw new SecurityConfigurationException(
                        "Error initializing JPAIdentityStore - no group property found in membership class "
                                + membershipClass.getName());
            }
        }

        // Determine the role property
        props = PropertyQueries.createQuery(membershipClass).addCriteria(new TypedPropertyCriteria(identityClass))
                .addCriteria(new PropertyTypeCriteria(PropertyType.ROLE)).getResultList();

        if (props.size() == 1) {
            modelProperties.put(PROPERTY_MEMBERSHIP_ROLE, props.get(0));
        } else if (props.size() > 1) {
            throw new SecurityConfigurationException("Ambiguous role property in membership class "
                    + membershipClass.getName());
        } else {
            Property<Object> p = findNamedProperty(membershipClass, "role");

            if (p != null) {
                modelProperties.put(PROPERTY_MEMBERSHIP_ROLE, p);
            } else {
                throw new SecurityConfigurationException(
                        "Error initializing JPAIdentityStore - no role property found in membership class "
                                + membershipClass.getName());
            }
        }
    }

    /**
     * Configures the identity store for reading and writing attribute values
     * 
     * @throws SecurityConfigurationException
     */
    protected void configureAttributes() throws SecurityConfigurationException {
        // If an attribute class has been configured, scan it for attribute
        // properties
        if (attributeClass != null) {
            List<Property<Object>> props = PropertyQueries.createQuery(attributeClass)
                    .addCriteria(new PropertyTypeCriteria(PropertyType.NAME))
                    .addCriteria(new TypedPropertyCriteria(String.class)).getResultList();

            if (props.size() == 1) {
                modelProperties.put(PROPERTY_ATTRIBUTE_NAME, props.get(0));
            } else if (props.size() > 1) {
                throw new SecurityConfigurationException("Ambiguous attribute name property in attribute class "
                        + attributeClass.getName());
            } else {
                Property<Object> prop = findNamedProperty(attributeClass, "attributeName", "name");
                if (prop != null) {
                    modelProperties.put(PROPERTY_ATTRIBUTE_NAME, prop);
                } else {
                    throw new SecurityConfigurationException(
                            "Error initializing JPAIdentityStore - no name property found in attribute class "
                                    + attributeClass.getName());
                }
            }

            props = PropertyQueries.createQuery(attributeClass)
                    .addCriteria(new PropertyTypeCriteria(PropertyType.VALUE)).getResultList();

            if (props.size() == 1) {
                modelProperties.put(PROPERTY_ATTRIBUTE_VALUE, props.get(0));
            } else if (props.size() > 1) {
                throw new SecurityConfigurationException("Ambiguous attribute value property in class "
                        + attributeClass.getName());
            } else {
                Property<Object> prop = findNamedProperty(attributeClass, "attributeValue", "value");
                if (prop != null) {
                    modelProperties.put(PROPERTY_ATTRIBUTE_VALUE, prop);
                } else {
                    throw new SecurityConfigurationException(
                            "Error initializing JPAIdentityStore - no value property found in attribute class "
                                    + attributeClass.getName());
                }
            }

            props = PropertyQueries.createQuery(attributeClass).addCriteria(new TypedPropertyCriteria(identityClass))
                    .getResultList();

            if (props.size() == 1) {
                modelProperties.put(PROPERTY_ATTRIBUTE_IDENTITY, props.get(0));
            } else if (props.size() > 1) {
                throw new SecurityConfigurationException("Ambiguous identity property in attribute class "
                        + attributeClass.getName());
            } else {
                throw new SecurityConfigurationException("Error initializing JPAIdentityStore - "
                        + "no attribute identity property found.");
            }

            props = PropertyQueries.createQuery(attributeClass)
                    .addCriteria(new PropertyTypeCriteria(PropertyType.ATTRIBUTE_TYPE)).getResultList();

            if (props.size() == 1) {
                modelProperties.put(PROPERTY_ATTRIBUTE_TYPE, props.get(0));
            } else if (props.size() > 1) {
                throw new SecurityConfigurationException("Ambiguous attribute type property in class "
                        + attributeClass.getName());
            } else {
                Property<Object> prop = findNamedProperty(attributeClass, "attributeType", "type");
                if (prop != null) {
                    modelProperties.put(PROPERTY_ATTRIBUTE_TYPE, prop);
                } else {
                    throw new SecurityConfigurationException(
                            "Error initializing JPAIdentityStore - no attribute type property found in attribute class "
                                    + attributeClass.getName());
                }
            }
        }

        // Scan for attribute properties in the identity class
        List<Property<Object>> props = PropertyQueries.createQuery(identityClass)
                .addCriteria(new AnnotatedPropertyCriteria(IDMAttribute.class)).getResultList();

        for (Property<Object> p : props) {
            String attribName = p.getAnnotatedElement().getAnnotation(IDMAttribute.class).name();

            if (attributeProperties.containsKey(attribName)) {
                Property<Object> other = attributeProperties.get(attribName).getAttributeProperty();

                throw new SecurityConfigurationException("Multiple properties defined for attribute [" + attribName
                        + "] - " + "Property: " + other.getDeclaringClass().getName() + "."
                        + other.getAnnotatedElement().toString() + ", Property: " + p.getDeclaringClass().getName()
                        + "." + p.getAnnotatedElement().toString());
            }

            attributeProperties.put(attribName, new MappedAttribute(null, p));
        }

        // scan any entity classes referenced by the identity class also
        props = PropertyQueries.createQuery(identityClass).getResultList();

        for (Property<Object> p : props) {
            if (!p.isReadOnly() && p.getJavaClass().isAnnotationPresent(Entity.class)) {

                List<Property<Object>> pp = PropertyQueries.createQuery(p.getJavaClass())
                        .addCriteria(new AnnotatedPropertyCriteria(IDMAttribute.class)).getResultList();

                for (Property<Object> attributeProperty : pp) {
                    String attribName = attributeProperty.getAnnotatedElement().getAnnotation(IDMAttribute.class)
                            .name();

                    if (attributeProperties.containsKey(attribName)) {
                        Property<Object> other = attributeProperties.get(attribName).getAttributeProperty();

                        throw new SecurityConfigurationException("Multiple properties defined for attribute ["
                                + attribName + "] - " + "Property: " + other.getDeclaringClass().getName() + "."
                                + other.getAnnotatedElement().toString() + ", Property: "
                                + attributeProperty.getDeclaringClass().getName() + "."
                                + attributeProperty.getAnnotatedElement().toString());
                    }

                    attributeProperties.put(attribName, new MappedAttribute(p, attributeProperty));
                }
            }
        }
    }

    protected EntityManager getEntityManager(IdentityStoreInvocationContext invocationContext) {
        return ((JPAIdentityStoreSession) invocationContext.getIdentityStoreSession()).getEntityManager();
    }


    @Override
    public Set<Feature> getFeatureSet() {
        return featureSet;
    }    

    @Override
    public void createUser(IdentityStoreInvocationContext ctx, User user) {
        try {
            // Create the identity instance first
            Object identity = identityClass.newInstance();

            modelProperties.get(PROPERTY_IDENTITY_ID).setValue(identity, user.getId());

            modelProperties.get(PROPERTY_IDENTITY_DISCRIMINATOR).setValue(identity, identityTypeUser);

            if (modelProperties.containsKey(PROPERTY_USER_FIRST_NAME)) {
                modelProperties.get(PROPERTY_USER_FIRST_NAME).setValue(identity, user.getFirstName());
            }

            if (modelProperties.containsKey(PROPERTY_USER_LAST_NAME)) {
                modelProperties.get(PROPERTY_USER_LAST_NAME).setValue(identity, user.getLastName());
            }

            if (modelProperties.containsKey(PROPERTY_USER_EMAIL)) {
                modelProperties.get(PROPERTY_USER_EMAIL).setValue(identity, user.getEmail());
            }

            EntityManager em = getEntityManager(ctx);

            // Create any related entities that may be containers for attribute values
            for (String attribName : attributeProperties.keySet()) {
                MappedAttribute attrib = attributeProperties.get(attribName);
                if (attrib.getIdentityProperty() != null && attrib.getIdentityProperty().getValue(identity) == null) {
                    Object instance = attrib.getIdentityProperty().getJavaClass().newInstance();
                    attrib.getIdentityProperty().setValue(identity, instance);

                    em.persist(instance);
                }
            }

            em.persist(identity);

            // TODO fire an event here via the InvocationContext SPI, passing the identity entity

            if (user.getAttributes() != null && !user.getAttributes().isEmpty()) {
                for (String key : user.getAttributes().keySet()) {
                    setAttribute(ctx, user, key, user.getAttributeValues(key));
                }
            }
            
            em.flush();

        } catch (Exception ex) {
            throw new IdentityManagementException("Exception while creating user", ex);
        }
    }

    @Override
    public void removeUser(IdentityStoreInvocationContext ctx, User user) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean validateCredential(IdentityStoreInvocationContext ctx, User user, Credential credential) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void updateCredential(IdentityStoreInvocationContext ctx, User user, Credential credential) {
        // TODO Auto-generated method stub

    }

    @Override
    public User getUser(IdentityStoreInvocationContext ctx, String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Group createGroup(IdentityStoreInvocationContext ctx, String name, Group parent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeGroup(IdentityStoreInvocationContext ctx, Group group) {
        // TODO Auto-generated method stub

    }

    @Override
    public Group getGroup(IdentityStoreInvocationContext ctx, String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Role createRole(IdentityStoreInvocationContext ctx, String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeRole(IdentityStoreInvocationContext ctx, Role role) {
        // TODO Auto-generated method stub

    }

    @Override
    public Role getRole(IdentityStoreInvocationContext ctx, String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<User> executeQuery(IdentityStoreInvocationContext ctx, UserQuery query, Range range) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Group> executeQuery(IdentityStoreInvocationContext ctx, GroupQuery query, Range range) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Role> executeQuery(IdentityStoreInvocationContext ctx, RoleQuery query, Range range) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Membership> executeQuery(IdentityStoreInvocationContext ctx, MembershipQuery query, Range range) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAttribute(IdentityStoreInvocationContext ctx, IdentityType identity, String name, String[] values) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeAttribute(IdentityStoreInvocationContext ctx, IdentityType identity, String name) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String[] getAttributeValues(IdentityStoreInvocationContext ctx, IdentityType identity, String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String[]> getAttributes(IdentityStoreInvocationContext ctx, IdentityType identity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Membership createMembership(IdentityStoreInvocationContext ctx, IdentityType member, Group group, Role role) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeMembership(IdentityStoreInvocationContext ctx, IdentityType member, Group group, Role role) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Membership getMembership(IdentityStoreInvocationContext ctx, IdentityType member, Group group, Role role) {
        // TODO Auto-generated method stub
        return null;
    }

}
