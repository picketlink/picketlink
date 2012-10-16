package org.picketlink.idm.jpa.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.internal.util.properties.Property;
import org.picketlink.idm.internal.util.properties.query.NamedPropertyCriteria;
import org.picketlink.idm.internal.util.properties.query.PropertyCriteria;
import org.picketlink.idm.internal.util.properties.query.PropertyQueries;
import org.picketlink.idm.internal.util.properties.query.TypedPropertyCriteria;
import org.picketlink.idm.jpa.annotations.IDMProperty;
import org.picketlink.idm.jpa.annotations.PropertyType;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Membership;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.GroupQuery;
import org.picketlink.idm.query.MembershipQuery;
import org.picketlink.idm.query.Range;
import org.picketlink.idm.query.RoleQuery;
import org.picketlink.idm.query.UserQuery;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.JPAIdentityStoreConfiguration;

/**
 * Implementation of IdentityStore that stores its state in a relational database.
 *
 * @author Shane Bryzak
 *
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
    
    // Properties common to Users and Groups
    private static final String PROPERTY_IDENTITY_ID = "IDENTITY_ID";

    // Properties common to Groups and Roles
    private static final String PROPERTY_IDENTITY_NAME = "IDENTITY_NAME";

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

    // Entity classes
    private Class<?> identityClass;
    private Class<?> membershipClass;
    
    private Class<?> credentialClass;    
    private Class<?> attributeClass;

    /**
     * Model properties
     */
    private Map<String, Property<Object>> modelProperties = new HashMap<String, Property<Object>>();

    private String userIdentityType = DEFAULT_USER_IDENTITY_DISCRIMINATOR;
    private String roleIdentityType = DEFAULT_ROLE_IDENTITY_DISCRIMINATOR;
    private String groupIdentityType = DEFAULT_GROUP_IDENTITY_DISCRIMINATOR;

    private class PropertyTypeCriteria implements PropertyCriteria {
        private PropertyType pt;

        public PropertyTypeCriteria(PropertyType pt) {
            this.pt = pt;
        }

        public boolean fieldMatches(Field f) {
            return f.isAnnotationPresent(IDMProperty.class) &&
                    f.getAnnotation(IDMProperty.class).value().equals(pt);
        }

        public boolean methodMatches(Method m) {
            return m.isAnnotationPresent(IDMProperty.class) &&
                    m.getAnnotation(IDMProperty.class).value().equals(pt);
        }
    }
    
    protected Property<Object> findNamedProperty(Class<?> targetClass, String... allowedNames) {
        List<Property<Object>> props = PropertyQueries.createQuery(targetClass)
                    .addCriteria(new TypedPropertyCriteria(String.class))
                    .addCriteria(new NamedPropertyCriteria(allowedNames))
                    .getResultList();

        for (String name : allowedNames) {
            for (Property<Object> prop : props) {
                if (name.equals(prop.getName())) return prop;
            }
        }

        return null;
    }    

    public void bootstrap(JPAIdentityStoreConfiguration config)
            throws SecurityConfigurationException {

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
        configureIdentityEnabled();
        configureIdentityCreationDate();
        configureIdentityExpiryDate();
        
        configureMemberships();
        
        //configureCredentials();
        
        
        //configureRelationships();
        //configureAttributes();

        //if (namedRelationshipsSupported) {
            //configureRoleTypeName();
        //}

        //featuresMetaData = new FeaturesMetaDataImpl(
          //      configurationContext.getStoreConfigurationMetaData(),
          //      new HashSet<IdentityObjectSearchCriteriaType>(),
          //      false,
          //      namedRelationshipsSupported,
          //      new HashSet<String>()
        //);
    }
    
    
    protected void configureIdentityDiscriminator() throws SecurityConfigurationException {
        List<Property<Object>> props = PropertyQueries.createQuery(identityClass)
                .addCriteria(new PropertyTypeCriteria(PropertyType.DISCRIMINATOR))
                .getResultList();
        
        if (props.size() == 1) {
            modelProperties.put(PROPERTY_IDENTITY_DISCRIMINATOR, props.get(0));
        } else if (props.size() > 1) {
            throw new SecurityConfigurationException("Ambiguous identity discriminator property in identity class " + 
                    identityClass.getName());
        } else {
            Property<Object> p = findNamedProperty(identityClass, 
                    "discriminator", "identityType", "identityTypeName", "typeName", "type");
            
            if (p != null) {
                modelProperties.put(PROPERTY_IDENTITY_DISCRIMINATOR, p);
            }
        }
    }    

    protected void configureIdentityKey() throws SecurityConfigurationException {
        List<Property<Object>> props = PropertyQueries.createQuery(identityClass)
                .addCriteria(new PropertyTypeCriteria(PropertyType.KEY))
                .getResultList();

        if (props.size() == 1) {
            modelProperties.put(PROPERTY_IDENTITY_KEY, props.get(0));
        } else if (props.size() > 1) {
            throw new SecurityConfigurationException("Ambiguous identity key property in identity class " + 
                    identityClass.getName());
        } else {
            props = PropertyQueries.createQuery(identityClass)
                    .addCriteria(new NamedPropertyCriteria("key"))
                    .getResultList();

            if (!props.isEmpty()) {
                modelProperties.put(PROPERTY_IDENTITY_KEY, props.get(0));
            } else {
                throw new SecurityConfigurationException(
                    "Error initializing JPAIdentityStore - no key property found in identity class " +
                    identityClass.getName());
            }
        }
    }
    
    protected void configureIdentityId() throws SecurityConfigurationException {
        List<Property<Object>> props = PropertyQueries.createQuery(identityClass)
                .addCriteria(new PropertyTypeCriteria(PropertyType.ID))
                .getResultList();
        
        if (props.size() == 1) {
            modelProperties.put(PROPERTY_IDENTITY_ID,  props.get(0));
        } else if (props.size() > 1) {
            throw new SecurityConfigurationException("Ambiguous identity id property in identity class " + 
                    identityClass.getName());
        } else {
            throw new SecurityConfigurationException(
                    "Error initializing JPAIdentityStore - no id property found in identity class " +
                    identityClass.getName());
        }        
    }
    
    protected void configureIdentityName() throws SecurityConfigurationException {
        List<Property<Object>> props = PropertyQueries.createQuery(identityClass)
                .addCriteria(new PropertyTypeCriteria(PropertyType.NAME))
                .getResultList();
        
        if (props.size() == 1) {
            modelProperties.put(PROPERTY_IDENTITY_NAME,  props.get(0));
        } else if (props.size() > 1) {
            throw new SecurityConfigurationException("Ambiguous identity name property in identity class " + 
                    identityClass.getName());
        } else {
            props = PropertyQueries.createQuery(identityClass)
                    .addCriteria(new NamedPropertyCriteria("name"))
                    .getResultList();

            if (!props.isEmpty()) {
                modelProperties.put(PROPERTY_IDENTITY_NAME, props.get(0));
            } else {
                throw new SecurityConfigurationException(
                        "Error initializing JPAIdentityStore - no name property found in identity class " +
                    identityClass.getName());
            }            
        }        
    }

    /**
     * This is an optional property, we don't throw an exception if it's not present.
     * 
     * @throws SecurityConfigurationException
     */
    protected void configureIdentityEnabled() throws SecurityConfigurationException {
        List<Property<Object>> props = PropertyQueries.createQuery(identityClass)
                .addCriteria(new PropertyTypeCriteria(PropertyType.ENABLED))
                .getResultList();
        
        if (props.size() == 1) {
            modelProperties.put(PROPERTY_IDENTITY_ENABLED,  props.get(0));
        } else if (props.size() > 1) {
            throw new SecurityConfigurationException("Ambiguous identity enabled property in identity class " + 
                    identityClass.getName());
        } else {
            Property<Object> prop = findNamedProperty(identityClass, "enabled", "active");

            if (prop != null) {                                
                modelProperties.put(PROPERTY_IDENTITY_ENABLED, props.get(0));
            }            
        }                
    }
    
    /**
     * This is an optional property, we don't throw an exception if it's not present.
     * 
     * @throws SecurityConfigurationException
     */    
    protected void configureIdentityCreationDate() throws SecurityConfigurationException {
        List<Property<Object>> props = PropertyQueries.createQuery(identityClass)
                .addCriteria(new PropertyTypeCriteria(PropertyType.CREATION_DATE))
                .getResultList();
        
        if (props.size() == 1) {
            modelProperties.put(PROPERTY_IDENTITY_CREATED,  props.get(0));
        } else if (props.size() > 1) {
            throw new SecurityConfigurationException("Ambiguous identity creation date property in identity class " + 
                    identityClass.getName());
        } else {
            Property<Object> prop = findNamedProperty(identityClass,  "created", "creationDate");

            if (prop != null) {
                modelProperties.put(PROPERTY_IDENTITY_CREATED, prop);
            }            
        }                
    }    
    
    /**
     * This is an optional property, we don't throw an exception if it's not present.
     * 
     * @throws SecurityConfigurationException
     */    
    protected void configureIdentityExpiryDate() throws SecurityConfigurationException {
        List<Property<Object>> props = PropertyQueries.createQuery(identityClass)
                .addCriteria(new PropertyTypeCriteria(PropertyType.EXPIRY_DATE))
                .getResultList();
        
        if (props.size() == 1) {
            modelProperties.put(PROPERTY_IDENTITY_EXPIRES,  props.get(0));
        } else if (props.size() > 1) {
            throw new SecurityConfigurationException("Ambiguous identity expiry date property in identity class " + 
                    identityClass.getName());
        } else {
            Property<Object> prop = findNamedProperty(identityClass,  "expires", "expiryDate");

            if (prop != null) {
                modelProperties.put(PROPERTY_IDENTITY_EXPIRES, prop);
            }            
        }                
    }

    /*
     * Configures properties for reading and writing identity memberships. As this is an optional feature, 
     * the specified membershipClass property may be left as null in which case no configuration will occur.
     * 
     */
    protected void configureMemberships() throws SecurityConfigurationException {
        if (membershipClass == null) {
            return;
        }
        
        // First determine the member property
        List<Property<Object>> props = PropertyQueries.createQuery(membershipClass)
                .addCriteria(new TypedPropertyCriteria(identityClass))
                .addCriteria(new PropertyTypeCriteria(PropertyType.MEMBER))
                .getResultList();
        
        if (props.size() == 1) {
            modelProperties.put(PROPERTY_MEMBERSHIP_MEMBER, props.get(0));
        } else if (props.size() > 1) {
            throw new SecurityConfigurationException("Ambiguous member property in membership class " + 
                membershipClass.getName());
        } else {
            Property<Object> p = findNamedProperty(membershipClass, "member");
            
            if (p != null) {
                modelProperties.put(PROPERTY_MEMBERSHIP_MEMBER, p);
            } else {
                throw new SecurityConfigurationException(
                    "Error initializing JPAIdentityStore - no member property found in membership class " + 
                    membershipClass.getName());
            }            
        }
        
        // Determine the group property
        props = PropertyQueries.createQuery(membershipClass)
                .addCriteria(new TypedPropertyCriteria(identityClass))
                .addCriteria(new PropertyTypeCriteria(PropertyType.GROUP))
                .getResultList();
        
        if (props.size() == 1) {
            modelProperties.put(PROPERTY_MEMBERSHIP_GROUP, props.get(0));
        } else if (props.size() > 1) {
            throw new SecurityConfigurationException("Ambiguous group property in membership class " + 
                membershipClass.getName());
        } else {
            Property<Object> p = findNamedProperty(membershipClass, "group");
            
            if (p != null) {
                modelProperties.put(PROPERTY_MEMBERSHIP_GROUP, p);
            } else {
                throw new SecurityConfigurationException(
                    "Error initializing JPAIdentityStore - no group property found in membership class " + 
                    membershipClass.getName());
            }            
        }        
        
        // Determine the role property
        props = PropertyQueries.createQuery(membershipClass)
                .addCriteria(new TypedPropertyCriteria(identityClass))
                .addCriteria(new PropertyTypeCriteria(PropertyType.ROLE))
                .getResultList();
        
        if (props.size() == 1) {
            modelProperties.put(PROPERTY_MEMBERSHIP_ROLE, props.get(0));
        } else if (props.size() > 1) {
            throw new SecurityConfigurationException("Ambiguous role property in membership class " + 
                membershipClass.getName());
        } else {
            Property<Object> p = findNamedProperty(membershipClass, "role");
            
            if (p != null) {
                modelProperties.put(PROPERTY_MEMBERSHIP_ROLE, p);
            } else {
                throw new SecurityConfigurationException(
                    "Error initializing JPAIdentityStore - no role property found in membership class " + 
                    membershipClass.getName());
            }            
        }        
    }

    @Override
    public boolean validatePassword(User user, String password) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void updatePassword(User user, String password) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean validateCertificate(User user, X509Certificate certificate) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean updateCertificate(User user, X509Certificate certificate) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public User createUser(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public User createUser(User user) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeUser(User user) {
        // TODO Auto-generated method stub

    }

    @Override
    public User getUser(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Group createGroup(String name, Group parent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeGroup(Group group) {
        // TODO Auto-generated method stub

    }

    @Override
    public Group getGroup(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Role createRole(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeRole(Role role) {
        // TODO Auto-generated method stub

    }

    @Override
    public Role getRole(String role) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Membership createMembership(Role role, User user, Group group) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeMembership(Role role, User user, Group group) {
        // TODO Auto-generated method stub

    }

    @Override
    public Membership getMembership(Role role, User user, Group group) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<User> executeQuery(UserQuery query, Range range) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Group> executeQuery(GroupQuery query, Range range) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Role> executeQuery(RoleQuery query, Range range) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Membership> executeQuery(MembershipQuery query, Range range) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAttribute(User user, String name, String[] values) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeAttribute(User user, String name) {
        // TODO Auto-generated method stub

    }

    @Override
    public String[] getAttributeValues(User user, String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String[]> getAttributes(User user) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAttribute(Group group, String name, String[] values) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeAttribute(Group group, String name) {
        // TODO Auto-generated method stub

    }

    @Override
    public String[] getAttributeValues(Group group, String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String[]> getAttributes(Group group) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAttribute(Role role, String name, String[] values) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeAttribute(Role role, String name) {
        // TODO Auto-generated method stub

    }

    @Override
    public String[] getAttributeValues(Role role, String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String[]> getAttributes(Role role) {
        // TODO Auto-generated method stub
        return null;
    }

}
