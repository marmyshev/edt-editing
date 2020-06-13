package org.mard.dt.editing;

import java.util.List;

public class EditingSettingsContent {

	private List<String> subsystem;

	private List<String> path;

	private List<String> fullname;

	public List<String> getSubsystem() {
		return subsystem;
	}

	public void setSubsystem(List<String> subsystem) {
		this.subsystem = subsystem;
	}

	public List<String> getPath() {
		return path;
	}

	public void setPath(List<String> path) {
		this.path = path;
	}

	public List<String> getFullname() {
		return fullname;
	}

	public void setFullname(List<String> fullname) {
		this.fullname = fullname;
	}

}
