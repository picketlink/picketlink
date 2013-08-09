package org.picketlink.test.idm.complex;

import org.junit.Before;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.jpa.model.sample.complex.Application;
import org.picketlink.idm.jpa.model.sample.complex.Company;
import org.picketlink.idm.jpa.model.sample.complex.EmployeeUser;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreComplexSchemaConfigurationTester;

import java.util.List;

import static org.junit.Assert.*;

/**
 *
 * @author pedroigor
 */
@Configuration(include = JPAStoreComplexSchemaConfigurationTester.class)
public class MultiPartitionTestCase extends AbstractPartitionManagerTestCase {

    private ComplexSchemaHelper wayneHelper;
    private ComplexSchemaHelper acmeHelper;
    private ComplexSchemaHelper umbrellaHelper;

    public MultiPartitionTestCase(final IdentityConfigurationTester visitor) {
        super(visitor);
    }

    @Override
    @Before
    public void onBefore() {
        super.onBefore();

        PartitionManager partitionManager = getPartitionManager();

        this.acmeHelper = new ComplexSchemaHelper("Acme", "acme.com", getVisitor(), partitionManager);
        this.umbrellaHelper = new ComplexSchemaHelper("Umbrella", "umbrella.com", getVisitor(), partitionManager);
        this.wayneHelper = new ComplexSchemaHelper("Wayne", "wayne.com", getVisitor(), partitionManager);
    }

    @Test
    public void testUserPartitioning() {
        EmployeeUser bugs = this.acmeHelper.createEmployeeUser("Bugs", "Bunny", "bugs",
                this.acmeHelper.getLawOrgUnit());
        EmployeeUser jill = this.umbrellaHelper.createEmployeeUser("Jill", "Hills", "jill",
                this.umbrellaHelper.getSecurityOrgUnit());
        EmployeeUser wayne = this.wayneHelper.createEmployeeUser("Bruce", "Wayne", "bruce",
                this.wayneHelper.getExecutiveOrgUnit());

        IdentityManager acmeIdentityManager = createIdentityManager(this.acmeHelper.getCompany());

        List<EmployeeUser> result = acmeIdentityManager.createIdentityQuery(EmployeeUser.class).getResultList();

        assertEquals(1, result.size());
        assertEquals(bugs.getId(), result.get(0).getId());

        IdentityManager umbrellaIdentityManager = createIdentityManager(this.umbrellaHelper.getCompany());

        result = umbrellaIdentityManager.createIdentityQuery(EmployeeUser.class).getResultList();

        assertEquals(1, result.size());
        assertEquals(jill.getId(), result.get(0).getId());

        IdentityManager wayneIdentityManager = createIdentityManager(this.wayneHelper.getCompany());

        result = wayneIdentityManager.createIdentityQuery(EmployeeUser.class).getResultList();

        assertEquals(1, result.size());
        assertEquals(wayne.getId(), result.get(0).getId());
    }

    @Test
    public void testApplicationAuthorization() {
        EmployeeUser bugs = this.acmeHelper.createEmployeeUser("Bugs", "Bunny", "bugs",
                this.acmeHelper.getLawOrgUnit());
        EmployeeUser jill = this.umbrellaHelper.createEmployeeUser("Jill", "Hills", "jill",
                this.umbrellaHelper.getSecurityOrgUnit());
        EmployeeUser wayne = this.wayneHelper.createEmployeeUser("Bruce", "Wayne", "bruce",
                this.wayneHelper.getExecutiveOrgUnit());

        Application acmeApplication = this.acmeHelper.createApplication("Application A");
        Application umbrellaApplication = this.umbrellaHelper.createApplication("Application A");
        Application wayneApplication = this.wayneHelper.createApplication("Application A");

        this.acmeHelper.authorizeApplication(bugs, acmeApplication);
        this.umbrellaHelper.authorizeApplication(jill, umbrellaApplication);
        this.wayneHelper.authorizeApplication(wayne, wayneApplication);

        assertTrue(this.acmeHelper.isAuthorized(bugs, acmeApplication));
        assertTrue(this.umbrellaHelper.isAuthorized(jill, umbrellaApplication));
        assertTrue(this.wayneHelper.isAuthorized(wayne, wayneApplication));

        assertFalse(this.acmeHelper.isAuthorized(bugs, umbrellaApplication));
        assertFalse(this.acmeHelper.isAuthorized(jill, acmeApplication));
    }

    @Test
    public void testApplicationAuthorizationBetweenDifferenceCompanies() {
        EmployeeUser bugs = this.acmeHelper.createEmployeeUser("Bugs", "Bunny", "bugs",
                this.acmeHelper.getLawOrgUnit());
        EmployeeUser wayne = this.wayneHelper.createEmployeeUser("Bruce", "Wayne", "bruce",
                this.wayneHelper.getExecutiveOrgUnit());

        Application acmeApplication = this.acmeHelper.createApplication("Application A");
        Application wayneApplication = this.wayneHelper.createApplication("Application A");

        this.acmeHelper.authorizeApplication(wayne, acmeApplication);
        this.wayneHelper.authorizeApplication(bugs, wayneApplication);

        assertTrue(this.acmeHelper.isAuthorized(bugs, wayneApplication));
        assertTrue(this.wayneHelper.isAuthorized(wayne, acmeApplication));
    }

    private IdentityManager createIdentityManager(Company company) {
        return getPartitionManager().createIdentityManager(company);
    }
}
