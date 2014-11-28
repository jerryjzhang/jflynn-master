package com.tencent.jflynn.dto.scheduler;

import java.util.HashMap;
import java.util.Map;

public class ScheduleRequest {
	private String appName;
	private String imageUri;
	private Map<String,String> appEnv = new HashMap<String,String>();
	private Map<String,ExtendedProgram> programs = new HashMap<String,ExtendedProgram>();
	
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getImageUri() {
		return imageUri;
	}
	public void setImageUri(String imageUri) {
		this.imageUri = imageUri;
	}
	public Map<String, String> getAppEnv() {
		return appEnv;
	}
	public void setAppEnv(Map<String, String> appEnv) {
		this.appEnv = appEnv;
	}
	public Map<String, ExtendedProgram> getPrograms() {
		return programs;
	}
	public void setPrograms(Map<String, ExtendedProgram> programs) {
		this.programs = programs;
	}
}
