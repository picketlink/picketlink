package org.picketlink.test.idm.other.shane.model.scenario2;

import org.picketlink.idm.model.AbstractIdentityType;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.query.QueryParameter;

/**
 * 
 * @author Shane Bryzak
 *
 */
public class User extends AbstractIdentityType implements Account {
    private static final long serialVersionUID = -1123904143500412864L;

    public static final QueryParameter LOGIN_NAME = QUERY_ATTRIBUTE.byName("loginName");

    @AttributeProperty private String loginName;
    @AttributeProperty private String firstName;
    @AttributeProperty private String lastName;

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
