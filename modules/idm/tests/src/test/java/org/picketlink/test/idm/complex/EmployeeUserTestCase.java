package org.picketlink.test.idm.complex;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.jpa.model.sample.complex.User;
import org.picketlink.idm.jpa.model.sample.complex.entity.Employee;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.test.idm.IgnoreTester;
import org.picketlink.test.idm.basic.AbstractIdentityTypeTestCase;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPStoreConfigurationTester;

import java.util.List;

import static org.junit.Assert.*;

/**
 */
@IgnoreTester({LDAPStoreConfigurationTester.class, JPAStoreConfigurationTester.class})
public class EmployeeUserTestCase extends AbstractIdentityTypeTestCase<User> {

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
    protected User createIdentityType() {
        User user = this.helper.createEmployeeUser("Chuck", "Norris", "chuck", this.helper.getSecurityOrgUnit());

        getIdentityManager().add(user);

        return user;
    }

    @Test
    public void testCreate() {
        User user = createIdentityType();

        User storedUser = getIdentityType();

        assertNotNull(storedUser);
        assertEquals(storedUser.getUserName(), user.getUserName());
        assertEquals(storedUser.getPerson().getId(), user.getPerson().getId());
        assertEquals(storedUser.getPerson().getFirstName(), user.getPerson().getFirstName());
        assertEquals(storedUser.getPerson().getLastName(), user.getPerson().getLastName());
        assertEquals(storedUser.getPerson().getBirthDate(), user.getPerson().getBirthDate());

        assertEquals(1, user.getPerson().getAddress().size());
        assertEquals(storedUser.getPerson().getAddress().get(0).getCity(), user.getPerson().getAddress().get(0).getCity());
        assertEquals(storedUser.getPerson().getAddress().get(0).getCity(), user.getPerson().getAddress().get(0).getCity());
        assertEquals(storedUser.getPerson().getAddress().get(0).getCountry(), user.getPerson().getAddress().get(0).getCountry());
        assertEquals(storedUser.getPerson().getAddress().get(0).getStreetName(), user.getPerson().getAddress().get(0).getStreetName());
        assertEquals(storedUser.getPerson().getAddress().get(0).getStreetNumber(), user.getPerson().getAddress().get(0).getStreetNumber());
        assertEquals(storedUser.getPerson().getAddress().get(0).getType(), user.getPerson().getAddress().get(0).getType());
        assertEquals(storedUser.getPerson().getAddress().get(0).getUnitNumber(), user.getPerson().getAddress().get(0).getUnitNumber());
        assertEquals(storedUser.getPerson().getAddress().get(0).getZip(), user.getPerson().getAddress().get(0).getZip());

        assertEquals(1, user.getPerson().getEmails().size());
        assertEquals(storedUser.getPerson().getEmails().get(0).getAddress(), user.getPerson().getEmails().get(0).getAddress());
        assertEquals(storedUser.getPerson().getEmails().get(0).isPrimaryEmail(), user.getPerson().getEmails().get(0).isPrimaryEmail());

        assertEquals(1, user.getPerson().getPhones().size());
        assertEquals(storedUser.getPerson().getPhones().get(0).getNumber(), user.getPerson().getPhones().get(0).getNumber());
        assertEquals(storedUser.getPerson().getPhones().get(0).getType(), user.getPerson().getPhones().get(0).getType());

        assertEquals(Employee.class, storedUser.getPerson().getClass());
        assertNotNull(((Employee) storedUser.getPerson()).getOrganizationUnit());
        assertEquals(((Employee) storedUser.getPerson()).getOrganizationUnit(), ((Employee) user.getPerson()).getOrganizationUnit());
        assertEquals(((Employee) storedUser.getPerson()).getOrganizationUnit().getParent(), this.helper.getTechnologyOrgUnit());
    }

    @Override
    protected User getIdentityType() {
        IdentityQuery<User> query = this.helper.getIdentityManager().createIdentityQuery(User.class);

        query.setParameter(User.QUERY_ATTRIBUTE.byName("userName"), "chuck");

        List<User> result = query.getResultList();

        assertEquals(1, result.size());

        return result.get(0);
    }

    @Override
    public IdentityManager getIdentityManager() {
        return this.helper.getIdentityManager();
    }
}
