package org.picketlink.test.idm.complex;

import org.junit.Before;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.jpa.model.sample.complex.Company;
import org.picketlink.idm.jpa.model.sample.complex.EmployeeUser;
import org.picketlink.idm.jpa.model.sample.complex.entity.Email;
import org.picketlink.idm.jpa.model.sample.complex.entity.Employee;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreComplexSchemaConfigurationTester;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 * @author  Pedro Igor
 */
@Configuration(include = JPAStoreComplexSchemaConfigurationTester.class)
public class MappingTestCase extends AbstractPartitionManagerTestCase {

    private JPAStoreComplexSchemaConfigurationTester configurationTester;
    private Company company;

    public MappingTestCase(final IdentityConfigurationTester visitor) {
        super(visitor);
        this.configurationTester = (JPAStoreComplexSchemaConfigurationTester) visitor;
    }

    @Before
    public void onSetup() {
        this.company = new Company();

        company.setName("Acme");
        company.setDomain("domain.com");

        getPartitionManager().add(company);
    }

    @Test
    public void testSingleAttributeValue() {
        Employee employee = new Employee();

        employee.setFirstName("John");
        employee.setLastName("Paul");
        employee.setInternalId("33221221");

        getEntityManager().persist(employee);

        EmployeeUser user = new EmployeeUser("john");

        user.setPerson(employee);

        getIdentityManager().add(user);

        List<EmployeeUser> result = getIdentityManager().createIdentityQuery(EmployeeUser.class).getResultList();

        assertEquals(1, result.size());

        EmployeeUser storedUser = result.get(0);

        assertNotNull(storedUser.getPerson());
        assertEquals(user.getPerson().getClass(), storedUser.getPerson().getClass());
        assertEquals(user.getPerson(), storedUser.getPerson());
    }

    @Test
    public void testSingleNamedMappedAttribute() {
        Employee employee = new Employee();

        employee.setFirstName("John");
        employee.setLastName("Paul");
        employee.setInternalId("33221221");

        getEntityManager().persist(employee);

        EmployeeUser user = new EmployeeUser("john");

        user.setPerson(employee);

        Email email = new Email();

        email.setAddress("user@domain.com");
        email.setPrimaryEmail(true);

        user.setEmail(email);

        getIdentityManager().add(user);

        this.getEntityManager().getTransaction().commit();

        List<EmployeeUser> result = getIdentityManager().createIdentityQuery(EmployeeUser.class).getResultList();

        assertEquals(1, result.size());

        EmployeeUser storedUser = result.get(0);

        assertNotNull(storedUser.getEmail());
        assertEquals(user.getEmail(), storedUser.getEmail());
    }

    @Test
    public void testListNamedMappedAttribute() throws InterruptedException {
        Employee employee = new Employee();

        employee.setFirstName("John");
        employee.setLastName("Paul");
        employee.setInternalId("33221221");

        getEntityManager().persist(employee);

        EmployeeUser user = new EmployeeUser("john");

        user.setPerson(employee);

        Email email = new Email();

        email.setAddress("user@domain.com");
        email.setPrimaryEmail(true);

        user.setEmail(email);

        getIdentityManager().add(user);

        List<EmployeeUser> result = getIdentityManager().createIdentityQuery(EmployeeUser.class).getResultList();

        assertEquals(1, result.size());

        EmployeeUser storedUser = result.get(0);

        assertNotNull(storedUser.getEmail());
        assertEquals(user.getEmail(), storedUser.getEmail());

        storedUser.getEmail().setAddress("changed@domain.com");

        getIdentityManager().update(user);

        result = getIdentityManager().createIdentityQuery(EmployeeUser.class).getResultList();

        assertEquals(1, result.size());

        storedUser = result.get(0);

        assertEquals("changed@domain.com", storedUser.getEmail().getAddress());
    }

    private EntityManager getEntityManager() {
        return this.configurationTester.getEntityManager();
    }

    @Override
    public IdentityManager getIdentityManager() {
        return getPartitionManager().createIdentityManager(this.company);
    }
}
