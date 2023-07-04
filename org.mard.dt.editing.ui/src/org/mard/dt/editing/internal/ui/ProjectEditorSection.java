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
package org.mard.dt.editing.internal.ui;

import java.io.InputStream;
import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.notifications.NotificationPopup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledPageBook;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.mard.dt.editing.IPathEditingService;

import com._1c.g5.v8.dt.ui.editor.IProjectEditor;
import com._1c.g5.v8.dt.ui.editor.IProjectEditorSection;

/**
 * The section for Project editor that allows to open or create new settings for disable editing.
 */
public class ProjectEditorSection
    implements IProjectEditorSection, IResourceChangeListener
{

    private static final String EXT_YML = ".yml"; //$NON-NLS-1$

    private static final String TEMPLATES = "/templates/"; //$NON-NLS-1$

    private static final Object NEW_SETTINGS_PAGE = "new-settings-page"; //$NON-NLS-1$

    private static final Object EXISTING_SETTINGS_PAGE = "existing-settings-page"; //$NON-NLS-1$

    private IProjectEditor editor;

    private IFile file;

    private ScrolledPageBook pageBook;

    @Override
    public void createContent(IProjectEditor editor, IFormPage page, FormToolkit toolkit, Composite parent,
        Section section)
    {
        this.editor = editor;
        file = editor.getProject().getFile(IPathEditingService.SETTING_FILE_PATH);

        pageBook = toolkit.createPageBook(parent, SWT.NONE);
        Composite newSettingsPage = pageBook.createPage(NEW_SETTINGS_PAGE);

        toolkit.createLabel(newSettingsPage, Messages.ProjectEditorSection_Project_has_no_settings_to_disable_editing);
        Button createSettingsButton =
            toolkit.createButton(newSettingsPage, Messages.ProjectEditorSection_Create_new_settings, SWT.PUSH);
        createSettingsButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::createSettings));
        createSettingsButton.setToolTipText(Messages.ProjectEditorSection_Create_new_blank_sttings_with_template);

        Button allObjectButton =
            toolkit.createButton(newSettingsPage, Messages.ProjectEditorSection_Disable_all_objects, SWT.NONE);
        allObjectButton.setToolTipText(Messages.ProjectEditorSection_Create_new_sttings_with_disabling_all_objects);
        allObjectButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::createAllSettings));

        Button withSubsystemsButton =
            toolkit.createButton(newSettingsPage, Messages.ProjectEditorSection_Disable_with_subsystems, SWT.NONE);
        withSubsystemsButton.setToolTipText(
            Messages.ProjectEditorSection_Create_new_sttings_with_disabling_objects_by_selected_subsystems);
        withSubsystemsButton
            .addSelectionListener(SelectionListener.widgetSelectedAdapter(this::createSubsystemSettings));

        Button vendorBranchButton =
            toolkit.createButton(newSettingsPage, Messages.ProjectEditorSection_Disable_by_vendor_Git_branch, SWT.NONE);
        vendorBranchButton.setToolTipText(
            Messages.ProjectEditorSection_Create_new_sttings_with_disabling_objects_by_Git_branch_vendor);
        vendorBranchButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::createVendorSettings));

        GridDataFactory.fillDefaults().grab(true, true).minSize(100, 200).applyTo(newSettingsPage);
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(newSettingsPage);

        Composite existingSettingsPage = pageBook.createPage(EXISTING_SETTINGS_PAGE);

        toolkit.createLabel(existingSettingsPage, Messages.ProjectEditorSection_Project_has_disable_editing_settings);
        Button openSettingsButton =
            toolkit.createButton(existingSettingsPage, Messages.ProjectEditorSection_Open_settings, SWT.PUSH);
        openSettingsButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::openSettings));

        GridDataFactory.fillDefaults().grab(true, true).minSize(100, 200).applyTo(existingSettingsPage);
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(existingSettingsPage);

        GridDataFactory.fillDefaults().grab(true, true).minSize(100, 200).applyTo(pageBook);

        showSettingsPage();

        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event)
    {
        IResourceDelta delta = event.getDelta();
        if (delta != null && file != null && !pageBook.isDisposed() && delta.findMember(file.getFullPath()) != null)
        {
            Display display = pageBook.getDisplay();
            if (!display.isDisposed())
            {
                display.asyncExec(this::showSettingsPage);
            }
        }
    }

    @Override
    public void dispose()
    {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        editor = null;
        file = null;
        pageBook = null;
    }

    @Override
    public void doSave(IProgressMonitor monitor) throws CoreException
    {
        // do nothing
    }

    @Override
    public void inputChanged(IProjectEditor editor)
    {
        this.editor = editor;
        file = editor.getProject().getFile(IPathEditingService.SETTING_FILE_PATH);

        showSettingsPage();
    }

    @Override
    public boolean isDirty()
    {
        return false;
    }

    @Override
    public IStatus validate()
    {
        return Status.OK_STATUS;
    }

    private void showSettingsPage()
    {
        if (pageBook == null)
        {
            return;
        }

        if (file != null && file.exists())
        {
            pageBook.showPage(EXISTING_SETTINGS_PAGE);
        }
        else
        {
            pageBook.showPage(NEW_SETTINGS_PAGE);
        }
    }

    private void openSettings(SelectionEvent event)
    {
        openFile(event.display);
    }

    private void createSettings(SelectionEvent event)
    {
        createSettings("empty", event.display); //$NON-NLS-1$
    }

    private void createAllSettings(SelectionEvent event)
    {
        createSettings("all", event.display); //$NON-NLS-1$
    }

    private void createSubsystemSettings(SelectionEvent event)
    {
        createSettings("subsystem", event.display); //$NON-NLS-1$
    }

    private void createVendorSettings(SelectionEvent event)
    {
        createSettings("vendor", event.display); //$NON-NLS-1$
    }

    private void createSettings(String template, Display display)
    {
        String templatePath = TEMPLATES + template + EXT_YML;

        Job.create(Messages.ProjectEditorSection_Create_disable_editing_settings_file, monitor -> {
            if (!file.isAccessible())
            {
                try
                {
                    InputStream in = getClass().getResourceAsStream(templatePath);
                    file.create(in, true, new NullProgressMonitor());
                    if (display.isDisposed())
                    {
                        return;
                    }
                    display.asyncExec(() -> NotificationPopup.forDisplay(display)
                        .fadeIn(true)
                        .title(Messages.ProjectEditorSection_Disable_settings_created, true)
                        .text(MessageFormat.format(
                            Messages.ProjectEditorSection_Settings_file__0__to_disable__objects_created,
                            file.getFullPath()))
                        .open());

                }
                catch (CoreException e)
                {
                    UiPlugin.logError(e);
                }
            }
            openFile(display);
        }).schedule();
    }

    private void openFile(Display display)
    {
        if (file == null || !file.isAccessible() || display.isDisposed())
        {
            return;
        }

        display.asyncExec(() -> {
            try
            {
                String editorId = IDE.getEditorDescriptor(file, true, true).getId();
                IEditorInput input = new FileEditorInput(file);
                editor.getEditorSite().getWorkbenchWindow().getActivePage().openEditor(input, editorId);
            }
            catch (PartInitException | OperationCanceledException e)
            {
                UiPlugin.logError(e);
            }
        });

    }

}
