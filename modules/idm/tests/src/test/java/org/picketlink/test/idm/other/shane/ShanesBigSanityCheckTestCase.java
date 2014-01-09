package org.picketlink.test.idm.other.shane;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.PermissionManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.permission.Permission;
import org.picketlink.idm.spi.ContextInitializer;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.test.idm.other.shane.model.scenario1.Role;
import org.picketlink.test.idm.other.shane.model.scenario1.User;
import org.picketlink.test.idm.other.shane.model.scenario1.entity.City;
import org.picketlink.test.idm.other.shane.model.scenario1.entity.Country;
import org.picketlink.test.idm.other.shane.model.scenario1.entity.EmployeeContract;
import org.picketlink.test.idm.other.shane.model.scenario1.entity.IdentityObjectAttribute;
import org.picketlink.test.idm.other.shane.model.scenario1.entity.PartitionAttribute;
import org.picketlink.test.idm.other.shane.model.scenario1.entity.PasswordHash;
import org.picketlink.test.idm.other.shane.model.scenario1.entity.RoleDetail;
import org.picketlink.test.idm.other.shane.model.scenario1.entity.State;
import org.picketlink.test.idm.other.shane.model.scenario1.entity.StreetType;
import org.picketlink.test.idm.other.shane.model.scenario1.entity.UserAddress;
import org.picketlink.test.idm.other.shane.model.scenario2.entity.Customer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Shane's broad-spectrum test suite - PLEASE DO NOT TOUCH!!
 *
 * @author Shane Bryzak
 */
public class ShanesBigSanityCheckTestCase {

