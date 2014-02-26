package org.picketlink.forge.ui;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.jboss.forge.addon.configuration.Configuration;
import org.jboss.forge.addon.configuration.facets.ConfigurationFacet;
import org.jboss.forge.addon.convert.Converter;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.DependencyResolver;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.dependencies.builder.DependencyQueryBuilder;
import org.jboss.forge.addon.dependencies.util.NonSnapshotDependencyFilter;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.dependencies.DependencyInstaller;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.picketlink.forge.ConfigurationOperations;

/**
 * Adds PicketLink dependencies to a project and creates a default configuration.
 *
 * @author Shane Bryzak
 */
public class PicketLinkSetupCommand extends AbstractProjectCommand {

    public static final String PICKETLINK_CONFIGURATION_PACKAGE = "PICKETLINK_CONFIGURATION_PACKAGE";

    @Inject ProjectFactory projectFactory;

    @Inject DependencyInstaller dependencyInstaller;

    @Inject DependencyResolver dependencyResolver;

    @Inject @WithAttributes(label = "Version", required = true,
            description = "Select the version of PicketLink", shortName = 'v')
    private UISelectOne<Coordinate> version;

    @Inject @WithAttributes(label = "Include snapshot versions",
            description = "Include snapshot versions in the list")
    private UIInput<Boolean> showSnapshots;

    @Inject @WithAttributes(label = "Configuration package", required = true,
            description = "The PicketLink configuration will be created in this package",
            shortName = 'p')
    private UIInput<String> configurationPackage;

    @Inject ConfigurationOperations configurationOps;

    @Override
    public void initializeUI(UIBuilder builder) throws Exception {

        Callable<Iterable<Coordinate>> coordinatesBuilder = new Callable<Iterable<Coordinate>>() {
            @Override
            public Iterable<Coordinate> call() throws Exception {

                DependencyQueryBuilder query = DependencyQueryBuilder
                        .create("org.picketlink:picketlink-api");
                if (!showSnapshots.getValue()) {
                    query.setFilter(new NonSnapshotDependencyFilter());
                }
                return dependencyResolver.resolveVersions(query);
            }

        };

        version.setValueChoices(coordinatesBuilder);
        version.setItemLabelConverter(new Converter<Coordinate,String>() {
            @Override
            public String convert(Coordinate source) {
                return source != null ? String.format("PicketLink %s", source.getVersion()) : null;
            }
        });
        builder.add(version);
        builder.add(showSnapshots);

        Project project = getSelectedProject(builder.getUIContext());
        ConfigurationFacet facet = project.getFacet(ConfigurationFacet.class);
        Configuration config = facet.getConfiguration();
        if (config.containsKey(PICKETLINK_CONFIGURATION_PACKAGE)) {
            configurationPackage.setValue(config.getString(PICKETLINK_CONFIGURATION_PACKAGE));
        }

        builder.add(configurationPackage);

    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        Project project = getSelectedProject(context);

        DependencyBuilder builder = DependencyBuilder.create();
        builder.setCoordinate(version.getValue());
        dependencyInstaller.install(project, builder);
        builder.getCoordinate().setArtifactId("picketlink-impl");
        dependencyInstaller.install(project, builder);

        configurationOps.newDefaultConfiguration(project, configurationPackage.getValue());

        ConfigurationFacet facet = project.getFacet(ConfigurationFacet.class);
        Configuration config = facet.getConfiguration();
        config.setProperty(PICKETLINK_CONFIGURATION_PACKAGE, configurationPackage.getValue());

        return Results.success("Successful");
    }

    @Override
    public UICommandMetadata getMetadata(UIContext context)
    {
        return Metadata.forCommand(getClass())
                .name("PicketLink: Setup")
                .description("Installs the PicketLink dependencies into your project's pom.xml and creates a default configuration.")
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
