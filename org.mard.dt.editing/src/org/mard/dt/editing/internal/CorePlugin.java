package org.mard.dt.editing.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class CorePlugin extends Plugin {

	public static final String PLUGIN_ID = "org.mard.dt.editing"; //$NON-NLS-1$

	private static CorePlugin plugin;

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	public static CorePlugin getDefault() {
		return plugin;
	}

	public static void log(IStatus status) {
		plugin.getLog().log(status);
	}

	public static void logError(Throwable throwable) {
		log(createErrorStatus(throwable.getMessage(), throwable));
	}

	public static IStatus createErrorStatus(String message, Throwable throwable) {
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, throwable);
	}

	public static IStatus createWarningStatus(String message) {
		return new Status(IStatus.WARNING, PLUGIN_ID, 0, message, null);
	}

	public static IStatus createWarningStatus(final String message, Exception throwable) {
		return new Status(IStatus.WARNING, PLUGIN_ID, 0, message, throwable);
	}

	private Injector injector;

	@Override
	public void start(BundleContext bundleContext) throws Exception {

		super.start(bundleContext);
		CorePlugin.context = bundleContext;
		plugin = this;
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		plugin = null;

		super.stop(bundleContext);

		CorePlugin.context = null;
	}

	public synchronized Injector getInjector() {

		if (injector == null) {
			injector = createInjector();
		}
		return injector;
	}

	private Injector createInjector() {
		try {
			return Guice.createInjector(new ExternalDependenciesModule(this));
		} catch (Exception e) {

			String message = "Failed to create injector for " + getBundle().getSymbolicName(); //$NON-NLS-1$
			log(createErrorStatus(message, e));
			throw new RuntimeException(message, e);
		}
	}

}