    /**
     * This scenario exercises a variety of use cases against a relational database (JPA) identity store
     */
    @Test
    public void testScenario1() throws InterruptedException {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("shanes-test-suite-scenario1-pu");
        final EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder.named("default")
            .stores()
                .jpa()
                    .mappedEntity(org.picketlink.test.idm.other.shane.model.scenario1.entity.AccountLogin.class,
                    org.picketlink.test.idm.other.shane.model.scenario1.entity.IdentityObject.class,
                    org.picketlink.test.idm.other.shane.model.scenario1.entity.IdentityObjectAttribute.class,
                    org.picketlink.test.idm.other.shane.model.scenario1.entity.IdentityTextAttribute.class,
                    org.picketlink.test.idm.other.shane.model.scenario1.entity.Partition.class,
                    org.picketlink.test.idm.other.shane.model.scenario1.entity.PartitionAttribute.class,
                    org.picketlink.test.idm.other.shane.model.scenario1.entity.Relationship.class,
                    org.picketlink.test.idm.other.shane.model.scenario1.entity.RelationshipAttribute.class,
                    org.picketlink.test.idm.other.shane.model.scenario1.entity.RelationshipIdentity.class,
                    org.picketlink.test.idm.other.shane.model.scenario1.entity.RoleDetail.class,
                    org.picketlink.test.idm.other.shane.model.scenario1.entity.UserAddress.class,
                    org.picketlink.test.idm.other.shane.model.scenario1.entity.UserContact.class,
                    org.picketlink.test.idm.other.shane.model.scenario1.entity.UserDetail.class,
                    org.picketlink.test.idm.other.shane.model.scenario1.entity.UserEmail.class,
                    org.picketlink.test.idm.other.shane.model.scenario1.entity.EmployeeContract.class,
                    org.picketlink.test.idm.other.shane.model.scenario1.entity.PasswordHash.class)
                    .addContextInitializer(new ContextInitializer() {
                        @Override
                        public void initContextForStore(IdentityContext context, IdentityStore<?> store) {
                            context.setParameter(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER, em);
                        }
                    })
                    .addCredentialHandler(UserPasswordCredentialHandler.class)
                    .supportAllFeatures();

        PartitionManager partitionManager = new DefaultPartitionManager(builder.buildAll());

        // Create a realm with some attributes
        Realm r = new Realm(Realm.DEFAULT_REALM);
        r.setAttribute(new Attribute<String>("foo", "bar"));
        partitionManager.add(r, "default");

        // Assert that the attribute exclude was set
        r = partitionManager.<Realm>getPartition(Realm.class, Realm.DEFAULT_REALM);
        assert "bar".equals(r.getAttribute("foo").getValue());

        PartitionAttribute pa = em.createQuery(
                "select pa from PartitionAttribute pa where pa.partition.partitionId = :id and pa.attributeName = :attributeName",
                PartitionAttribute.class)
            .setParameter("id", r.getId())
            .setParameter("attributeName", "foo")
            .getSingleResult();

        // Assert that the attribute exclude is stored as a String
//        assert "bar".equals(pa.getAttributeValue()); attribute exclude is serialized

        // Update the attribute exclude
        r.setAttribute(new Attribute<String>("foo", "222"));
        partitionManager.update(r);

        // Lookup the realm again
        r = partitionManager.<Realm>getPartition(Realm.class, Realm.DEFAULT_REALM);
        // Assert that the attribute exclude was updated
        assert "222".equals(r.getAttribute("foo").getValue());

        // Delete the attribute
        r.removeAttribute("foo");
        partitionManager.update(r);

        r = partitionManager.<Realm>getPartition(Realm.class, Realm.DEFAULT_REALM);
        // Confirm the attribute exclude was deleted
        assert r.getAttribute("foo") == null;

        // Create a new user
        User u = new User();
        u.setFirstName("John");
        u.setLastName("Smith");
        u.setLoginName("jsmith");

        // Create an identity manager for the default partition
        IdentityManager identityManager = partitionManager.createIdentityManager();

        // Add the user
        identityManager.add(u);

        // Confirm that the identifier has been set
        assert u.getId() != null;

        // Lookup the user
        u = identityManager.lookupIdentityById(User.class, u.getId());

        // Confirm the properties have been set correctly
        assert "John".equals(u.getFirstName());
        assert "Smith".equals(u.getLastName());
        assert "jsmith".equals(u.getLoginName());

        // Set an attribute and update the user
        u.setAttribute(new Attribute<String>("SSN", "123-456-7890"));
        identityManager.update(u);

        // Lookup the user again
        u = identityManager.lookupIdentityById(User.class, u.getId());

        // Confirm the attribute was correctly set
        assert "123-456-7890".equals(u.<String>getAttribute("SSN").getValue());

//        // Lookup the attribute record in the database
//        IdentityTextAttribute attr = em.createQuery(
//                "select a from IdentityTextAttribute a where a.identity.id = :id and a.attributeName = :attributeName",
//                IdentityTextAttribute.class)
//            .setParameter("id", u.getId())
//            .setParameter("attributeName", "SSN")
//            .getSingleResult();
//
//        // Confirm the attribute string exclude is stored as text
//        assert "123-456-7890".equals(attr.getAttributeValue());

        // Create a random size byte array at least 512 bytes in size and populate it
        Random rnd = new Random(System.currentTimeMillis());
        final byte[] binaryData = new byte[512 + rnd.nextInt(50000)];
        rnd.nextBytes(binaryData);

        // Add another attribute containing binary data
        u.setAttribute(new Attribute<byte[]>("profilePhoto", binaryData));

        // Update the user
        identityManager.update(u);

        // Lookup the user again
        u = identityManager.lookupIdentityById(User.class, u.getId());

        // Confirm the binary exclude was correctly set
        assert Arrays.equals(binaryData,  u.<byte[]>getAttribute("profilePhoto").getValue());

        // Confirm that the binary exclude was persisted in the correct table
//        assert !em.createQuery(
//                "select a from IdentityObjectAttribute a where a.identity.id = :id and a.attributeName = :attributeName",
//                IdentityObjectAttribute.class)
//            .setParameter("id", u.getId())
//            .setParameter("attributeName", "profilePhoto")
//            .getResultList().isEmpty();

        // Create some default lookup data in the database
        Country country = new Country();
        country.setName("Australia");

        State state = new State();
        state.setCountry(country);
        state.setName("QLD");

        City city = new City();
        city.setState(state);
        city.setName("Brisbane");

        StreetType streetType = new StreetType();
        streetType.setDescription("Street");

        em.persist(country);
        em.persist(state);
        em.persist(city);
        em.persist(streetType);

        // Create a UserAddress instance and assign it to the userberkshire
        UserAddress addr = new UserAddress();
        addr.setUnitNumber("15B");
        addr.setStreetNumber("123");
        addr.setStreetName("Main");
        addr.setStreetType(streetType);
        addr.setCity(city);

        List<UserAddress> addresses = new ArrayList<UserAddress>();
        addresses.add(addr);

        u.setAddresses(addresses);

        // Update the user
        identityManager.update(u);

        // Lookup the user again
        u = identityManager.lookupIdentityById(User.class, u.getId());

        // Confirm the address was created, and details are correct
        assert u.getAddresses().size() == 1;
        addr = u.getAddresses().get(0);
        assert "15B".equals(addr.getUnitNumber());
        assert "123".equals(addr.getStreetNumber());

        // Create another address
        addr = new UserAddress();
        addr.setStreetNumber("16");
        addr.setStreetName("Elizabeth");
        addr.setStreetType(streetType);
        addr.setCity(city);

        u.getAddresses().add(addr);

        // Update the user
        identityManager.update(u);

        // Lookup the user again
        u = identityManager.lookupIdentityById(User.class, u.getId());

        // Confirm the second address was persisted
        assert u.getAddresses().size() == 2;

        // Delete the first address, and update the details of the second one
        Iterator<UserAddress> i = u.getAddresses().iterator();
        while (i.hasNext()) {
            addr = i.next();
            if ("Main".equals(addr.getStreetName())) {
                i.remove();
            } else {
                addr.setStreetNumber("18");
            }
        }

        // Update the user
        identityManager.update(u);

        // Lookup the user again
        u = identityManager.lookupIdentityById(User.class, u.getId());

        // Confirm there is only one address now
        assert u.getAddresses().size() == 1;

        // Confirm that the address details were updated
        assert "18".equals(u.getAddresses().get(0).getStreetNumber());

        // Confirm that no employee contract is currently set
        assert u.getEmployeeContract() == null;

        // Create an employee contract and assign it to the user
        EmployeeContract contract = new EmployeeContract();
        Date now = new Date();
        contract.setSignedDate(now);

        // Generate some more random binary data to represent the scanned contract document
        final byte[] documentData = new byte[512 + rnd.nextInt(50000)];
        rnd.nextBytes(documentData);
        contract.setDocument(documentData);

        // Assign the employee contract to the user and then update the user
        u.setEmployeeContract(contract);
        identityManager.update(u);

        // Lookup the user again
        u = identityManager.lookupIdentityById(User.class, u.getId());

        // Confirm that the employee contract was set
        assert u.getEmployeeContract() != null;

        // Confirm that the date was set correctly, and the document data is correct
        assert now.equals(u.getEmployeeContract().getSignedDate());
        assert Arrays.equals(documentData, u.getEmployeeContract().getDocument());

        // Confirm that the employee contract was persisted in the correct table
        assert !em.createQuery(
                "select c from EmployeeContract c where c.identity.id = :id",
                EmployeeContract.class)
            .setParameter("id", u.getId())
            .getResultList().isEmpty();

        // Now remove the contract and then update the user
        u.setEmployeeContract(null);
        identityManager.update(u);

        // Lookup the user again
        u = identityManager.lookupIdentityById(User.class, u.getId());

        // Confirm that the employee contract is null
        assert u.getEmployeeContract() == null;

        // Confirm that the record was deleted from the database
        assert em.createQuery(
                "select c from EmployeeContract c where c.identity.id = :id",
                EmployeeContract.class)
            .setParameter("id", u.getId())
            .getResultList().isEmpty();

        // Assign a password to the user
        Password pwd = new Password("password1234");
        identityManager.updateCredential(u, pwd);

        // Confirm that the user can authenticate using their password
        Credentials creds = new UsernamePasswordCredentials("jsmith", pwd);
        identityManager.validateCredentials(creds);
        assert Credentials.Status.VALID.equals(creds.getStatus());

        // Confirm that the password was persisted in the correct table
        assert !em.createQuery(
                "select h from PasswordHash h where h.identity.id = :id",
                PasswordHash.class)
            .setParameter("id", u.getId())
            .getResultList().isEmpty();

        // Assign a new password that doesn't come into effect until tomorrow
        Date tomorrow = new Date(System.currentTimeMillis() + (24 * 60 * 60 * 1000));
        Password newPwd = new Password("newpassword1234");
        identityManager.updateCredential(u, newPwd, tomorrow, null);

        // Confirm that the old password still validates
        creds = new UsernamePasswordCredentials("jsmith", pwd);
        identityManager.validateCredentials(creds);
        assert Credentials.Status.VALID.equals(creds.getStatus());

        // Confirm that the new password doesn't validate yet
        creds = new UsernamePasswordCredentials("jsmith", newPwd);
        identityManager.validateCredentials(creds);
        assert Credentials.Status.INVALID.equals(creds.getStatus());

        // Create a new role and add it
        Role role = new Role("employee");
        identityManager.add(role);

        // Lookup the role
        role = identityManager.lookupIdentityById(Role.class, role.getId());

        // Confirm the role name was correctly set
        assert "employee".equals(role.getName());

        // Confirm that a RoleDetail entity was created
        List<RoleDetail> id = em.createQuery(
                "select r from RoleDetail r where r.roleName = ?",
                RoleDetail.class)
                .setParameter(1, role.getName())
                .getResultList();

        assert !id.isEmpty();

        em.getTransaction().commit();
    }

