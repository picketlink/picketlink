package org.picketlink.forge.ui;

import java.util.List;
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
import org.jboss.forge.addon.javaee.cdi.CDIFacet;
import org.jboss.forge.addon.javaee.cdi.ui.CDISetupCommand;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.dependencies.DependencyInstaller;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.ui.command.PrerequisiteCommandsProvider;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.result.navigation.NavigationResultBuilder;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.picketlink.forge.ConfigurationOperations;

/**
 * Adds PicketLink dependencies to a project and creates a default configuration.
 *
 * @author Shane Bryzak
 */
public class PicketLinkSetupCommand extends AbstractProjectCommand implements PrerequisiteCommandsProvider {

    public static final String PICKETLINK_CONFIGURATION_PACKAGE = "picketlinkConfigurationPackage";
    public static final String DEFAULT_CONFIG_PACKAGE = "picketlink.config";

    @Inject ProjectFactory projectFactory;

    @Inject DependencyInstaller dependencyInstaller;

    @Inject DependencyResolver dependencyResolver;

    @Inject @WithAttributes(label = "Version", required = true,
            description = "Select the version of PicketLink", shortName = 'v')
    private UISelectOne<Coordinate> version;

    @Inject @WithAttributes(label = "Show snapshot versions",
            description = "Show snapshot versions in the list")
    private UIInput<Boolean> showSnapshots;

    @Inject @WithAttributes(label = "Configuration package", required = true,
            description = "The PicketLink configuration will be created in this package",
            shortName = 'p')
    private UIInput<String> configurationPackage;

    @Inject ConfigurationOperations configurationOps;

    @Override
    public void initializeUI(UIBuilder builder) throws Exception {

        DependencyQueryBuilder query = DependencyQueryBuilder
                .create("org.picketlink:picketlink-api");
        if (!showSnapshots.getValue()) {
            query.setFilter(new NonSnapshotDependencyFilter());
        }
        final List<Coordinate> coordinates = dependencyResolver.resolveVersions(query);

        Callable<Iterable<Coordinate>> coordinatesBuilder = new Callable<Iterable<Coordinate>>() {
            @Override
            public Iterable<Coordinate> call() throws Exception {
                return coordinates;
            }

        };

        version.setValueChoices(coordinatesBuilder);
        version.setItemLabelConverter(new Converter<Coordinate,String>() {
            @Override
            public String convert(Coordinate source) {
                return source != null ? String.format("PicketLink %s", source.getVersion()) : null;
            }
        });
        if (!coordinates.isEmpty()) {
            Coordinate defaultCoord = coordinates.get(coordinates.size() - 1);
            for (int i = coordinates.size() - 1; i >= 0; i--) {
                String version = coordinates.get(i).getVersion();
                if (version != null && version.toLowerCase().contains("final")) {
                    defaultCoord = coordinates.get(i);
                    break;
                }
            }
            version.setDefaultValue(defaultCoord);
        }

        builder.add(version);
        builder.add(showSnapshots);

        Project project = getSelectedProject(builder.getUIContext());
        ConfigurationFacet facet = project.getFacet(ConfigurationFacet.class);
        Configuration config = facet.getConfiguration();
        if (config.containsKey(PICKETLINK_CONFIGURATION_PACKAGE)) {
            configurationPackage.setValue(config.getString(PICKETLINK_CONFIGURATION_PACKAGE));
        } else {
            MetadataFacet metadataFacet = project.getFacet(MetadataFacet.class);
            configurationPackage.setValue(metadataFacet.getTopLevelPackage() + "." + DEFAULT_CONFIG_PACKAGE);
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

    @Override
    public NavigationResult getPrerequisiteCommands(UIContext context) {
        NavigationResultBuilder builder = NavigationResultBuilder.create();
        Project project = getSelectedProject(context);
        if (project != null)
        {
           if (!project.hasFacet(CDIFacet.class))
           {
              builder.add(CDISetupCommand.class);
           }
        }
        return builder.build();
    }

}
