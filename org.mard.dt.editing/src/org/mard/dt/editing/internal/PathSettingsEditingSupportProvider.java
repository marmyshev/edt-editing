package org.mard.dt.editing.internal;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.ecore.EObject;
import org.mard.dt.editing.IPathEditingService;

import com._1c.g5.v8.bm.core.IBmEngine;
import com._1c.g5.v8.bm.core.IBmObject;
import com._1c.g5.v8.dt.core.model.EditingMode;
import com._1c.g5.v8.dt.core.model.IModelEditingSupportProvider;
import com._1c.g5.v8.dt.core.platform.IResourceLookup;
import com.google.inject.Inject;

public class PathSettingsEditingSupportProvider
    implements IModelEditingSupportProvider
{

    private static final Set<String> IGNORE_STACK_CALLER_CLASS =
        Set.of("com._1c.g5.v8.dt.internal.md.compare.participant.MdObjectComparisonParticipant"); //$NON-NLS-1$

    private final IResourceLookup resourceLookup;

    private final IPathEditingService editingService;

    @Inject
    public PathSettingsEditingSupportProvider(IResourceLookup resourceLookup, IPathEditingService editingService)
    {
        this.resourceLookup = resourceLookup;
        this.editingService = editingService;
    }

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
            if (engine != null && !engine.isActive())
            {
                return true;
            }
        }

        IProject project = resourceLookup.getProject(eObject);
        if (project == null)
            return true;

        boolean canEdit = editingService.canEdit(project, eObject);
        if (!canEdit && editingService.canEditInMerge(project) && isCallFromMerge())
        {
            return true;
        }
        return canEdit;
    }

    private boolean isCallFromMerge()
    {
        StackWalker walker = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);
        //@formatter:off
        Optional<String> callerClass = walker.walk(s ->
            s.limit(10)
            .map(StackFrame::getClassName)
            .filter(IGNORE_STACK_CALLER_CLASS::contains)
            .findFirst());
        //@formatter:on

        return callerClass.isPresent();
    }
}
