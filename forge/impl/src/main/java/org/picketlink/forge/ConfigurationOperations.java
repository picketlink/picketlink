package org.picketlink.forge;

import java.io.FileNotFoundException;

import javax.inject.Inject;

import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.parser.java.JavaSourceFactory;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.parser.java.resources.JavaResource;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.parser.java.JavaClass;

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
        JavaClass javaClass = javaSourceFactory.create(JavaClass.class)
                .setName(className)
                .setPublic();

        javaClass.setPackage(packageName);


        return javaClass;
    }
}
