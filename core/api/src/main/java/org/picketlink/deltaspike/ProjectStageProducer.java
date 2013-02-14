/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.picketlink.deltaspike;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import java.io.Serializable;
import java.util.logging.Logger;

/**
 * <p>Produces {@link org.apache.deltaspike.core.api.projectstage.ProjectStage} configurations.</p>
 *
 * <p>The producer will try to detect the currently active ProjectStage on startup
 * and use that for all generated fields.</p>
 * <p>Usage:</p>
 * Simply inject the current ProjectStage into any bean:
 * <pre>
 * public class MyBean {
 *   private @Inject ProjectStage projectStage;
 *
 *   public void fn() {
 *     if(projectStage == ProjectStage.Production) {
 *        // do some production stuff...
 *     }
 *   }
 * }
 * </pre>
 *
 */
@ApplicationScoped
public class ProjectStageProducer implements Serializable
{
    protected static final Logger LOG = Logger.getLogger(ProjectStageProducer.class.getName());

    private static final long serialVersionUID = -2987762608635612074L;

    /**
     * The detected ProjectStage
     */
    private static ProjectStage projectStage;

    /**
     * for the singleton factory
     */
    private static volatile ProjectStageProducer projectStageProducer;

    /**
     * ProjectStageProducers must only be created by subclassing producers
     */
    protected ProjectStageProducer()
    {
    }

    /**
     * We can only produce @Dependent scopes since an enum is final.
     * @return current ProjectStage
     */
    @Produces
    @Dependent
    @Default
    public ProjectStage getProjectStage()
    {
        if (projectStage == null)
        {
            //triggers initialization
            getInstance();
        }
        return projectStage;
    }

    /**
     * <p>This factory method should only get used if there is absolutely no way
     * to get the current {@link ProjectStage} via &#064;Inject.</p>
     *
     * <p></p>
     *
     * @return the ProjectStageProducer instance.
     */
    public static ProjectStageProducer getInstance()
    {
        if (projectStageProducer == null)
        {
            lazyInit();
        }

        if (projectStage == null)
        {
            projectStageProducer.initProjectStage();
        }

        return projectStageProducer;
    }

    private static synchronized void lazyInit()
    {
        // switch into paranoia mode
        if (projectStageProducer != null)
        {
            return;
        }

        projectStageProducer = new ProjectStageProducer();

        projectStageProducer.initProjectStage();
    }

    /**
     * Resolves the project-stage configured for DeltaSpike
     * @return the resolved {@link ProjectStage} or <code>null</code> if none defined.
     */
    protected ProjectStage resolveProjectStage()
    {
        String stageName = ConfigResolver.getPropertyValue("org.apache.deltaspike.ProjectStage");

        if (stageName != null)
        {
            return ProjectStage.valueOf(stageName);
        }

        return null;
    }

    protected void initProjectStage()
    {
        // switch into paranoia mode
        synchronized (ProjectStageProducer.class)
        {
            if (projectStage == null)
            {
                projectStage = resolveProjectStage();

                if (projectStage == null)
                {
                    projectStage = ProjectStage.Production;
                }

                LOG.info("Computed the following DeltaSpike ProjectStage: " + projectStage);
            }
        }
    }

    /*
     * Methods which might be needed for unit tests
     */

    /**
     * Can be used esp. for internal tests.
     * Usage:
     * <pre>
     * new ProjectStageProducer() {
     *     &#064;Override
     *     protected void reset() { super.reset(); }
     * }.reset();
     * </pre>
     */
    protected void reset()
    {
        resetCache();
    }

    /**
     * This function exists to prevent findbugs to complain about
     * setting a static member from a non-static function.
     */
    private static void resetCache()
    {
        projectStage = null;
        projectStageProducer = null;
    }

    /**
     * This function can be used to manually set the ProjectStage for the application.
     * This is e.g. useful in unit tests.
     * @param ps the ProjectStage to set
     */
    public static void setProjectStage(ProjectStage ps)
    {
        projectStage = ps;
    }
}
