package org.picketlink.forge;

import java.io.FileNotFoundException;

import javax.inject.Inject;

import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.parser.java.JavaSourceFactory;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.parser.java.Field;
import org.jboss.forge.parser.java.JavaClass;
import org.picketlink.idm.model.AbstractIdentityType;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.model.annotation.InheritsPrivileges;
import org.picketlink.idm.model.annotation.Unique;

/**
 * Creates and modifies identity model classes
 *
 * @author Shane Bryzak
 */
public class ModelOperations {
    @Inject FacetFactory facetFactory;

    @Inject JavaSourceFactory javaSourceFactory;

    public void createIdentityModel(Project project, String packageName, boolean supportRole,
            boolean supportGroup, boolean supportRealm) 
            throws FileNotFoundException {
        final JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
        java.saveJavaSource(createUserClass(packageName));

        if (supportRole) {
            java.saveJavaSource(createRoleClass(packageName));
        }

        if (supportGroup) {
            java.saveJavaSource(createGroupClass(packageName));
        }
    }

    private JavaClass createUserClass(String packageName) {
        String lineSeparator = System.getProperty("line.separator");

        JavaClass javaClass = javaSourceFactory.create(JavaClass.class)
                .setName("User")
                .setPublic()
                .addInterface(Account.class)
                .setSuperType(AbstractIdentityType.class);

        javaClass.setPackage(packageName);

        // Create the loginName property
        Field<JavaClass> f = javaClass.addField("private String loginName = null;");
        f.addAnnotation(AttributeProperty.class);
        f.addAnnotation(Unique.class);

        javaClass.addMethod("public String getLoginName() {" + lineSeparator +
                "  return loginName;" + lineSeparator +
                "}");

        javaClass.addMethod("public void setLoginName(String loginName) {" + lineSeparator +
                "  this.loginName = loginName;" + lineSeparator + 
                "}");

        // Create the email property
        f = javaClass.addField("private String email = null;");
        f.addAnnotation(AttributeProperty.class);
        f.addAnnotation(Unique.class);

        javaClass.addMethod("public String getEmail() {" + lineSeparator +
                "  return email;" + lineSeparator +
                "}");

        javaClass.addMethod("public void setEmail(String email) {" + lineSeparator +
                "  this.email = email;" + lineSeparator + 
                "}");

        // Create the firstName property
        f = javaClass.addField("private String firstName = null;");
        f.addAnnotation(AttributeProperty.class);

        javaClass.addMethod("public String getFirstName() {" + lineSeparator +
                "  return firstName;" + lineSeparator +
                "}");

        javaClass.addMethod("public void setFirstName(String firstName) {" + lineSeparator +
                "  this.firstName = firstName;" + lineSeparator + 
                "}");

        // Create the lastName property
        f = javaClass.addField("private String lastName = null;");
        f.addAnnotation(AttributeProperty.class);

        javaClass.addMethod("public String getLastName() {" + lineSeparator +
                "  return lastName;" + lineSeparator +
                "}");

        javaClass.addMethod("public void setLastName(String lastName) {" + lineSeparator +
                "  this.lastName = lastName;" + lineSeparator + 
                "}");

        // Add the necessary imports
        javaClass.addImport(Account.class);
        javaClass.addImport(AttributeProperty.class);
        javaClass.addImport(AbstractIdentityType.class);

        return javaClass;
    }

    private JavaClass createRoleClass(String packageName) {
        String lineSeparator = System.getProperty("line.separator");

        JavaClass javaClass = javaSourceFactory.create(JavaClass.class)
                .setName("Role")
                .setPublic()
                .setSuperType(AbstractIdentityType.class);

        javaClass.setPackage(packageName);

        // Create the name property
        Field<JavaClass> f = javaClass.addField("private String name = null;");
        f.addAnnotation(AttributeProperty.class);
        f.addAnnotation(Unique.class);

        javaClass.addMethod("public String getName() {" + lineSeparator +
                "  return name;" + lineSeparator +
                "}");

        javaClass.addMethod("public void setName(String name) {" + lineSeparator +
                "  this.name = name;" + lineSeparator + 
                "}");

        // Add the necessary imports
        javaClass.addImport(AttributeProperty.class);
        javaClass.addImport(AbstractIdentityType.class);

        return javaClass;
    }

    private JavaClass createGroupClass(String packageName) {
        String lineSeparator = System.getProperty("line.separator");

        JavaClass javaClass = javaSourceFactory.create(JavaClass.class)
                .setName("Group")
                .setPublic()
                .setSuperType(AbstractIdentityType.class);

        javaClass.setPackage(packageName);

        // Create the name property
        Field<JavaClass> f = javaClass.addField("private String name = null;");
        f.addAnnotation(AttributeProperty.class);

        javaClass.addMethod("public String getName() {" + lineSeparator +
                "  return name;" + lineSeparator +
                "}");

        javaClass.addMethod("public void setName(String name) {" + lineSeparator +
                "  this.name = name;" + lineSeparator + 
                "}");

        // Create the path property
        f = javaClass.addField("private String path = null;");
        f.addAnnotation(AttributeProperty.class);
        f.addAnnotation(Unique.class);

        javaClass.addMethod("public String getPath() {" + lineSeparator +
                "  this.path = buildPath(this);" + lineSeparator +
                "  return this.path;" + lineSeparator +
                "}");

        javaClass.addMethod("public void setPath(String path) {" + lineSeparator +
                "  this.path = path;" + lineSeparator +
                "}");

        // Create the buildPath() method
        javaClass.addField("public static final String PATH_SEPARATOR = \"/\";");
        javaClass.addMethod("private String buildPath(Group group) {" + lineSeparator +
                "  String name = PATH_SEPARATOR + group.getName(); " + lineSeparator +
                "  if (group.getParentGroup() != null) { " + lineSeparator +
                "    name = buildPath(group.getParentGroup()) + name; " + lineSeparator +
                "  }" + lineSeparator +
                "  return name;" + lineSeparator +
                "}");

        // Create the parentGroup property
        f = javaClass.addField("private Group parentGroup = null;");
        f.addAnnotation(InheritsPrivileges.class);
        f.addAnnotation(AttributeProperty.class);

        javaClass.addMethod("public Group getParentGroup() {" + lineSeparator +
                "  return this.parentGroup;" + lineSeparator +
                "}");

        javaClass.addMethod("public void setParentGroup(Group parentGroup) {" + lineSeparator +
                "  this.parentGroup = parentGroup;" + lineSeparator +
                "}");

        // Add the necessary imports
        javaClass.addImport(AttributeProperty.class);
        javaClass.addImport(AbstractIdentityType.class);

        return javaClass;
    }

}
