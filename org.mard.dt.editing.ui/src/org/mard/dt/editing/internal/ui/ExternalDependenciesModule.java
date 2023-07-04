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
package org.mard.dt.editing.internal.ui;

import org.eclipse.core.runtime.Plugin;
import org.mard.dt.editing.IPathEditingService;

import com._1c.g5.v8.dt.core.platform.IResourceLookup;
import com._1c.g5.wiring.AbstractServiceAwareModule;

public class ExternalDependenciesModule
    extends AbstractServiceAwareModule
{

    public ExternalDependenciesModule(Plugin plugin)
    {
        super(plugin);
    }

    @Override
    protected void doConfigure()
    {
        bind(IResourceLookup.class).toService();
        bind(IPathEditingService.class).toService();
    }

}
