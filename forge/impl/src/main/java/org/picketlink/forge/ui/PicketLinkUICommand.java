package org.picketlink.forge.ui;

import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.DependencyQuery;
import org.jboss.forge.addon.dependencies.DependencyResolver;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.dependencies.builder.DependencyQueryBuilder;
import org.jboss.forge.addon.dependencies.util.NonSnapshotDependencyFilter;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.dependencies.DependencyInstaller;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.furnace.addons.Addon;

/**
 * This is where the magic will happen
 *
 * @author Shane Bryzak
 */
public class PicketLinkUICommand extends AbstractProjectCommand {
    
    private static final String VERSION = "${project.version}";
    
    @Inject ProjectFactory projectFactory;
    
    @Inject DependencyInstaller dependencyInstaller;
    
    @Inject DependencyResolver dependencyResolver;

    @Inject private UISelectOne<Coordinate> version;
    
    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        
        DependencyQuery query = DependencyQueryBuilder
                .create("org.picketlink:picketlink-api")
                .setFilter(new NonSnapshotDependencyFilter());

        List<Coordinate> coordinates = dependencyResolver.resolveVersions(query);
        version.setValueChoices(coordinates);
        builder.add(version);        
        
        
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        DependencyBuilder builder = DependencyBuilder.create();
        builder.setCoordinate(version.getValue());
        
        dependencyInstaller.install(getSelectedProject(context), builder);

        return Results.success("Successful");
    }

    @Override
    public UICommandMetadata getMetadata(UIContext context)
    {
        return Metadata.forCommand(getClass())
                .name("PicketLink: Setup")
                .description("Setups Picketlink")
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
