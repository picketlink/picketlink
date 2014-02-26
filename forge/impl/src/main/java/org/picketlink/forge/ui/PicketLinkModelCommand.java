package org.picketlink.forge.ui;

import javax.inject.Inject;

import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

/**
 * Provides features for generating a custom identity model
 *
 * @author Shane Bryzak
 *
 */
public class PicketLinkModelCommand extends AbstractProjectCommand {
    @Inject ProjectFactory projectFactory;

    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UICommandMetadata getMetadata(UIContext context)
    {
        return Metadata.forCommand(getClass())
                .name("PicketLink: Create Model")
                .description("Creates a new Identity Model for your project.")
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
