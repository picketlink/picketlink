/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.test.idm.complex;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.jpa.model.sample.complex.Company;
import org.picketlink.idm.jpa.model.sample.complex.User;
import org.picketlink.idm.jpa.model.sample.complex.entity.Address;
import org.picketlink.idm.jpa.model.sample.complex.entity.Country;
import org.picketlink.idm.jpa.model.sample.complex.entity.Email;
import org.picketlink.idm.jpa.model.sample.complex.entity.Employee;
import org.picketlink.idm.jpa.model.sample.complex.entity.OrganizationUnit;
import org.picketlink.idm.jpa.model.sample.complex.entity.Phone;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreComplexSchemaConfigurationTester;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * @author pedroigor
 */
public class ComplexSchemaHelper {

    private IdentityManager identityManager;
    private PartitionManager partitionManager;
    private Country country;
    private EntityManager entityManager;

    private Company company;

    private OrganizationUnit technologyOrgUnit;
    private OrganizationUnit financeOrgUnit;
    private OrganizationUnit lawOrgUnit;
    private OrganizationUnit humanResourceOrgUnit;
    private OrganizationUnit executiveOrgUnit;
    private OrganizationUnit securityOrgUnit;

    public ComplexSchemaHelper(String company, String domain, IdentityConfigurationTester visitor) {
        this.partitionManager = visitor.getPartitionManager();
        this.country = new Country("Brazil");

        if (JPAStoreComplexSchemaConfigurationTester.class.isInstance(visitor)) {
            JPAStoreComplexSchemaConfigurationTester jpaVisitor = (JPAStoreComplexSchemaConfigurationTester) visitor;

            this.entityManager = jpaVisitor.getEntityManager();
            this.entityManager.persist(this.country);
        }

        if (entityManager == null) {
            this.country.setId(1l);
        }

        this.company = createCompany(company, domain);
        this.identityManager = this.partitionManager.createIdentityManager(this.company);
    }

    @Test
    public void testUser() {
        User john = createEmployeeUser("John", "Smith", "john", this.executiveOrgUnit);
        User mary = createEmployeeUser("Mary", "Anne", "mary", this.financeOrgUnit);
        User ayrton = createEmployeeUser("Ayrton", "Senna", "ayrton", this.lawOrgUnit);
        User francisco = createEmployeeUser("Francisco", "Miller", "francisco", this.humanResourceOrgUnit);
        User steve = createEmployeeUser("Steve", "Taylor", "steve", this.technologyOrgUnit);
        User chuck = createEmployeeUser("Chuck", "Norris", "chuck", this.securityOrgUnit);

        identityManager.add(john);
        identityManager.add(mary);
        identityManager.add(ayrton);
        identityManager.add(francisco);
        identityManager.add(steve);
        identityManager.add(chuck);

        IdentityQuery<User> query = identityManager.createIdentityQuery(User.class);

        query.setParameter(User.QUERY_ATTRIBUTE.byName("userName"), chuck.getUserName());

        List<User> result = query.getResultList();

        assertEquals(1, result.size());

        User user = result.get(0);

        assertNotNull(chuck);
        assertEquals(chuck.getUserName(), user.getUserName());
        assertEquals(chuck.getPerson().getId(), user.getPerson().getId());
        assertEquals(chuck.getPerson().getFirstName(), user.getPerson().getFirstName());
        assertEquals(chuck.getPerson().getLastName(), user.getPerson().getLastName());
        assertEquals(chuck.getPerson().getBirthDate(), user.getPerson().getBirthDate());

        assertEquals(1, chuck.getPerson().getAddress().size());
        assertEquals(user.getPerson().getAddress().get(0).getCity(), chuck.getPerson().getAddress().get(0).getCity());
        assertEquals(user.getPerson().getAddress().get(0).getCity(), chuck.getPerson().getAddress().get(0).getCity());
        assertEquals(user.getPerson().getAddress().get(0).getCountry(), chuck.getPerson().getAddress().get(0).getCountry());
        assertEquals(user.getPerson().getAddress().get(0).getStreetName(), chuck.getPerson().getAddress().get(0).getStreetName());
        assertEquals(user.getPerson().getAddress().get(0).getStreetNumber(), chuck.getPerson().getAddress().get(0).getStreetNumber());
        assertEquals(user.getPerson().getAddress().get(0).getType(), chuck.getPerson().getAddress().get(0).getType());
        assertEquals(user.getPerson().getAddress().get(0).getUnitNumber(), chuck.getPerson().getAddress().get(0).getUnitNumber());
        assertEquals(user.getPerson().getAddress().get(0).getZip(), chuck.getPerson().getAddress().get(0).getZip());

        assertEquals(1, chuck.getPerson().getEmails().size());
        assertEquals(user.getPerson().getEmails().get(0).getAddress(), chuck.getPerson().getEmails().get(0).getAddress());
        assertEquals(user.getPerson().getEmails().get(0).isPrimaryEmail(), chuck.getPerson().getEmails().get(0).isPrimaryEmail());

        assertEquals(1, chuck.getPerson().getPhones().size());
        assertEquals(user.getPerson().getPhones().get(0).getNumber(), chuck.getPerson().getPhones().get(0).getNumber());
        assertEquals(user.getPerson().getPhones().get(0).getType(), chuck.getPerson().getPhones().get(0).getType());

        assertEquals(Employee.class, user.getPerson().getClass());
        assertNotNull(((Employee) user.getPerson()).getOrganizationUnit());
        assertEquals(((Employee) user.getPerson()).getOrganizationUnit(), ((Employee) chuck.getPerson()).getOrganizationUnit());
        assertEquals(((Employee) user.getPerson()).getOrganizationUnit().getParent(), this.technologyOrgUnit);
    }

