package org.picketlink.test.idm.complex;

import org.junit.Ignore;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.jpa.model.sample.complex.Application;
import org.picketlink.idm.jpa.model.sample.complex.OrganizationUnit;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.basic.AbstractIdentityTypeTestCase;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreComplexSchemaConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPStoreConfigurationTester;

import java.util.List;

import static org.junit.Assert.*;

/**
 */
@Configuration(include = JPAStoreComplexSchemaConfigurationTester.class)
public class ApplicationTestCase extends AbstractIdentityTypeTestCase<Application> {

    private ComplexSchemaHelper helper;

    public ApplicationTestCase(final IdentityConfigurationTester builder) {
        super(builder);
    }

    @Override
    public void onBefore() {
        super.onBefore();
        this.helper = new ComplexSchemaHelper("Acme", "acme.com", getVisitor());
    }

    @Override
    protected Application createIdentityType() {
        Application orgUnit = new Application("Employee Manager");

        getIdentityManager().add(orgUnit);

        return orgUnit;
    }

    @Test
    public void testCreate() {
        Application orgUnit = createIdentityType();
        Application storedOrgUnit = getIdentityType();

        assertEquals(orgUnit.getName(), storedOrgUnit.getName());
        assertEquals(orgUnit.getId(), storedOrgUnit.getId());
    }

    @Test
    public void testUpdate() {
        Application orgUnit = createIdentityType();

        Application parent = new Application("Customer Relationship");

        IdentityManager identityManager = getIdentityManager();

        identityManager.add(parent);

        identityManager.update(orgUnit);
    }

    @Override
    @Test
    @Ignore("In this case we do not need enable/disable this type.")
    public void testDisable() throws Exception {
    }

    @Override
    @Test
    @Ignore("In this case we do not need expiry this type.")
    public void testExpiration() throws Exception {
    }

    @Override
    protected Application getIdentityType() {
        IdentityQuery<Application> query = this.helper.getIdentityManager().createIdentityQuery(Application.class);

        query.setParameter(OrganizationUnit.QUERY_ATTRIBUTE.byName("name"), "Employee Manager");

        List<Application> result = query.getResultList();

        assertEquals(1, result.size());

        return result.get(0);
    }

    @Override
    public IdentityManager getIdentityManager() {
        return this.helper.getIdentityManager();
    }
}
