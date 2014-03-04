package org.picketlink.forge.ui;

import javax.inject.Inject;

import org.jboss.forge.addon.configuration.Configuration;
import org.jboss.forge.addon.configuration.facets.ConfigurationFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.picketlink.forge.ModelOperations;

/**
 * Provides features for generating a custom identity model
 *
 * @author Shane Bryzak
 *
 */
public class PicketLinkModelCommand extends AbstractProjectCommand {
    public static final String PICKETLINK_MODEL_PACKAGE = "picketlinkModelPackage";
    public static final String DEFAULT_MODEL_PACKAGE = "picketlink.model";

    @Inject ProjectFactory projectFactory;

    @Inject @WithAttributes(label = "Model package", required = true,
            description = "The Identity Model will be created in this package",
            shortName = 'p')
    private UIInput<String> modelPackage;

    @Inject @WithAttributes(label = "Support for Roles", required = false,
            description = "Select this if the Identity Model should support roles", shortName = 'r')
    private UIInput<Boolean> supportRoles;

    @Inject @WithAttributes(label = "Support for Groups", required = false,
            description = "Select this if the Identity Model should support groups", shortName = 'g')
    private UIInput<Boolean> supportGroups;

    @Inject @WithAttributes(label = "Support for Realms", required = false,
            description = "Select this if the Identity Model should support realm partitions", shortName = 'p')
    private UIInput<Boolean> supportRealms;

    @Inject ModelOperations modelOps;

    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        Project project = getSelectedProject(builder.getUIContext());
        ConfigurationFacet facet = project.getFacet(ConfigurationFacet.class);
        Configuration config = facet.getConfiguration();
        if (config.containsKey(PICKETLINK_MODEL_PACKAGE)) {
            modelPackage.setValue(config.getString(PICKETLINK_MODEL_PACKAGE));
        } else {
            MetadataFacet metadataFacet = project.getFacet(MetadataFacet.class);
            modelPackage.setValue(metadataFacet.getTopLevelPackage() + "." + DEFAULT_MODEL_PACKAGE);
        }

        builder.add(modelPackage);

        supportRoles.setValue(true);
        builder.add(supportRoles);

        supportGroups.setValue(true);
        builder.add(supportGroups);

        builder.add(supportRealms);
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        Project project = getSelectedProject(context);

        modelOps.createIdentityModel(project, modelPackage.getValue(), supportRoles.getValue(), supportGroups.getValue(), 
                supportRealms.getValue());

        ConfigurationFacet facet = project.getFacet(ConfigurationFacet.class);
        Configuration config = facet.getConfiguration();
        config.setProperty(PICKETLINK_MODEL_PACKAGE, modelPackage.getValue());

        return Results.success("Successful");
    }

    @Override
    public UICommandMetadata getMetadata(UIContext context)
    {
        return Metadata.forCommand(getClass())
                .name("PicketLink: Create Model")
                .description("Generates a new Identity Model for your project.")
                .category(Categories.create("PicketLink"));
    }

    @Override
    protected boolean isProjectRequired() {
        return true;
    }

    @Override
    protected ProjectFactory getProjectFactory() {
        return projectFactory;
    }

}
