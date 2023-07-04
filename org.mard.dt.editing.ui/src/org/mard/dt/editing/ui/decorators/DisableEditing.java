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
package org.mard.dt.editing.ui.decorators;

import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.mard.dt.editing.IPathEditingService;
import org.mard.dt.editing.internal.ui.UiPlugin;

import com._1c.g5.v8.dt.core.platform.IResourceLookup;
import com.google.inject.Inject;

/**
 * This decorator shows overlay icon for objects that are instances of {@link IResource} or instances of {@link EObject}
 * and editing is desabled.
 *
 * @author Dmitriy Marmyshev
 *
 * @see ILightweightLabelDecorator
 */
public class DisableEditing
    extends LabelProvider
    implements ILightweightLabelDecorator
{
    @Inject
    private IPathEditingService editingService;

    @Inject
    private IResourceLookup resourceLookup;

    /** The icon image location in the project folder */
    private String iconPath = "icons/ovr16/disable_editing.png"; //NON-NLS-1 //$NON-NLS-1$

    /**
     * The image description used in
     * <code>addOverlay(ImageDescriptor, int)</code>
     */
    private ImageDescriptor descriptor;

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object, org.eclipse.jface.viewers.IDecoration)
     */
    @Override
    public void decorate(Object element, IDecoration decoration)
    {
        boolean canEdit = true;
        if (element instanceof IResource)
        {
            IResource resource = (IResource)element;
            IProject project = resource.getProject();
            if (project != null)
                canEdit = editingService.canEdit(project, resource.getProjectRelativePath());
        }
        else if (element instanceof EObject)
        {
            EObject eObject = (EObject)element;
            IProject project = resourceLookup.getProject(eObject);

            if (project != null)
                canEdit = editingService.canEdit(project, eObject);
        }

        if (!canEdit && getDescriptor() != null)
        {
            decoration.addOverlay(getDescriptor(), IDecoration.TOP_LEFT);
        }
    }

    private ImageDescriptor getDescriptor()
    {
        if (descriptor == null)
        {
            URL url = FileLocator.find(Platform.getBundle(UiPlugin.PLUGIN_ID), new Path(iconPath), null); //NON-NLS-1

            if (url == null)
                return null;
            descriptor = ImageDescriptor.createFromURL(url);
        }
        return descriptor;
    }

}
