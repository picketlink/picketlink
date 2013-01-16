package org.picketlink.idm.model;

/**
 * Simple implementation of the Agent interface
 * 
 * @author Shane Bryzak
 */
public class SimpleAgent extends AbstractIdentityType implements Agent {
    private static final long serialVersionUID = -7418037050013416323L;

    private String loginName;

    public SimpleAgent(String loginName) {
        this.loginName = loginName;
    }

    @Override
    public String getLoginName() {
        return loginName;
    }
    
    @Override
    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }
}