    public User createEmployeeUser(final String firstName, final String lastName, final String userName,
                                   final OrganizationUnit executiveOrgUnit1) {
        Employee person = new Employee(executiveOrgUnit1);

        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setBirthDate(new Date());

        Address address = new Address();

        address.setCity("Barbacena");
        address.setCountry(this.country);
        address.setStreetName("D. Pedro I");
        address.setType("Home");
        address.setUnitNumber("1501");
        address.setZip("70000000");
        address.setPrimaryAddress(true);

        person.addAddress(address);

        Email email = new Email(userName + "@" + this.company.getDomain());

        person.addEmail(email);

        Phone phone = new Phone("555-555-555");

        person.addPhone(phone);

        if (entityManager != null) {
            this.entityManager.persist(person);
        } else {
            person.setId(new Random().nextLong());
        }

        User user = new User(userName);

        user.setPerson(person);

        return user;
    }

    public Company createCompany(String name, final String domain) {
        Company company = new Company(name);

        company.setDomain(domain);

        this.partitionManager.add(company);

        org.picketlink.idm.jpa.model.sample.complex.entity.Company companyEntity = null;

        if (this.entityManager != null) {
            companyEntity = this.entityManager.find(org
                    .picketlink.idm.jpa.model.sample.complex.entity.Company.class, company.getId());
        } else {
            companyEntity = new org.picketlink.idm.jpa.model.sample.complex.entity.Company(name);
            companyEntity.setDomain(domain);
        }

        this.executiveOrgUnit = createOrganizationUnit("Executive Board", null, companyEntity);
        this.financeOrgUnit = createOrganizationUnit("Finance", this.executiveOrgUnit, companyEntity);
        this.lawOrgUnit = createOrganizationUnit("Law Department", this.executiveOrgUnit, companyEntity);
        this.humanResourceOrgUnit = createOrganizationUnit("Human Resources & Administration", this.executiveOrgUnit, companyEntity);
        this.technologyOrgUnit = createOrganizationUnit("Engineering, Operations & Technology", this.executiveOrgUnit, companyEntity);
        this.securityOrgUnit = createOrganizationUnit("Security Division", this.technologyOrgUnit, companyEntity);

        return company;
    }

    private OrganizationUnit createOrganizationUnit(final String name, final OrganizationUnit parent,
                                                    final org.picketlink.idm.jpa.model.sample.complex.entity.Company
                                                            company) {
        OrganizationUnit organizationUnit = new OrganizationUnit(name);

        organizationUnit.setParent(parent);
        organizationUnit.setCompany(company);

        if (entityManager != null) {
            this.entityManager.persist(organizationUnit);
        } else {
            organizationUnit.setId(new Random().nextLong());
        }

        return organizationUnit;
    }

    public OrganizationUnit getSecurityOrgUnit() {
        return securityOrgUnit;
    }

    public OrganizationUnit getTechnologyOrgUnit() {
        return technologyOrgUnit;
    }

    public IdentityManager getIdentityManager() {
        return identityManager;
    }
}
