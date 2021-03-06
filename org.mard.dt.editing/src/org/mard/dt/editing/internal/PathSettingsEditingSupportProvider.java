package org.mard.dt.editing.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.ecore.EObject;
import org.mard.dt.editing.IPathEditingService;

import com._1c.g5.v8.bm.core.EngineState;
import com._1c.g5.v8.bm.core.IBmEngine;
import com._1c.g5.v8.bm.core.IBmObject;
import com._1c.g5.v8.dt.core.model.EditingMode;
import com._1c.g5.v8.dt.core.model.IModelEditingSupportProvider;
import com._1c.g5.v8.dt.core.platform.IResourceLookup;
import com.google.inject.Inject;

public class PathSettingsEditingSupportProvider
    implements IModelEditingSupportProvider
{

    @Inject
    private IResourceLookup resourceLookup;

    @Inject
    private IPathEditingService editingService;

    @Override
    public boolean canDelete(EObject object, EditingMode mode)
    {
        return true;
    }

    @Override
    public boolean canEdit(EObject eObject, EditingMode mode)
    {

        if (mode != EditingMode.DIRECT)
            return true;

        if (eObject instanceof IBmObject)
        {
            IBmEngine engine = ((IBmObject)eObject).bmGetEngine();
            if (engine != null && engine.getState() != EngineState.RUNNING)
            {
                return true;
            }
        }

        IProject project = resourceLookup.getProject(eObject);
        if(project == null)
            return true;

        return editingService.canEdit(project, eObject);
    }

}
