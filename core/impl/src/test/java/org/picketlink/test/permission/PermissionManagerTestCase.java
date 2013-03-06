package org.picketlink.test.permission;

import java.io.File;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
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

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class)
                .addClass(PermissionManagerTestCase.class)
                .addClass(Permissions.class)
                .addAsManifestResource(new FileAsset(new File("src/test/resources/META-INF/beans.xml")), "beans.xml")
                .addAsManifestResource(new FileAsset(new File("src/test/resources/META-INF/persistence.xml")), "persistence.xml")
                .addAsLibraries(
                        DependencyResolvers.use(MavenDependencyResolver.class)
                        .artifact("org.picketlink:picketlink-core-api:3.0.0-SNAPSHOT")
                        .artifact("org.picketlink:picketlink-core-impl:3.0.0-SNAPSHOT")
                        .resolveAsFiles()
                        );

        System.out.println(war.toString(true));

        return war;
    }

    @Test
    public void testPermissionManager() {
        assert permissionManager != null;
    }
}
