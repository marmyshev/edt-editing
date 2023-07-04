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

public class EditingSettings {

	private EditingSettingsContent disable;

	private EditingSettingsContent enable;

	public EditingSettingsContent getDisable() {
		return disable;
	}

	public void setDisable(EditingSettingsContent disable) {
		this.disable = disable;
	}

	public EditingSettingsContent getEnable() {
		return enable;
	}

	public void setEnable(EditingSettingsContent enable) {
		this.enable = enable;
	}
}
