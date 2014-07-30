package org.picketlink.idm.credential;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.annotation.StereotypeProperty;

import java.lang.reflect.Constructor;

import static org.picketlink.common.reflection.Reflections.classForName;
import static org.picketlink.common.reflection.Reflections.findDeclaredConstructor;

/**
 * <p>Represents a token credential.</p>
 *
 * <p>Basically, a token is a self-contained repository for identities and claims for a particular subject.
 *
 * <p>Each token type has its own {@link org.picketlink.idm.credential.Token.Provider} and {@link org.picketlink.idm.credential.Token.Consumer}.
 *  The first is responsible for manage a specific token type (eg.: issue, renew, invalidate, etc). The latter is responsible for
 *  consume a specific token type, providing to clients all the necessary code to properly handle a specific token type.</p>
 *
 * @author Pedro Igor
 *
 * @see org.picketlink.idm.credential.Token.Provider
 * @see org.picketlink.idm.credential.Token.Consumer
 * @see org.picketlink.idm.credential.TokenCredential
 * @see org.picketlink.idm.credential.handler.TokenCredentialHandler
 */
public interface Token {

    /**
     * <p>Returns the type of the token.</p>
     *
     * @return
     */
    String getType();

    /**
     * <p>Returns the subject identifier. The identifier usually represents a unique and never reassigned identifier within the
     * Issuer for the End-User. Which is intended to be consumed by the Client.</p>
     *
     * @return
     */
    String getSubject();

    /**
     * <p>Returns the string representation of a token.</p>
     *
     * @return
     */
    String getToken();

    /**
     * <p>
     *     Token providers are responsible to provide some importantant management operations for a specific {@link Token} type.
     * </p>
     *
     * @author Pedor Igor
     */
    public interface Provider<T extends Token> {

        /**
         * <p>Issues a new token for the given {@link org.picketlink.idm.model.Account}.</p>
         *
         * @param account
         * @return
         */
        T issue(Account account);

        /**
         * <p>
         *     Renew a token based on the current token in use.
         * </p>
         *
         * @param renewToken
         * @return
         */
        T renew(Account account, T renewToken);

        /**
         * <p>Invalidates the current token for the given {@link org.picketlink.idm.model.Account}.</p>
         *
         * @param account
         */
        void invalidate(Account account);

        /**
         * <p>Returns the {@link org.picketlink.idm.credential.Token} type supported by this provider.</p>
         *
         * @return
         */
        Class<T> getTokenType();
    }

    /**
     * <p>
     *     Token consumers are responsible to provide all the necessary support to consume information from a specific {@link org.picketlink.idm.credential.Token}.
     * </p>
     *
     * @author Pedor Igor
     */
    public interface Consumer<T extends Token> {

        /**
         * <p>Extracts a certain {@link org.picketlink.idm.model.IdentityType} considering the information from the given {@link Token}.</p>
         *
         * <p>Usually, a token contains a set of claims which can be mapped to the identity types supported by PicketLink {@link org.picketlink.idm.model.annotation.IdentityStereotype.Stereotype}.
         * Each stereotype has a set of common properties that can be used to identify them. In this case, the {@link org.picketlink.idm.model.annotation.StereotypeProperty.Property} should be
         * used to tell which property of the given identity type should be populated with the <code>identifier</code> value if there is any claim in the token representing it.</p>
         *
         * @param token The token.
         * @param identityType The type of the identity type that should be created based on the claims of a token.
         * @param stereotypeProperty The stereotype property used to identify and populate the identity type instance from the token claims.
         * @param identifier The value of the identifier used to match the existence of a identity type based on the token claims set.
         * @return An identity type instance of there is any claim from the token referencing it. Otherwise this method returns null.
         */
        <I extends IdentityType> I extractIdentity(T token, Class<I> identityType, StereotypeProperty.Property stereotypeProperty, Object identifier);

        /**
         * <p>
         *     Validates a token.
         * </p>
         *
         * @param token
         * @return
         */
        boolean validate(T token);

        /**
         * <p>Returns the {@link org.picketlink.idm.credential.Token} type supported by this consumer.</p>
         *
         * @return
         */
        Class<T> getTokenType();
    }

    public static class Builder {
        /**
         * <p>Creates a {@link org.picketlink.idm.credential.Token} instance from the given {@link org.picketlink.idm.credential.storage.TokenCredentialStorage}.</p>
         *
         * @return
         * @throws org.picketlink.idm.IdentityManagementException
         */
        public static Token create(String tokenType, String tokenValue) throws IdentityManagementException {
            try {
                Class<Token> tokenClazz = classForName(tokenType);
                Constructor<Token> expectedConstructor = (Constructor<Token>) findDeclaredConstructor(tokenClazz, String.class);

                if (expectedConstructor == null) {
                    throw new IdentityManagementException("Token type [" + tokenClazz.getName() + "] must provide a constructor that accepts a String.");
                }

                return expectedConstructor.newInstance(tokenValue);
            } catch (ClassCastException cce) {
                throw new IdentityManagementException("Wrong Token type [" + tokenType + "]. It must be a subclass of [" + Token.class.getName() + "].", cce);
            } catch (ClassNotFoundException cnfe) {
                throw new IdentityManagementException("Token type not found [" + tokenType + "].", cnfe);
            } catch (Exception e) {
                throw new IdentityManagementException("Could not create Token type [" + tokenType + "].", e);
            }
        }
    }
}
