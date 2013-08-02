package org.picketlink.test.idm.complex;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.jpa.model.sample.complex.EmployeeUser;
import org.picketlink.idm.jpa.model.sample.complex.entity.Employee;
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
public class EmployeeUserTestCase extends AbstractIdentityTypeTestCase<EmployeeUser> {

    private ComplexSchemaHelper helper;

    public EmployeeUserTestCase(final IdentityConfigurationTester builder) {
        super(builder);
    }

    @Override
    public void onBefore() {
        super.onBefore();
        this.helper = new ComplexSchemaHelper("Acme", "acme.com", getVisitor());
    }

    @Override
    protected EmployeeUser createIdentityType() {
        return createEmployeeUser("Chuck", "Norris", "chuck");
    }

    @Override
    protected EmployeeUser getIdentityType() {
        IdentityQuery<EmployeeUser> query = this.helper.getIdentityManager().createIdentityQuery(EmployeeUser.class);

        query.setParameter(EmployeeUser.QUERY_ATTRIBUTE.byName("userName"), "chuck");

        List<EmployeeUser> result = query.getResultList();

        assertEquals(1, result.size());

        return result.get(0);
    }

    @Test
    public void testCreate() {
        EmployeeUser employeeUser = createIdentityType();

        EmployeeUser storedEmployeeUser = getIdentityType();

        assertNotNull(storedEmployeeUser);
        assertEquals(storedEmployeeUser.getUserName(), employeeUser.getUserName());
        assertEquals(storedEmployeeUser.getPerson().getId(), employeeUser.getPerson().getId());
        assertEquals(storedEmployeeUser.getPerson().getFirstName(), employeeUser.getPerson().getFirstName());
        assertEquals(storedEmployeeUser.getPerson().getLastName(), employeeUser.getPerson().getLastName());
        assertEquals(storedEmployeeUser.getPerson().getBirthDate(), employeeUser.getPerson().getBirthDate());

        assertEquals(1, employeeUser.getPerson().getAddress().size());
        assertEquals(storedEmployeeUser.getPerson().getAddress().get(0).getCity(), employeeUser.getPerson().getAddress().get(0).getCity());
        assertEquals(storedEmployeeUser.getPerson().getAddress().get(0).getCity(), employeeUser.getPerson().getAddress().get(0).getCity());
        assertEquals(storedEmployeeUser.getPerson().getAddress().get(0).getCountry(), employeeUser.getPerson().getAddress().get(0).getCountry());
        assertEquals(storedEmployeeUser.getPerson().getAddress().get(0).getStreetName(), employeeUser.getPerson().getAddress().get(0).getStreetName());
        assertEquals(storedEmployeeUser.getPerson().getAddress().get(0).getStreetNumber(), employeeUser.getPerson().getAddress().get(0).getStreetNumber());
        assertEquals(storedEmployeeUser.getPerson().getAddress().get(0).getType(), employeeUser.getPerson().getAddress().get(0).getType());
        assertEquals(storedEmployeeUser.getPerson().getAddress().get(0).getUnitNumber(), employeeUser.getPerson().getAddress().get(0).getUnitNumber());
        assertEquals(storedEmployeeUser.getPerson().getAddress().get(0).getZip(), employeeUser.getPerson().getAddress().get(0).getZip());

        assertEquals(1, employeeUser.getPerson().getEmails().size());
        assertEquals(storedEmployeeUser.getPerson().getEmails().get(0).getAddress(), employeeUser.getPerson().getEmails().get(0).getAddress());
        assertEquals(storedEmployeeUser.getPerson().getEmails().get(0).isPrimaryEmail(), employeeUser.getPerson().getEmails().get(0).isPrimaryEmail());

        assertEquals(1, employeeUser.getPerson().getPhones().size());
        assertEquals(storedEmployeeUser.getPerson().getPhones().get(0).getNumber(), employeeUser.getPerson().getPhones().get(0).getNumber());
        assertEquals(storedEmployeeUser.getPerson().getPhones().get(0).getType(), employeeUser.getPerson().getPhones().get(0).getType());

        assertEquals(Employee.class, storedEmployeeUser.getPerson().getClass());
        assertNotNull(((Employee) storedEmployeeUser.getPerson()).getOrganizationUnit());
        assertEquals(((Employee) storedEmployeeUser.getPerson()).getOrganizationUnit(), ((Employee) employeeUser.getPerson()).getOrganizationUnit());
        assertEquals(((Employee) storedEmployeeUser.getPerson()).getOrganizationUnit().getParent(), this.helper.getTechnologyOrgUnit());
    }

    @Test
    public void testUpdate() {
        EmployeeUser employeeUser = createIdentityType();

        employeeUser.getPerson().setFirstName("Changed FirstName");

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(employeeUser);

        EmployeeUser storedEmployeeUser = getIdentityType();

        assertEquals(employeeUser.getPerson().getFirstName(), storedEmployeeUser.getPerson().getFirstName());
        assertEquals(employeeUser.getPerson().getLastName(), storedEmployeeUser.getPerson().getLastName());
        assertEquals(employeeUser.getPerson().getBirthDate(), storedEmployeeUser.getPerson().getBirthDate());

        employeeUser.setEmployeeId("RH000000000");

        identityManager.update(employeeUser);

        storedEmployeeUser = getIdentityType();

        assertEquals(employeeUser.getEmployeeId(), storedEmployeeUser.getEmployeeId());

        this.helper.addAddress(employeeUser.getPerson(), "Another City");

        identityManager.update(employeeUser);

        storedEmployeeUser = getIdentityType();

        assertEquals(2, employeeUser.getPerson().getAddress().size());

        employeeUser.setExpirationDate(new Date());

        identityManager.update(employeeUser);

        storedEmployeeUser = getIdentityType();

        assertNotNull(storedEmployeeUser.getExpirationDate());

        employeeUser.setLoginCount(10);
        employeeUser.setFailedLoginCount(3);

        identityManager.update(employeeUser);

        storedEmployeeUser = getIdentityType();

        assertEquals(employeeUser.getFailedLoginCount(), storedEmployeeUser.getFailedLoginCount());
        assertEquals(employeeUser.getLoginCount(), storedEmployeeUser.getLoginCount());
    }

    @Test
    public void testFindByPerson() {
        EmployeeUser chuck = createIdentityType();

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<EmployeeUser> query = identityManager.createIdentityQuery(EmployeeUser.class);

        query.setParameter(EmployeeUser.QUERY_ATTRIBUTE.byName("person"), chuck.getPerson());

        List<EmployeeUser> result = query.getResultList();

        assertEquals(1, result.size());
        assertEquals(chuck.getId(), result.get(0).getId());

        EmployeeUser mary = createEmployeeUser("Mary", "Anne", "mary");

        query = identityManager.createIdentityQuery(EmployeeUser.class);

        query.setParameter(EmployeeUser.QUERY_ATTRIBUTE.byName("person"), mary.getPerson());

        result = query.getResultList();

        assertEquals(1, result.size());
        assertEquals(mary.getId(), result.get(0).getId());
    }

    @Override
    public IdentityManager getIdentityManager() {
        return this.helper.getIdentityManager();
    }

    public EmployeeUser createEmployeeUser(String firstName, String lastName, String userName) {
        return this.helper.createEmployeeUser(firstName, lastName, userName,
                this.helper.getSecurityOrgUnit());
    }
}
