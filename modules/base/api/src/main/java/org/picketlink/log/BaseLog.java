/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketlink.log;

import org.jboss.logging.Cause;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Logger;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;
import org.picketlink.Identity;
import org.picketlink.common.logging.Log;
import org.picketlink.common.logging.LogFactory;

/**
 * @author Pedro Igor
 */
@MessageLogger(projectCode = BaseLog.PICKETLINK_BASE_PROJECT_CODE)
public interface BaseLog extends Log {

    String PICKETLINK_BASE_PROJECT_CODE = "PLINK";

    /**
     * <p>This is the root logger. General messages should be logged using it.</p>
     */
    BaseLog ROOT_LOGGER = LogFactory.getLog(BaseLog.class, Identity.class.getPackage().getName());

    /**
     * <p>This is the authentication logger. Authentication messages should be logged using it.</p>
     */
    BaseLog AUTHENTICATION_LOGGER = LogFactory.getLog(BaseLog.class, Identity.class.getPackage().getName() + ".authentication");

    /**
     * <p>This is the Http logger. All messages related with Http Security should be looged using it.</p>
     */
    BaseLog HTTP_LOGGER = LogFactory.getLog(BaseLog.class, Identity.class.getPackage().getName() + ".http");

    // General logging messages. Ids 2000-2099.
    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 2000, value = "Bootstrapping PicketLink")
    void picketlinkBootstrap();

    // Authentication logging messages. Ids 2100-2199.

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 2100, value = "Authentication failed for account [%s].")
    void authenticationFailed(String accountName, @Cause Throwable ae);
}
