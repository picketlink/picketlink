package org.picketlink.test.idm.config;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.picketlink.PartitionManagerCreateEvent;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.test.AbstractArquillianTestCase;
import org.picketlink.test.util.ArchiveUtils;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Pedro Igor
 */
public class InitializePartitionManagerTestCase extends AbstractArquillianTestCase {

    private static final String PARTITION_NAME = "somePartition";

    @Inject
    private PartitionManager partitionManager;

    @Deployment
    public static WebArchive deploy() {
        return ArchiveUtils.create(InitializePartitionManagerTestCase.class, AbstractArquillianTestCase.class);
    }

    @Test
    public void testDefaultPartitionNotCreated() {
        assertNull(this.partitionManager.getPartition(Realm.class, Realm.DEFAULT_REALM));
        assertNotNull(this.partitionManager.getPartition(Realm.class, PARTITION_NAME));
    }

    public static class PartitionManagerInitializer {

        public void init(@Observes PartitionManagerCreateEvent event) {
            PartitionManager partitionManager = event.getPartitionManager();

            partitionManager.add(new Realm(PARTITION_NAME));
        }

    }
}
