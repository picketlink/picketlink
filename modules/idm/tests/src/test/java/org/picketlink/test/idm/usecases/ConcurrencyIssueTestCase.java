package org.picketlink.test.idm.usecases;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA. User: pedroigor Date: 8/6/13 Time: 7:32 PM To change this template use File | Settings |
 * File Templates.
 */
public class ConcurrencyIssueTestCase {

    @Test
    public void testPreserveState() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("file-store-preserve-state")
                .stores()
                    .file()
                        .workingDirectory("/tmp/teste")
                        .supportAllFeatures();

        PartitionManager partitionManager = new DefaultPartitionManager(builder.buildAll());

        Realm realmA = new Realm(Realm.DEFAULT_REALM);

        partitionManager.add(realmA);

        final User userA = new User("admin");

        final IdentityManager identityManager = partitionManager.createIdentityManager();

        identityManager.add(userA);

        Password password = new Password("admin");

        identityManager.updateCredential(userA, password);

        ExecutorService executor = Executors.newFixedThreadPool(100);
        List<FutureTask<Boolean>> tasks = new ArrayList<FutureTask<Boolean>>();

        for (int i = 0; i < 1000; i++) {
            FutureTask<Boolean> task = new FutureTask<Boolean>(createTask(userA, partitionManager));

            tasks.add(task);

            executor.execute(task);
        }

        for (FutureTask<Boolean> task : tasks) {
            assertTrue(task.get(1, TimeUnit.SECONDS));
        }
    }

    private Callable<Boolean> createTask(final User userA, final PartitionManager partitionManager) {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    IdentityManager identityManager = partitionManager.createIdentityManager();
                    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userA.getLoginName(), new Password("admin"));

                    assertNotNull(identityManager.lookupById(userA.getClass(), userA.getId()));

                    identityManager.validateCredentials(credentials);

                    assertEquals(UsernamePasswordCredentials.Status.VALID, credentials.getStatus());
                } catch (Exception e) {
                    return false;
                }

                return true;
            }
        };
    }

}
