package org.picketlink.producer;

import org.picketlink.annotations.PicketLink;
import org.picketlink.authentication.levels.LevelFactory;
import org.picketlink.authentication.levels.internal.DefaultLevelFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Provides correct level factory
 *
 * @author Michal Trnka
 */
@ApplicationScoped
public class LevelFactoryResolver {

    @Inject
    @Any
    @PicketLink
    private Instance<LevelFactory> factory;

    @Inject
    private DefaultLevelFactory defFactory;

    public LevelFactory resolve() {
        if(factory.isUnsatisfied()){
            return defFactory;
        }else{
            return factory.get();
        }
    }
}
