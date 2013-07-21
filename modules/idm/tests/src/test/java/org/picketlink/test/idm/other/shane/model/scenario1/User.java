package org.picketlink.test.idm.other.shane.model.scenario1;

import java.util.List;

import org.picketlink.idm.model.AbstractIdentityType;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.test.idm.other.shane.model.scenario1.entity.EmployeeContract;
import org.picketlink.test.idm.other.shane.model.scenario1.entity.UserAddress;
import org.picketlink.test.idm.other.shane.model.scenario1.entity.UserEmail;

/**
 * 
 * @author Shane Bryzak
 *
 */
public class User extends AbstractIdentityType implements Account {
    private static final long serialVersionUID = -1123904143500412864L;

    @AttributeProperty private String loginName;
    @AttributeProperty private String firstName;
    @AttributeProperty private String lastName;
    private List<UserAddress> addresses;
    private List<UserEmail> emails;
    private EmployeeContract employeeContract;

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

    public List<UserAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<UserAddress> addresses) {
        this.addresses = addresses;
    }

    public List<UserEmail> getEmails() {
        return emails;
    }

    public void setEmails(List<UserEmail> emails) {
        this.emails = emails;
    }

    public EmployeeContract getEmployeeContract() {
        return employeeContract;
    }

    public void setEmployeeContract(EmployeeContract employeeContract) {
        this.employeeContract = employeeContract;
    }
}
