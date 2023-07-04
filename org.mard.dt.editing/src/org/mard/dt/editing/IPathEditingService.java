/*******************************************************************************
 * Copyright (C) 2021, 2023, Dmitriy Marmyshev and others.
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
package org.mard.dt.editing;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.EObject;

import com._1c.g5.wiring.IManagedService;

/**
 * The service allows to check if some object with {@link IPath path} or {@link EObject EMF object} with path can edit
 * in the project.
 *
 * @author Dmitriy Marmyshev
 */
public interface IPathEditingService
    extends IManagedService
{

    static final IPath SETTING_FILE_PATH = new Path(".settings/editing.yml"); //$NON-NLS-1$

    /**
     * Can edit the eObject.
     *
     * @param project the project, cannot be {@code null}.
     * @param eObject the EMF object, cannot be {@code null}.
     * @return true, if can edit
     */
    boolean canEdit(IProject project, EObject eObject);

    /**
     * Can edit the object by it's path.
     *
     * @param project the project, cannot be {@code null}.
     * @param path the path to check, cannot be {@code null}.
     * @return true, if can edit
     */
    boolean canEdit(IProject project, IPath path);

    /**
     * Can edit the object by compare and merge system.
     *
     * @param project the project, cannot be {@code null}.
     * @return true, if can edit
     */
    boolean canEditInMerge(IProject project);

}
