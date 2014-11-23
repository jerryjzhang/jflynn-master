package com.tencent.jflynn.domain;

import java.util.HashMap;
import java.util.Map;

public class Release {
	private String id;
	private String artifactID;
	private String appID;
	private Map<String,String> env = new HashMap<String,String>();
	private Map<String,ProcessType> processes = new HashMap<String,ProcessType>();
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getArtifactID() {
		return artifactID;
	}
	public void setArtifactID(String artifactID) {
		this.artifactID = artifactID;
	}
	public String getAppID() {
		return appID;
	}
	public void setAppID(String appID) {
		this.appID = appID;
	}
	public Map<String, String> getEnv() {
		return env;
	}
	public void setEnv(Map<String, String> env) {
		this.env = env;
	}
	public Map<String, ProcessType> getProcesses() {
		return processes;
	}
	public void setProcesses(Map<String, ProcessType> processes) {
		this.processes = processes;
	}
}
