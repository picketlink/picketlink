package org.picketlink.test.permission;

import java.io.File;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.permission.PermissionManager;
import org.picketlink.test.permission.action.FooAction;
import org.picketlink.test.permission.resource.Resources;
import org.picketlink.test.permission.schema.Foo;
import org.picketlink.test.permission.schema.Permissions;

/**
 * 
 * @author Shane Bryzak
 *
 */
@RunWith(Arquillian.class)
@Ignore
public class PermissionManagerTestCase {
    @Inject PermissionManager permissionManager;
    
    @Inject IdentityManager identityManager;

    @Inject FooAction fooAction;

    @Deployment
    public static WebArchive createDeployment() {

        WebArchive war = ShrinkWrap.create(WebArchive.class)
                .addClass(PermissionManagerTestCase.class)
                .addClass(Permissions.class)
                .addClass(Foo.class)
                .addClass(FooAction.class)
                .addClass(Resources.class)
                .addAsWebInfResource(new FileAsset(new File("src/test/resources/META-INF/beans.xml")), "beans.xml")
                .addAsWebInfResource(new FileAsset(new File("src/test/resources/META-INF/persistence.xml")), "classes/META-INF/persistence.xml")
                .addAsLibraries(
                        DependencyResolvers.use(MavenDependencyResolver.class)
                        .artifact("org.picketlink:picketlink-core-api:2.5.0-SNAPSHOT")
                        .artifact("org.picketlink:picketlink-core-impl:2.5.0-SNAPSHOT")
                        .artifact("org.picketlink:picketlink-idm-impl:2.5.0-SNAPSHOT")
                        .resolveAsFiles()
                        );

        //System.out.println(war.toString(true));

        return war;
    }

    @Test
    public void testPermissionManagerNotNull() {
        assert permissionManager != null;
    }

    @Test
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void testEntityPermissions() {
        Foo foo = fooAction.createFoo("bar");

        identityManager.add(new SimpleUser("jsmith"));
        User user = identityManager.getUser("jsmith");
        assert user != null;

        //permissionManager.grantPermission(new Permission(foo, user, "read"));

        //List<Permission> permissions = permissionManager.createPermissionQuery()
          //      .setResource(foo)
            //    .getResultList();

        //assert !permissions.isEmpty();
    }
}
