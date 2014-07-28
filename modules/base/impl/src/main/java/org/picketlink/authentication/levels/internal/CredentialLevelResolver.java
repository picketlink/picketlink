package org.picketlink.authentication.levels.internal;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.picketlink.Identity;
import org.picketlink.annotations.PicketLink;
import org.picketlink.authentication.levels.Level;
import org.picketlink.authentication.levels.SecurityLevelResolver;
import org.picketlink.authentication.levels.annotations.SecurityLevel;
import org.picketlink.common.exceptions.PicketLinkException;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.producer.AbstractLevelFactory;

/**
 * Resolves security level from Credentials
 * @author Michal Trnka
 */
@RequestScoped
@PicketLink
public class CredentialLevelResolver implements SecurityLevelResolver {

    @Inject
    DefaultLoginCredentials credentials;

    @Inject
    @Any
    private Identity identity;

    @Inject
    private AbstractLevelFactory abstractFactory;

    protected String getCredentialLevel() {
        if(!identity.isLoggedIn()){
            throw new PicketLinkException();
        }

        if (credentials == null) {
            throw new PicketLinkException();
        }

        Object cred = credentials.getCredential();

        if (cred == null) {
            throw new PicketLinkException();
        }
        return getLevelOfClass(cred);
    }

    private String getLevelOfClass(Object obj) {
        SecurityLevel sl = obj.getClass().getAnnotation(SecurityLevel.class);
        if (sl == null) {
            throw new PicketLinkException();
        }
        return sl.value();
    }

    @Override
    public Level resolve() {
        try{
            String level = getCredentialLevel();
            return abstractFactory.getFactory().createLevel(level);
        }catch(PicketLinkException e){
            return null;
        }
    }
}
