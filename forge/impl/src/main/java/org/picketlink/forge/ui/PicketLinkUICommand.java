package org.picketlink.forge.ui;

import javax.inject.Inject;

import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

/**
 * This is where the magic will happen
 *
 * @author Shane Bryzak
 */
public class PicketLinkUICommand extends AbstractUICommand {

    @Inject private UIInput<String> myName;
    
    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        builder.add(myName);        
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        return Results.success("Hello " + myName.getValue());
    }

    @Override
    public UICommandMetadata getMetadata(UIContext context)
    {
        return Metadata.forCommand(getClass())
                .name("PicketLink: Setup")
                .description("Setups Picketlink")
                .category(Categories.create("PicketLink"));
    }

}