    @Test
    public void testScenario2() throws InterruptedException {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("shanes-test-suite-scenario2-pu");
        final EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder.named("default")
            .stores()
                .jpa()
                    .mappedEntity(org.picketlink.test.idm.other.shane.model.scenario1.entity.AccountLogin.class,
                    org.picketlink.test.idm.other.shane.model.scenario2.entity.IdentityObject.class,
                    org.picketlink.test.idm.other.shane.model.scenario2.entity.IdentityObjectAttribute.class,
                    org.picketlink.test.idm.other.shane.model.scenario2.entity.Partition.class,
                    org.picketlink.test.idm.other.shane.model.scenario2.entity.PartitionAttribute.class,
                    org.picketlink.test.idm.other.shane.model.scenario2.entity.ResourcePermission.class,
                    org.picketlink.test.idm.other.shane.model.scenario2.entity.UserDetail.class)
                    .addContextInitializer(new ContextInitializer() {
                        @Override
                        public void initContextForStore(IdentityContext context, IdentityStore<?> store) {
                            context.setParameter(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER, em);
                        }
                    })
                    .addCredentialHandler(UserPasswordCredentialHandler.class)
                    .supportAllFeatures();

        PartitionManager partitionManager = new DefaultPartitionManager(builder.buildAll());

        // Create a realm with some attributes
        Realm r = new Realm(Realm.DEFAULT_REALM);
        r.setAttribute(new Attribute<String>("foo", "bar"));
        partitionManager.add(r, "default");

        // Create a new user
        org.picketlink.test.idm.other.shane.model.scenario2.User u = new org.picketlink.test.idm.other.shane.model.scenario2.User();
        u.setLoginName("shane");

        // Create an identity manager for the default partition
        IdentityManager identityManager = partitionManager.createIdentityManager();

        // Add the user
        identityManager.add(u);

        // Lookup the user
        u = identityManager.lookupIdentityById(org.picketlink.test.idm.other.shane.model.scenario2.User.class, u.getId());

        // Create some sample customer objects and persist them
        Customer c1 = new Customer();
        c1.setName("Acme");
        em.persist(c1);

        Customer c2 = new Customer();
        c2.setName("BuyNLarge");
        em.persist(c2);

        // Create a PermissionManager
        PermissionManager pm = partitionManager.createPermissionManager();

        // Grant the 'READ' permission for Customer c1 to the user we created
        pm.grantPermission(u, c1, Customer.PERMISSION_READ);

        // Confirm that the permission was created
        assert !pm.listPermissions(c1).isEmpty();

        // Clear all permissions for Customer c1
        pm.clearPermissions(c1);

        assert pm.listPermissions(c1).isEmpty();

        // Grant the 'UPDATE' permission for Customer c1 to the user we created
        pm.grantPermission(u, c1, Customer.PERMISSION_UPDATE);

        // Confirm that the permission can by looked up via the resource reference
        assert !pm.listPermissions(c1, Customer.PERMISSION_UPDATE).isEmpty();

        // Also assert there was only one permission created
        assert pm.listPermissions(c1).size() == 1;

        // Lookup the permission object
        Permission p = pm.listPermissions(c1).get(0);

        // Assert the permission properties are correctly set
        assert p.getResource().equals(c1);
        assert p.getAssignee().equals(u);
        assert Customer.PERMISSION_UPDATE.equals(p.getOperation());

        // Grant the 'DELETE' permission for Customer c1 to the user we created
        pm.grantPermission(u, c1, Customer.PERMISSION_DELETE);

        // Confirm that the permission exists
        assert !pm.listPermissions(c1, Customer.PERMISSION_DELETE).isEmpty();
    }
}
