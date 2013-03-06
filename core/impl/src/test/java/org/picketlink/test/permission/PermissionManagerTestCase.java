package org.picketlink.test.permission;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picketlink.permission.PermissionManager;
import org.picketlink.test.permission.schema.Permissions;

/**
 * 
 * @author Shane Bryzak
 *
 */
@RunWith(Arquillian.class)
public class PermissionManagerTestCase {
    @Inject PermissionManager permissionManager;
    
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(PermissionManagerTestCase.class)
                .addClass(Permissions.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsManifestResource(EmptyAsset.INSTANCE, "persistence.xml");
    }

    @Test
    public void testPermissionManager() {
        assert permissionManager != null;
    }
}
