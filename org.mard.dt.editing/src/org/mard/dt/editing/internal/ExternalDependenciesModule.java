package org.mard.dt.editing.internal;

import org.eclipse.core.runtime.Plugin;

import com._1c.g5.v8.dt.core.filesystem.IProjectFileSystemSupportProvider;
import com._1c.g5.v8.dt.core.platform.IBmModelManager;
import com._1c.g5.v8.dt.core.platform.IResourceLookup;
import com._1c.g5.wiring.AbstractServiceAwareModule;

public class ExternalDependenciesModule extends AbstractServiceAwareModule {

	public ExternalDependenciesModule(Plugin plugin) {
		super(plugin);
	}

	@Override
	protected void doConfigure() {
		bind(IResourceLookup.class).toService();
		bind(IProjectFileSystemSupportProvider.class).toService();
		bind(IBmModelManager.class).toService();

	}

}
