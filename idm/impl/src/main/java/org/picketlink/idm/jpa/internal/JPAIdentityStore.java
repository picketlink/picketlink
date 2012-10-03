package org.picketlink.idm.jpa.internal;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Id;

import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.internal.util.properties.Property;
import org.picketlink.idm.internal.util.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.idm.internal.util.properties.query.PropertyQueries;
import org.picketlink.idm.internal.util.reflection.Reflections;
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

    private static final String DEFAULT_USER_IDENTITY_TYPE = "USER";
    private static final String DEFAULT_ROLE_IDENTITY_TYPE = "ROLE";
    private static final String DEFAULT_GROUP_IDENTITY_TYPE = "GROUP";
    
    private static final String DEFAULT_RELATIONSHIP_TYPE_MEMBERSHIP = "MEMBERSHIP";
    private static final String DEFAULT_RELATIONSHIP_TYPE_ROLE = "ROLE";    
    
    // Property keys

    private static final String PROPERTY_IDENTITY_ID = "IDENTITY_ID";
    private static final String PROPERTY_IDENTITY_NAME = "IDENTITY_NAME";
    private static final String PROPERTY_IDENTITY_TYPE = "IDENTITY_TYPE";
    private static final String PROPERTY_IDENTITY_TYPE_NAME = "IDENTITY_TYPE_NAME";
    private static final String PROPERTY_CREDENTIAL_VALUE = "CREDENTIAL_VALUE";
    private static final String PROPERTY_CREDENTIAL_TYPE = "CREDENTIAL_TYPE";
    private static final String PROPERTY_CREDENTIAL_TYPE_NAME = "CREDENTIAL_TYPE_NAME";
    private static final String PROPERTY_CREDENTIAL_IDENTITY = "CREDENTIAL_IDENTITY";
    private static final String PROPERTY_RELATIONSHIP_FROM = "RELATIONSHIP_FROM";
    private static final String PROPERTY_RELATIONSHIP_TO = "RELATIONSHIP_TO";
    private static final String PROPERTY_RELATIONSHIP_TYPE = "RELATIONSHIP_TYPE";
    private static final String PROPERTY_RELATIONSHIP_TYPE_NAME = "RELATIONSHIP_TYPE_NAME";
    private static final String PROPERTY_RELATIONSHIP_NAME = "RELATIONSHIP_NAME";

    private static final String PROPERTY_ROLE_TYPE_NAME = "RELATIONSHIP_NAME_NAME";

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
    private Class<?> credentialClass;
    private Class<?> relationshipClass;
    private Class<?> attributeClass;
    private Class<?> roleTypeClass;
    
    /**
     * Model properties
     */
    private Map<String, Property<Object>> modelProperties = new HashMap<String, Property<Object>>();
    

    private String userIdentityType = DEFAULT_USER_IDENTITY_TYPE;
    private String roleIdentityType = DEFAULT_ROLE_IDENTITY_TYPE;
    private String groupIdentityType = DEFAULT_GROUP_IDENTITY_TYPE;

    private String relationshipTypeMembership = DEFAULT_RELATIONSHIP_TYPE_MEMBERSHIP;
    private String relationshipTypeRole = DEFAULT_RELATIONSHIP_TYPE_ROLE;    
    
    public void bootstrap(JPAIdentityStoreConfiguration config)
            throws SecurityConfigurationException {
        
        identityClass = config.getIdentityClass();

        if (identityClass == null) {
            throw new SecurityConfigurationException("Error initializing JpaIdentityStore - identityClass not set");
        }

        credentialClass = config.getCredentialClass();
        relationshipClass = config.getRelationshipClass();
        roleTypeClass = config.getRoleTypeClass();
        attributeClass = config.getAttributeClass();

        configureIdentityId();
        //configureIdentityName();
        //configureIdentityType();

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
    
    protected void configureIdentityId() throws SecurityConfigurationException {
        List<Property<Object>> props = PropertyQueries.createQuery(identityClass)
                .addCriteria(new AnnotatedPropertyCriteria(Id.class))
                .getResultList();

        if (props.size() == 1) {
            modelProperties.put(PROPERTY_IDENTITY_ID, props.get(0));
        } else {
            throw new SecurityConfigurationException("Error initializing JPAIdentityStore - no Identity ID found.");
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
