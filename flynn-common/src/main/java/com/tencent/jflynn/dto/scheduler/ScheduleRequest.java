package com.tencent.jflynn.dto.scheduler;

import java.util.HashMap;
import java.util.Map;

import com.tencent.jflynn.domain.Program;

public class ScheduleRequest {
	private String appName;
	private String imageUri;
	private Map<String,String> appEnv = new HashMap<String,String>();
	private Map<String,Program> programs = new HashMap<String,Program>();
	private Map<String,Integer> programReplica = new HashMap<String,Integer>();
	
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
	public Map<String, Program> getPrograms() {
		return programs;
	}
	public void setPrograms(Map<String, Program> programs) {
		this.programs = programs;
	}
	public Map<String, Integer> getProgramReplica() {
		return programReplica;
	}
	public void setProgramReplica(Map<String, Integer> programReplica) {
		this.programReplica = programReplica;
	}
}
