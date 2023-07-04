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

import org.mard.dt.editing.IPathEditingService;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * @author Dmitriy Marmyshev
 *
 */
public class ServiceModule
    extends AbstractModule
{

    @Override
    protected void configure()
    {
        bind(IPathEditingService.class).to(ProjectPathEditingService.class).in(Singleton.class);
    }

}
