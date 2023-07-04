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
package org.mard.dt.editing;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.mard.dt.editing.internal.CorePlugin;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.BaseConstructor;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

public class EditingSettingsYamlReader {

	public static EditingSettings readOrNull(IFile file) {

		if (file.isAccessible()) {

			try {
				return read(file);
			} catch (Exception e) {
				CorePlugin.logError(e);
			}
		}

		return null;
	}

	public static EditingSettings read(IFile file) throws CoreException {
		return read(file.getContents());
	}

	public static EditingSettings read(InputStream input) {

		BaseConstructor ctor = new CustomClassLoaderConstructor(EditingSettings.class.getClassLoader());
		ctor.getPropertyUtils().setBeanAccess(BeanAccess.PROPERTY);
		ctor.getPropertyUtils().setSkipMissingProperties(true);

		Yaml yaml = new Yaml(ctor);

		return yaml.loadAs(input, EditingSettings.class);
	}

	private EditingSettingsYamlReader() {
		throw new IllegalAccessError();
	}
}
