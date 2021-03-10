package org.mard.dt.editing;

import static com._1c.g5.v8.dt.metadata.mdclass.MdClassPackage.Literals.SUBSYSTEM;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.mard.dt.editing.internal.CorePlugin;

import com._1c.g5.v8.bm.core.IBmObject;
import com._1c.g5.v8.bm.core.IBmTransaction;
import com._1c.g5.v8.bm.integration.AbstractBmTask;
import com._1c.g5.v8.bm.integration.IBmModel;
import com._1c.g5.v8.dt.core.filesystem.IProjectFileSystemSupport;
import com._1c.g5.v8.dt.core.filesystem.IProjectFileSystemSupportProvider;
import com._1c.g5.v8.dt.core.platform.IBmModelManager;
import com._1c.g5.v8.dt.metadata.mdclass.MdObject;
import com._1c.g5.v8.dt.metadata.mdclass.Subsystem;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ProjectPathEditingService
{

    private static final int TOP_OBJECT_FIRST_SEGMENTS = 3;

    private static final IPath SETTING_FILE_PATH = new Path(".settings/editing.yml"); //$NON-NLS-1$

    /**
     * Gets the singleton shared instance of {@link ProjectPathEditingService}
     *
     * @return single instance of ProjectPathEditingService
     */
    public static ProjectPathEditingService getInstance()
    {
        return CorePlugin.getDefault().getInjector().getInstance(ProjectPathEditingService.class);
    }

    private final IProjectFileSystemSupportProvider fileSystemSupportProvider;

    private final IBmModelManager modelManager;

    private final Map<IProject, ProjectCache> projects = new ConcurrentHashMap<>();

    @Inject
    public ProjectPathEditingService(IProjectFileSystemSupportProvider fileSystemSupportProvider,
        IBmModelManager modelManager)
    {
        this.fileSystemSupportProvider = fileSystemSupportProvider;
        this.modelManager = modelManager;
    }

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

    public boolean canEdit(IProject project, IPath path)
    {

        if (project == null)
            return true;

        ProjectCache cache = projects.computeIfAbsent(project, this::createProjectCache);

        return canEdit(cache, path);
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

    }

    private void addFullnamePath(Set<IPath> paths, List<String> fullnames, IBmModel model,
        IProjectFileSystemSupport fileSystemSupport)
    {

        model.executeReadonlyTask(new AbstractBmTask<Void>("Read paths by fullname") //$NON-NLS-1$
        {

            @Override
            public Void execute(IBmTransaction transaction, IProgressMonitor monitor)
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
                return null;
            }
        });
    }

    private void addSubsystemPath(Set<IPath> paths, List<String> subsystems, IBmModel model,
        IProjectFileSystemSupport fileSystemSupport)
    {

        model.executeReadonlyTask(new AbstractBmTask<Void>("Read subsystem content paths") //$NON-NLS-1$
        {

            @Override
            public Void execute(IBmTransaction transaction, IProgressMonitor monitor)
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
                return null;
            }
        });
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
        if (settings != null)
        {

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
        }
        return new ProjectCache(disable, enable);
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

        public ProjectCache(Set<IPath> disable, Set<IPath> enable)
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

    }
}
