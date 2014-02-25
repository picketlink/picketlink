package org.picketlink.forge;

import java.io.FileNotFoundException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.parser.java.JavaSourceFactory;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.parser.java.resources.JavaResource;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.parser.java.JavaClass;
import org.jboss.forge.parser.java.Method;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;

/**
 * Performs tasks related to creating and updating a PicketLink configuration
 *
 * @author Shane Bryzak
 *
 */
public class ConfigurationOperations {

    public static final String PICKETLINK_CONFIGURATION_CLASS_NAME = "PicketLinkConfiguration";

    @Inject FacetFactory facetFactory;

    @Inject JavaSourceFactory javaSourceFactory;

    public JavaResource newDefaultConfiguration(Project project, String packageName) throws FileNotFoundException {
        final JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
        JavaClass javaClass = createJavaClass(PICKETLINK_CONFIGURATION_CLASS_NAME, packageName);
        return java.saveJavaSource(javaClass);
    }

    private JavaClass createJavaClass(String className, String packageName) {
        String lineSeparator = System.getProperty("line.separator");

        JavaClass javaClass = javaSourceFactory.create(JavaClass.class)
                .setName(className)
                .setPublic()
                .addAnnotation(ApplicationScoped.class).getOrigin();

        javaClass.setPackage(packageName);

        javaClass.addField("private IdentityConfiguration identityConfig = null;");

        Method<JavaClass> producerMethod = javaClass.addMethod("IdentityConfiguration createConfig() { " + lineSeparator +
                "  if (identityConfig == null) {" + lineSeparator +
                "    initConfig();" + lineSeparator +
                "  }" + lineSeparator +
                "  return identityConfig;" + lineSeparator +
                "}");
        producerMethod.addAnnotation(Produces.class);

        javaClass.addMethod("private void initConfig() {" + lineSeparator +
              "  IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();" + lineSeparator +
              "  builder" + lineSeparator +
              "    .named(\"default\")" + lineSeparator +
              "      .stores()" + lineSeparator +
              "        .file()" + lineSeparator +
              "          .supportAllFeatures();" + lineSeparator +
              "  identityConfig = builder.build();" +
              "}");

        javaClass.addImport(IdentityConfigurationBuilder.class);
        javaClass.addImport(IdentityConfiguration.class);

        return javaClass;
    }
}
