/**
 *
 */
package org.mard.dt.editing;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
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

}
