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
package org.mard.dt.editing.internal;

import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.SUBSYSTEM;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.mard.dt.editing.EditingSettings;
import org.mard.dt.editing.EditingSettingsContent;
import org.mard.dt.editing.EditingSettingsYamlReader;
import org.mard.dt.editing.IPathEditingService;

import com._1c.g5.v8.bm.core.IBmObject;
import com._1c.g5.v8.bm.core.IBmTransaction;
import com._1c.g5.v8.bm.integration.AbstractBmTask;
import com._1c.g5.v8.bm.integration.IBmModel;
import com._1c.g5.v8.dt.common.git.GitUtils;
import com._1c.g5.v8.dt.core.filesystem.IProjectFileSystemSupport;
import com._1c.g5.v8.dt.core.filesystem.IProjectFileSystemSupportProvider;
import com._1c.g5.v8.dt.core.lifecycle.ProjectContext;
import com._1c.g5.v8.dt.core.platform.IBmModelManager;
import com._1c.g5.v8.dt.core.platform.IDtProject;
import com._1c.g5.v8.dt.lifecycle.LifecycleParticipant;
import com._1c.g5.v8.dt.lifecycle.LifecyclePhase;
import com._1c.g5.v8.dt.lifecycle.LifecycleService;
import com._1c.g5.v8.dt.metadata.mdclass.MdObject;
import com._1c.g5.v8.dt.metadata.mdclass.Subsystem;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@LifecycleService(name = "ProjectPathEditingService")
@Singleton
public class ProjectPathEditingService
    implements IPathEditingService
{

    private static final int TOP_OBJECT_FIRST_SEGMENTS = 3;

    private static final IPath SUBSYSTEM_PATH = new Path("src/Subsystems"); //$NON-NLS-1$

    private final IProjectFileSystemSupportProvider fileSystemSupportProvider;

    private final IBmModelManager modelManager;

    private final Map<IProject, ProjectCache> projects = new ConcurrentHashMap<>();

    private final IResourceChangeListener resourceListener = new ResourceChangeListener();

    @Inject
    public ProjectPathEditingService(IProjectFileSystemSupportProvider fileSystemSupportProvider,
        IBmModelManager modelManager)
    {
        this.fileSystemSupportProvider = fileSystemSupportProvider;
        this.modelManager = modelManager;
    }

    @Override
    public void activate()
    {
        ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener);
    }

    @Override
    public void deactivate()
    {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
    }

    /**
     * Initializes the extension.
     *
     * @param projectContext The project context that is being started
     */
    @LifecycleParticipant(phase = LifecyclePhase.RESOURCE_LOADING, dependsOn = IBmModelManager.SERVICE_NAME)
    public void init(ProjectContext projectContext)
    {
        IDtProject dtProject = projectContext.getProject();
        IProject project = dtProject.getWorkspaceProject();

        if (project != null)
        {
            dispose(project);
        }
    }

    @LifecycleParticipant(phase = LifecyclePhase.DISPOSING)
    public void dispose(ProjectContext lifecycleContext)
    {
        IDtProject dtProject = lifecycleContext.getProject();
        IProject project = dtProject.getWorkspaceProject();

        if (project != null)
        {
            dispose(project);
        }
    }

    private void dispose(IProject project)
    {
        ProjectCache cache = projects.remove(project);
        if (cache != null)
        {
            cache.uriCache.cleanUp();
        }
    }

    @Override
    public boolean canEdit(IProject project, EObject eObject)
    {

        if (project == null || eObject == null || eObject.eIsProxy())
            return true;
        URI uri = EcoreUtil.getURI(eObject);
        if (uri == null)
            return true;

        String resourceUri = uri.trimFragment().toString();
        if (resourceUri == null)
            return true;

        ProjectCache cache = projects.computeIfAbsent(project, this::createProjectCache);

        try
        {
            return cache.getUriCache().get(resourceUri, () -> {
                IProjectFileSystemSupport fileSystemSupport =
                    fileSystemSupportProvider.getProjectFileSystemSupport(project);

                if (fileSystemSupport != null)
                {

                    IPath path = fileSystemSupport.getPath(eObject);
                    if (path != null)
                        return canEdit(cache, path);
                }
                return true;
            });
        }
        catch (ExecutionException e)
        {
            CorePlugin.logError(e);
        }
        return true;
    }

    @Override
    public boolean canEdit(IProject project, IPath path)
    {

        if (project == null)
            return true;

        ProjectCache cache = projects.computeIfAbsent(project, this::createProjectCache);

        return canEdit(cache, path);
    }

    @Override
    public boolean canEditInMerge(IProject project)
    {
        if (project == null)
            return true;

        ProjectCache cache = projects.computeIfAbsent(project, this::createProjectCache);

        return cache.canEditInMerge();
    }

    private void addAllPath(IProject project, EditingSettingsContent content, Set<IPath> paths)
    {

        if (content.getPath() != null && !content.getPath().isEmpty())
        {
            for (String line : content.getPath())
            {
                if (Path.isValidPosixPath(line))
                    paths.add(new Path(line));
            }
        }

        if (content.getSubsystem() != null && !content.getSubsystem().isEmpty())
        {

            IProjectFileSystemSupport fileSystemSupport =
                fileSystemSupportProvider.getProjectFileSystemSupport(project);

            IBmModel model = modelManager.getModel(project);

            if (fileSystemSupport != null && model != null)
            {
                addSubsystemPath(paths, content.getSubsystem(), model, fileSystemSupport);
            }
        }

        if (content.getFullname() != null && !content.getFullname().isEmpty())
        {
            IProjectFileSystemSupport fileSystemSupport =
                fileSystemSupportProvider.getProjectFileSystemSupport(project);

            IBmModel model = modelManager.getModel(project);

            if (fileSystemSupport != null && model != null)
            {
                addFullnamePath(paths, content.getFullname(), model, fileSystemSupport);
            }
        }

        if (content.getBranch() != null && !content.getBranch().isEmpty())
        {
            addBranches(paths, content.getBranch(), project);
        }
    }

    private void addBranches(Set<IPath> paths, List<String> branches, IProject project)
    {
        final Repository repository = GitUtils.getGitRepository(project);
        if (repository == null)
        {
            return;
        }

        IPath projectPath = project.getLocation();
        IPath srcPath = projectPath.append("src"); //$NON-NLS-1$
        IPath gitPath = new Path(repository.getWorkTree().getAbsolutePath());
        IPath configurationPath = srcPath.append("Configuration"); //$NON-NLS-1$

        try (final Git git = new Git(repository);
            final RevWalk rw = new RevWalk(repository);
            final TreeWalk tw = new TreeWalk(repository);)
        {
            List<Ref> remoteBranches = git.branchList().setListMode(ListMode.REMOTE).call();

            for (String shortBranchName : branches)
            {
                for (Ref remoteBranch : remoteBranches)
                {
                    if (shortBranchName.equals(repository.shortenRemoteBranchName(remoteBranch.getName())))
                    {
                        final Ref branchRef = repository.exactRef(remoteBranch.getName());
                        if (branchRef != null)
                        {
                            final RevCommit branchCommit = rw.parseCommit(branchRef.getObjectId());
                            tw.addTree(branchCommit.getTree());
                        }
                    }
                }
            }

            if (tw.getTreeCount() != 0)
            {
                tw.setRecursive(true);

                while (tw.next())
                {
                    IPath gitFilePath = gitPath.append(tw.getPathString());

                    if (srcPath.isPrefixOf(gitFilePath) && !configurationPath.isPrefixOf(gitFilePath)
                        && !gitFilePath.toFile().isDirectory())
                    {
                        IPath relativePath = gitFilePath.makeRelativeTo(projectPath);
                        paths.add(relativePath);
                    }
                }
            }
        }
        catch (IllegalStateException | IOException | GitAPIException e)
        {
            CorePlugin.logError(e);
        }
    }


    private void addFullnamePath(Set<IPath> paths, List<String> fullnames, IBmModel model,
        IProjectFileSystemSupport fileSystemSupport)
    {

        IBmTransaction transaction = model.getEngine().getCurrentTransaction();
        if (transaction != null)
        {
            addFullnamePath(paths, fullnames, fileSystemSupport, transaction);
        }
        else
        {
            model.executeReadonlyTask(new AbstractBmTask<Void>("Read paths by fullname") //$NON-NLS-1$
            {
                @Override
                public Void execute(IBmTransaction transaction, IProgressMonitor monitor)
                {

                    addFullnamePath(paths, fullnames, fileSystemSupport, transaction);
                    return null;
                }
            });
        }
    }

    private void addFullnamePath(Set<IPath> paths, List<String> fullnames, IProjectFileSystemSupport fileSystemSupport,
        IBmTransaction transaction)
    {
        for (String fullname : fullnames)
        {

            IBmObject top = transaction.getTopObjectByFqn(fullname);
            if (top != null)
            {
                IPath path = fileSystemSupport.getPath(top);
                if (path != null)
                {
                    paths.add(path);
                }
            }
        }
    }

    private void addSubsystemPath(Set<IPath> paths, List<String> subsystems, IBmModel model,
        IProjectFileSystemSupport fileSystemSupport)
    {

        IBmTransaction transaction = model.getEngine().getCurrentTransaction();
        if (transaction != null)
        {
            addSubsystemPath(paths, subsystems, fileSystemSupport, transaction);
        }
        else
        {
            model.executeReadonlyTask(new AbstractBmTask<Void>("Read subsystem content paths") //$NON-NLS-1$
            {
                @Override
                public Void execute(IBmTransaction transaction, IProgressMonitor monitor)
                {
                    addSubsystemPath(paths, subsystems, fileSystemSupport, transaction);
                    return null;
                }
            });
        }
    }

    private void addSubsystemPath(Set<IPath> paths, List<String> subsystems,
        IProjectFileSystemSupport fileSystemSupport, IBmTransaction transaction)
    {
        String preffix = SUBSYSTEM.getName();
        for (String subsystemName : subsystems)
        {
            StringBuilder sb = new StringBuilder();
            String[] segments = subsystemName.trim().split("\\."); //$NON-NLS-1$
            for (int i = 0; i < segments.length; i++)
            {
                String segment = segments[i];
                sb.append(preffix);
                sb.append("."); //$NON-NLS-1$
                sb.append(segment);
            }

            IBmObject subsystem = transaction.getTopObjectByFqn(sb.toString());
            if (subsystem instanceof Subsystem)
            {
                addSubsystemPath((Subsystem)subsystem, paths, fileSystemSupport);
            }
        }
    }

    private void addSubsystemPath(Subsystem subsystem, Set<IPath> paths, IProjectFileSystemSupport fileSystemSupport)
    {

        for (MdObject item : subsystem.getContent())
        {
            IPath path = fileSystemSupport.getPath(item);
            if (path != null && path.segmentCount() > TOP_OBJECT_FIRST_SEGMENTS)
            {
                int remove = path.segmentCount() - TOP_OBJECT_FIRST_SEGMENTS;
                paths.add(path.removeLastSegments(remove));
            }
        }

        for (Subsystem item : subsystem.getSubsystems())
        {
            addSubsystemPath(item, paths, fileSystemSupport);
        }

    }

    private boolean canEdit(ProjectCache cache, IPath path)
    {

        if (cache.getDisable().isEmpty())
            return true;

        if (!cache.getEnable().isEmpty()
            && (cache.getEnable().contains(path) || cache.getEnable().stream().anyMatch(p -> p.isPrefixOf(path))))
            return true;

        if (cache.getDisable().contains(path) || cache.getDisable().stream().anyMatch(p -> p.isPrefixOf(path)))
            return false;

        return true;
    }

    private ProjectCache createProjectCache(IProject project)
    {

        Comparator<IPath> comparator = (o1, o2) -> {
            if (o1.equals(o2))
                return 0;
            for (int i = 0; i < o1.segmentCount() && i < o2.segmentCount(); i++)
            {
                String p1 = o1.segments()[i];
                String p2 = o2.segments()[i];
                int compare = p1.compareTo(p2);
                if (compare != 0)
                    return compare;
            }
            return 1;
        };

        Set<IPath> disable = new ConcurrentSkipListSet<>(comparator);
        Set<IPath> enable = new ConcurrentSkipListSet<>(comparator);

        EditingSettings settings = loadEditingSettings(project);
        if (settings == null)
        {
            return new ProjectCache(disable, enable, true);
        }

        if (settings.getDisable() != null)
            addAllPath(project, settings.getDisable(), disable);

        if (!disable.isEmpty() && settings.getEnable() != null)
            addAllPath(project, settings.getEnable(), enable);

        // Remove disable path if there is higher enable (permissive) path
        for (IPath path : enable)
        {
            for (Iterator<IPath> iterator = disable.iterator(); iterator.hasNext();)
            {
                IPath iPath = iterator.next();
                if (path.isPrefixOf(iPath))
                    iterator.remove();
            }
        }

        boolean canEditInMerge = settings.getEnable() != null && settings.getEnable().isMerge();
        return new ProjectCache(disable, enable, canEditInMerge);
    }

    private EditingSettings loadEditingSettings(IProject project)
    {

        IFile file = project.getFile(SETTING_FILE_PATH);
        return EditingSettingsYamlReader.readOrNull(file);
    }

    private class ProjectCache
    {

        private final Cache<String, Boolean> uriCache =
            CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();

        private final Set<IPath> disable;

        private final Set<IPath> enable;

        private final boolean canEditInMerge;

        public ProjectCache(Set<IPath> disable, Set<IPath> enable, boolean canEditInMerge)
        {
            if (disable == null || disable.isEmpty())
            {
                this.disable = Set.of();
            }
            else
            {
                this.disable = Set.copyOf(disable);
            }

            if (enable == null || enable.isEmpty() || this.getDisable().isEmpty())
            {
                this.enable = Set.of();
            }
            else
            {
                this.enable = Set.copyOf(enable);
            }
            this.canEditInMerge = canEditInMerge;

        }

        public Set<IPath> getDisable()
        {
            return disable;
        }

        public Set<IPath> getEnable()
        {
            return enable;
        }

        public Cache<String, Boolean> getUriCache()
        {
            return uriCache;
        }

        public boolean canEditInMerge()
        {
            return canEditInMerge;
        }

    }

    private class ResourceChangeListener
        implements IResourceChangeListener
    {

        @Override
        public void resourceChanged(IResourceChangeEvent event)
        {
            IResource res = event.getResource();

            if (event.getType() == IResourceChangeEvent.PRE_CLOSE || event.getType() == IResourceChangeEvent.PRE_DELETE)
            {
                if (res instanceof IProject)
                {
                    dispose((IProject)res);
                }
            }
            else if (event.getType() == IResourceChangeEvent.POST_CHANGE)
            {
                IResourceDelta delta = event.getDelta();
                if (delta != null)
                {
                    for (Iterator<Entry<IProject, ProjectCache>> iterator = projects.entrySet().iterator(); iterator
                        .hasNext();)
                    {
                        Entry<IProject, ProjectCache> entry = iterator.next();
                        IPath path = entry.getKey().getFullPath();
                        IResourceDelta projectDelta = delta.findMember(path.append(SETTING_FILE_PATH));
                        if (projectDelta != null
                            || (projectDelta = delta.findMember(path.append(SUBSYSTEM_PATH))) != null)
                        {
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }

}
