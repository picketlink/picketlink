package org.picketlink.idm;

/**
 * Interface used to control transactions on resource-local identity stores
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface IdentityTransaction {
    /**
     * Start a resource transaction.
     *
     * @throws IllegalStateException if <code>isActive()</code> is true
     */
    void begin();

    /**
     * Commit the current resource transaction, writing any
     * unflushed changes to the database.
     *
     * @throws IllegalStateException if <code>isActive()</code> is false
     */
    void commit();

    /**
     * Roll back the current resource transaction.
     *
     * @throws IllegalStateException if <code>isActive()</code> is false
     */
    void rollback();

    /**
     * Mark the current resource transaction so that the only
     * possible outcome of the transaction is for the transaction
     * to be rolled back.
     *
     * @throws IllegalStateException if <code>isActive()</code> is false
     */
    void setRollbackOnly();

    /**
     * Determine whether the current resource transaction has been
     * marked for rollback.
     *
     * @return boolean indicating whether the transaction has been
     *         marked for rollback
     * @throws IllegalStateException if <code>isActive()</code> is false
     */
    boolean getRollbackOnly();

    /**
     * Indicate whether a resource transaction is in progress.
     *
     * @return boolean indicating whether transaction is
     *         in progress
     */
    boolean isActive();
}
