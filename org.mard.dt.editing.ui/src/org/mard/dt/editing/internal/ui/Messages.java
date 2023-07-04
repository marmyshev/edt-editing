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

import org.eclipse.osgi.util.NLS;

/**
 * @author Dmitriy Marmyshev
 *
 */
final class Messages
    extends NLS
{
    private static final String BUNDLE_NAME = Messages.class.getPackageName() + ".messages"; //$NON-NLS-1$
    public static String ProjectEditorSection_Create_disable_editing_settings_file;
    public static String ProjectEditorSection_Create_new_blank_sttings_with_template;
    public static String ProjectEditorSection_Create_new_settings;
    public static String ProjectEditorSection_Create_new_sttings_with_disabling_all_objects;
    public static String ProjectEditorSection_Create_new_sttings_with_disabling_objects_by_Git_branch_vendor;
    public static String ProjectEditorSection_Create_new_sttings_with_disabling_objects_by_selected_subsystems;
    public static String ProjectEditorSection_Disable_all_objects;
    public static String ProjectEditorSection_Disable_by_vendor_Git_branch;
    public static String ProjectEditorSection_Disable_settings_created;
    public static String ProjectEditorSection_Disable_with_subsystems;
    public static String ProjectEditorSection_Open_settings;
    public static String ProjectEditorSection_Project_has_disable_editing_settings;
    public static String ProjectEditorSection_Project_has_no_settings_to_disable_editing;
    public static String ProjectEditorSection_Settings_file__0__to_disable__objects_created;
    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
