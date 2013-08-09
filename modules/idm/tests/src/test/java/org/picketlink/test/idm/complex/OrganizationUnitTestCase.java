package org.picketlink.test.idm.complex;

import org.junit.Ignore;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.jpa.model.sample.complex.OrganizationUnit;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.basic.AbstractIdentityTypeTestCase;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreComplexSchemaConfigurationTester;

import java.util.List;

import static org.junit.Assert.*;

/**
 */
@Configuration(include = JPAStoreComplexSchemaConfigurationTester.class)
public class OrganizationUnitTestCase extends AbstractIdentityTypeTestCase<OrganizationUnit> {

    private ComplexSchemaHelper helper;

    public OrganizationUnitTestCase(final IdentityConfigurationTester builder) {
        super(builder);
    }

    @Override
    public void onBefore() {
        super.onBefore();
        this.helper = new ComplexSchemaHelper("Acme", "acme.com", getVisitor());
    }

    @Override
    protected OrganizationUnit createIdentityType() {
        OrganizationUnit orgUnit = new OrganizationUnit("Business Affairs");

        getIdentityManager().add(orgUnit);

        return orgUnit;
    }

    @Test
    public void testCreate() {
        OrganizationUnit orgUnit = createIdentityType();
        OrganizationUnit storedOrgUnit = getIdentityType();

        assertEquals(orgUnit.getName(), storedOrgUnit.getName());
        assertEquals(orgUnit.getId(), storedOrgUnit.getId());
    }

    @Test
    public void testUpdate() {
        OrganizationUnit orgUnit = createIdentityType();

        OrganizationUnit parent = new OrganizationUnit("Customer Relationship");

        IdentityManager identityManager = getIdentityManager();

        identityManager.add(parent);

        orgUnit.setParent(parent);

        identityManager.update(orgUnit);

        OrganizationUnit storedOrgUnit = getIdentityType();

        assertNotNull(storedOrgUnit.getParent());
        assertEquals(orgUnit.getParent().getId(), storedOrgUnit.getParent().getId());
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
    protected OrganizationUnit getIdentityType() {
        IdentityQuery<OrganizationUnit> query = this.helper.getIdentityManager().createIdentityQuery(OrganizationUnit.class);

        query.setParameter(OrganizationUnit.QUERY_ATTRIBUTE.byName("name"), "Business Affairs");

        List<OrganizationUnit> result = query.getResultList();

        assertEquals(1, result.size());

        return result.get(0);
    }

    @Override
    public IdentityManager getIdentityManager() {
        return this.helper.getIdentityManager();
    }
}
