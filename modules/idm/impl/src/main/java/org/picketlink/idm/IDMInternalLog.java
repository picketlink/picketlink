package org.picketlink.idm;

import org.jboss.logging.LogMessage;
import org.jboss.logging.Logger;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;
import org.picketlink.common.logging.LogFactory;
import org.picketlink.idm.model.AttributedType;

import static org.picketlink.idm.IDMInternalLog.PICKETLINK_IDM_PROJECT_CODE;

/**
 * @author Pedro Igor
 */
@MessageLogger(projectCode = PICKETLINK_IDM_PROJECT_CODE)
public interface IDMInternalLog extends IDMLog {

    /**
     * <p>This is the logger for the {@link org.picketlink.idm.file.internal.FileIdentityStore}.</p>
     */
    IDMInternalLog FILE_STORE_LOGGER = LogFactory.getLog(IDMInternalLog.class, IDMInternalLog.class.getPackage().getName() + ".identity.store.file");

    /**
     * <p>This is the logger for the {@link org.picketlink.idm.jpa.internal.JPAIdentityStore}.</p>
     */
    IDMInternalLog JPA_STORE_LOGGER = LogFactory.getLog(IDMInternalLog.class, IDMInternalLog.class.getPackage().getName() + ".identity.store.jpa");

    /**
     * <p>This is the logger for the {@link org.picketlink.idm.ldap.internal.LDAPIdentityStore}.</p>
     */
    IDMInternalLog LDAP_STORE_LOGGER = LogFactory.getLog(IDMInternalLog.class, IDMInternalLog.class.getPackage().getName() + ".identity.store.ldap");
    /**
     * <p>This is the logger for the {@link org.picketlink.idm.ldap.internal.JDBCIdentityStore}.</p>
     */
    IDMInternalLog JDBC_STORE_LOGGER = LogFactory.getLog(IDMInternalLog.class, IDMInternalLog.class.getPackage().getName() + ".identity.store.jdbc");

    // File store logging messages. Ids 1100-1199.
    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 1100, value = "Using working directory [%s].")
    void fileConfigUsingWorkingDir(String path);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 1101, value = "Working directory [%s] is marked to be always created. All your existing data will be lost.")
    void fileConfigAlwaysCreateWorkingDir(String path);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id=1102, value = "Async write enabled. Using thread pool of size %s")
    void fileAsyncWriteEnabled(int threadPoolSize);

    // LDAP store logging messages. Ids 1200-1299

    @LogMessage(level = Logger.Level.INFO)
    @Message(id=1200, value = "LDAP Store is configured for Active Directory.")
    void ldapActiveDirectoryConfiguration();

    @LogMessage(level = Logger.Level.WARN)
    @Message(id=1201, value = "LDAP Store does not support relationship updates [%s].")
    void ldapRelationshipUpdateNotSupported(AttributedType attributedType);

    // JPA store logging messages. Ids 1300-1399

    @LogMessage(level = Logger.Level.INFO)
    @Message(id=1300, value = "No ContextInitializer provided for the JPA Store. The store maybe be unable to retrieve the EntityManager instance to perform operations.")
    void jpaContextInitializerNotProvided();
}
