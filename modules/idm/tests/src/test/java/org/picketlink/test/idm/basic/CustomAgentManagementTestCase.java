package org.picketlink.test.idm.basic;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.basic.Agent;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.FileStoreConfigurationTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreConfigurationTester;

import java.util.List;

import static junit.framework.Assert.*;

/**
 * @author  pedroigor
 */
@Configuration(include = {JPAStoreConfigurationTester.class, FileStoreConfigurationTester.class})
public class CustomAgentManagementTestCase extends AbstractIdentityTypeTestCase<CustomAgentManagementTestCase.CustomAgent> {

    public CustomAgentManagementTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    @Test
    public void testFindById() throws Exception {
        CustomAgent identityType = createIdentityType();

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<IdentityType> query = identityManager.createIdentityQuery(IdentityType.class);

        query.setParameter(AttributedType.ID, identityType.getId());

        List<IdentityType> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(identityType.getId(), result.get(0).getId());
    }

    @Override
    protected CustomAgent createIdentityType() {
        CustomAgent agent = new CustomAgent("john");

        List<CustomAgent> result = getIdentityManager().createIdentityQuery(CustomAgent.class)
                .setParameter(CustomAgent.LOGIN_NAME, agent.getLoginName())
                .getResultList();

        if (!result.isEmpty()) {
            getIdentityManager().remove(result.get(0));
        }

        getIdentityManager().add(agent);

        return agent;
    }

    @Override
    protected CustomAgent getIdentityType() {
        List<CustomAgent> result = getIdentityManager().createIdentityQuery(CustomAgent.class)
                .setParameter(CustomAgent.LOGIN_NAME, "john")
                .getResultList();

        if (!result.isEmpty()) {
            return result.get(0);
        }

        return null;
    }

    public static class CustomAgent extends Agent {

        public CustomAgent() {
        }

        public CustomAgent(final String john) {
            super(john);
        }
    }

}