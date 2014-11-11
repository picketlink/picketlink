package org.picketlink.authentication.levels.internal;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.picketlink.Identity;
import org.picketlink.annotations.PicketLink;
import org.picketlink.authentication.Authenticator;
import org.picketlink.authentication.levels.Level;
import org.picketlink.authentication.levels.SecurityLevelResolver;
import org.picketlink.authentication.levels.annotations.SecurityLevel;
import org.picketlink.common.exceptions.PicketLinkException;
import org.picketlink.producer.AbstractLevelFactory;

/**
 * Resolves security level from Authenticator
 * @author Michal Trnka
 */
@PicketLink
public class AuthenticatorLevelResolver implements SecurityLevelResolver{

    @Inject
    @PicketLink
    private Instance<Authenticator> authenticatorInstance;

    @Inject
    @Any
    private Identity identity;

    @Inject
    private AbstractLevelFactory abstractFactory;

    protected String resolveLevel(){
        if(!identity.isLoggedIn()){
            throw new PicketLinkException();
        }
        if(authenticatorInstance.isUnsatisfied()){
            throw new PicketLinkException();
        }
        SecurityLevel a = authenticatorInstance.get().getClass().getAnnotation(SecurityLevel.class);
        if (a == null) {
            throw new PicketLinkException();
        }
        return a.value();
    }

    @Override
    public Level resolve() {
        try{
            String level = resolveLevel();
            return abstractFactory.getFactory().createLevel(level);
        }catch(PicketLinkException e){
            return null;
        }
    }

}
