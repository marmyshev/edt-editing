/*******************************************************************************
 * Copyright (C) 2021, Dmitriy Marmyshev and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dmitriy Marmyshev - initial API and implementation
 *******************************************************************************/
package org.mard.dt.editing.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.mard.dt.editing.IPathEditingService;
import org.osgi.framework.BundleContext;

import com._1c.g5.wiring.InjectorAwareServiceRegistrator;
import com._1c.g5.wiring.ServiceInitialization;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * The Core plug-in activator. The activator class controls the plug-in life cycle.
 *
 * @author Dmitriy Marmyshev
 */
public class CorePlugin
    extends Plugin
{

    public static final String PLUGIN_ID = "org.mard.dt.editing"; //$NON-NLS-1$

    private static CorePlugin plugin;

    private static BundleContext context;

    static BundleContext getContext()
    {
        return context;
    }

    public static CorePlugin getDefault()
    {
        return plugin;
    }

    public static void log(IStatus status)
    {
        plugin.getLog().log(status);
    }

    public static void logError(Throwable throwable)
    {
        log(createErrorStatus(throwable.getMessage(), throwable));
    }

    public static IStatus createErrorStatus(String message, Throwable throwable)
    {
        return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, throwable);
    }

    public static IStatus createWarningStatus(String message)
    {
        return new Status(IStatus.WARNING, PLUGIN_ID, 0, message, null);
    }

    public static IStatus createWarningStatus(final String message, Exception throwable)
    {
        return new Status(IStatus.WARNING, PLUGIN_ID, 0, message, throwable);
    }

    private volatile Injector injector;

    private InjectorAwareServiceRegistrator registrator;

    @Override
    public void start(BundleContext bundleContext) throws Exception
    {

        super.start(bundleContext);
        CorePlugin.context = bundleContext;
        plugin = this;

        registrator = new InjectorAwareServiceRegistrator(bundleContext, this::getInjector);

        ServiceInitialization.schedule(() -> {
            // register services from injector
            try
            {
                registrator.managedService(IPathEditingService.class).activateBeforeRegistration().registerInjected();
            }
            catch (Exception e)
            {
                logError(e);
            }

        });
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception
    {
        registrator.deactivateManagedServices(this);
        registrator.unregisterServices();

        plugin = null;

        super.stop(bundleContext);

        CorePlugin.context = null;
    }

    /**
     * Returns Guice injector of the plug-in.
     *
     * @return Guice injector of the plug-in, never {@code null} if plug-in is started
     */
    /* package*/ Injector getInjector()
    {

        Injector localInstance = injector;
        if (localInstance == null)
        {
            synchronized (CorePlugin.class)
            {
                localInstance = injector;
                if (localInstance == null)
                {
                    injector = localInstance = createInjector();
                }
            }
        }
        return localInstance;
    }

    private Injector createInjector()
    {
        try
        {
            return Guice.createInjector(new ServiceModule(), new ExternalDependenciesModule(this));
        }
        catch (Exception e)
        {

            String message = "Failed to create injector for " + getBundle().getSymbolicName(); //$NON-NLS-1$
            log(createErrorStatus(message, e));
            throw new RuntimeException(message, e);
        }
    }

}
