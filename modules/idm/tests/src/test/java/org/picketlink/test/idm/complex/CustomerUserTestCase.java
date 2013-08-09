package org.picketlink.test.idm.complex;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.jpa.model.sample.complex.CustomerUser;
import org.picketlink.idm.jpa.model.sample.complex.EmployeeUser;
import org.picketlink.idm.jpa.model.sample.complex.entity.Customer;
import org.picketlink.idm.jpa.model.sample.complex.entity.Email;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.basic.AbstractIdentityTypeTestCase;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreComplexSchemaConfigurationTester;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 */
@Configuration(include = JPAStoreComplexSchemaConfigurationTester.class)
public class CustomerUserTestCase extends AbstractIdentityTypeTestCase<CustomerUser> {

    private ComplexSchemaHelper helper;

    public CustomerUserTestCase(final IdentityConfigurationTester builder) {
        super(builder);
    }

    @Override
    public void onBefore() {
        super.onBefore();
        this.helper = new ComplexSchemaHelper("Acme", "acme.com", getVisitor());
    }

    @Override
    protected CustomerUser createIdentityType() {
        return createCustomerUser("Mario", "Trevis", "mario");
    }

    @Override
    protected CustomerUser getIdentityType() {
        IdentityQuery<CustomerUser> query = this.helper.getIdentityManager().createIdentityQuery(CustomerUser.class);

        query.setParameter(EmployeeUser.QUERY_ATTRIBUTE.byName("userName"), "mario");

        List<CustomerUser> result = query.getResultList();

        assertEquals(1, result.size());

        return result.get(0);
    }

    @Test
    public void testCreate() {
        CustomerUser customer = createIdentityType();

        CustomerUser storedEmployeeUser = getIdentityType();

        assertNotNull(storedEmployeeUser);
        assertEquals(storedEmployeeUser.getUserName(), customer.getUserName());
        assertEquals(storedEmployeeUser.getPerson().getId(), customer.getPerson().getId());
        assertEquals(storedEmployeeUser.getPerson().getFirstName(), customer.getPerson().getFirstName());
        assertEquals(storedEmployeeUser.getPerson().getLastName(), customer.getPerson().getLastName());
        assertEquals(storedEmployeeUser.getPerson().getBirthDate(), customer.getPerson().getBirthDate());

        assertEquals(1, customer.getPerson().getAddress().size());
        assertEquals(storedEmployeeUser.getPerson().getAddress().get(0).getCity(), customer.getPerson().getAddress().get(0).getCity());
        assertEquals(storedEmployeeUser.getPerson().getAddress().get(0).getCity(), customer.getPerson().getAddress().get(0).getCity());
        assertEquals(storedEmployeeUser.getPerson().getAddress().get(0).getCountry(), customer.getPerson().getAddress().get(0).getCountry());
        assertEquals(storedEmployeeUser.getPerson().getAddress().get(0).getStreetName(), customer.getPerson().getAddress().get(0).getStreetName());
        assertEquals(storedEmployeeUser.getPerson().getAddress().get(0).getStreetNumber(), customer.getPerson().getAddress().get(0).getStreetNumber());
        assertEquals(storedEmployeeUser.getPerson().getAddress().get(0).getType(), customer.getPerson().getAddress().get(0).getType());
        assertEquals(storedEmployeeUser.getPerson().getAddress().get(0).getUnitNumber(), customer.getPerson().getAddress().get(0).getUnitNumber());
        assertEquals(storedEmployeeUser.getPerson().getAddress().get(0).getZip(), customer.getPerson().getAddress().get(0).getZip());

        assertEquals(1, customer.getPerson().getEmails().size());
        assertEquals(storedEmployeeUser.getPerson().getEmails().get(0).getAddress(), customer.getPerson().getEmails().get(0).getAddress());
        assertEquals(storedEmployeeUser.getPerson().getEmails().get(0).isPrimaryEmail(), customer.getPerson().getEmails().get(0).isPrimaryEmail());

        assertEquals(1, customer.getPerson().getPhones().size());
        assertEquals(storedEmployeeUser.getPerson().getPhones().get(0).getNumber(), customer.getPerson().getPhones().get(0).getNumber());
        assertEquals(storedEmployeeUser.getPerson().getPhones().get(0).getType(), customer.getPerson().getPhones().get(0).getType());

        assertEquals(Customer.class, storedEmployeeUser.getPerson().getClass());
    }

    @Test
    public void testSingleNamedMappedAttribute() {
        CustomerUser customer = createIdentityType();

        Email email = new Email();

        email.setAddress("user@domain.com");
        email.setPrimaryEmail(true);

        customer.setEmail(email);

        getIdentityManager().update(customer);

        CustomerUser storedEmployeeUser = getIdentityType();

        assertNotNull(storedEmployeeUser.getEmail());
        assertEquals(email.getAddress(), storedEmployeeUser.getEmail().getAddress());
        assertEquals(email.isPrimaryEmail(), storedEmployeeUser.getEmail().isPrimaryEmail());

        email.setPrimaryEmail(false);
        email.setAddress("changed@domain.com");

        getIdentityManager().update(customer);

        assertNotNull(storedEmployeeUser.getEmail());
        assertEquals(email.getAddress(), storedEmployeeUser.getEmail().getAddress());
        assertEquals(email.isPrimaryEmail(), storedEmployeeUser.getEmail().isPrimaryEmail());
    }

    @Test
    public void testUpdate() {
        CustomerUser customer = createIdentityType();

        customer.getPerson().setFirstName("Changed FirstName");

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(customer);

        CustomerUser storedEmployeeUser = getIdentityType();

        assertEquals(customer.getPerson().getFirstName(), storedEmployeeUser.getPerson().getFirstName());
        assertEquals(customer.getPerson().getLastName(), storedEmployeeUser.getPerson().getLastName());
        assertEquals(customer.getPerson().getBirthDate(), storedEmployeeUser.getPerson().getBirthDate());

        identityManager.update(customer);

        storedEmployeeUser = getIdentityType();

        this.helper.addAddress(customer.getPerson(), "Another City");

        identityManager.update(customer);

        storedEmployeeUser = getIdentityType();

        assertEquals(2, customer.getPerson().getAddress().size());

        customer.setExpirationDate(new Date());

        identityManager.update(customer);

        storedEmployeeUser = getIdentityType();

        assertNotNull(storedEmployeeUser.getExpirationDate());

        customer.setLoginCount(10);
        customer.setFailedLoginCount(3);

        identityManager.update(customer);

        storedEmployeeUser = getIdentityType();

        assertEquals(customer.getFailedLoginCount(), storedEmployeeUser.getFailedLoginCount());
        assertEquals(customer.getLoginCount(), storedEmployeeUser.getLoginCount());
    }

    @Test
    public void testFindByPerson() {
        CustomerUser customer = createIdentityType();

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<CustomerUser> query = identityManager.createIdentityQuery(CustomerUser.class);

        query.setParameter(EmployeeUser.QUERY_ATTRIBUTE.byName("person"), customer.getPerson());

        List<CustomerUser> result = query.getResultList();

        assertEquals(1, result.size());
        assertEquals(customer.getId(), result.get(0).getId());
    }

    @Override
    public IdentityManager getIdentityManager() {
        return this.helper.getIdentityManager();
    }

    public CustomerUser createCustomerUser(String firstName, String lastName, String userName) {
        return this.helper.createCustomerUser(firstName, lastName, userName);
    }
}
