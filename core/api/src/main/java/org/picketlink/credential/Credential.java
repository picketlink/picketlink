package org.picketlink.credential;

/**
 * Contains a single credential, such as a password
 * 
 * @author Shane Bryzak
 */
public interface Credential<T>
{
    T getValue();
}
