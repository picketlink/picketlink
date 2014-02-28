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

/**
 * Provides features for generating a custom identity model
 *
 * @author Shane Bryzak
 */
public class PicketLinkSchemaCommand extends AbstractProjectCommand {
    public static final String PICKETLINK_SCHEMA_PACKAGE = "picketlinkSchemaPackage";
    public static final String DEFAULT_SCHEMA_PACKAGE = "picketlink.schema";

    @Inject ProjectFactory projectFactory;

    @Inject @WithAttributes(label = "Model package", required = true,
            description = "The Identity Model will be created in this package",
            shortName = 'm')
    private UIInput<String> modelPackage;

    @Inject @WithAttributes(label = "Schema package", required = true,
            description = "The Identity Schema will be created in this package",
            shortName = 's')
    private UIInput<String> schemaPackage;

    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        Project project = getSelectedProject(builder.getUIContext());
        ConfigurationFacet facet = project.getFacet(ConfigurationFacet.class);
        Configuration config = facet.getConfiguration();
        if (config.containsKey(PicketLinkModelCommand.PICKETLINK_MODEL_PACKAGE)) {
            modelPackage.setValue(config.getString(PicketLinkModelCommand.PICKETLINK_MODEL_PACKAGE));
        } else {
            MetadataFacet metadataFacet = project.getFacet(MetadataFacet.class);
            modelPackage.setValue(metadataFacet.getTopLevelPackage() + "." + PicketLinkModelCommand.DEFAULT_MODEL_PACKAGE);
        }

        builder.add(modelPackage);

        if (config.containsKey(PICKETLINK_SCHEMA_PACKAGE)) {
            schemaPackage.setValue(config.getString(PICKETLINK_SCHEMA_PACKAGE));
        } else {
            MetadataFacet metadataFacet = project.getFacet(MetadataFacet.class);
            schemaPackage.setValue(metadataFacet.getTopLevelPackage() + "." + DEFAULT_SCHEMA_PACKAGE);
        }

        builder.add(schemaPackage);
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        Project project = getSelectedProject(context);

        ConfigurationFacet facet = project.getFacet(ConfigurationFacet.class);
        Configuration config = facet.getConfiguration();
        config.setProperty(PICKETLINK_SCHEMA_PACKAGE, modelPackage.getValue());

        return Results.success("Successful");
    }

    @Override
    public UICommandMetadata getMetadata(UIContext context)
    {
        return Metadata.forCommand(getClass())
                .name("PicketLink: Create Schema")
                .description("Generates a JPA schema for your Identity Model.")
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