package com.tencent.jflynn.domain;

import java.util.HashMap;
import java.util.Map;

public class Formation {
	private String appID;
	private String releaseID;
	private Map<String,Integer> programReplica = new HashMap<String,Integer>();

	public String getAppID() {
		return appID;
	}
	public void setAppID(String appID) {
		this.appID = appID;
	}
	public String getReleaseID() {
		return releaseID;
	}
	public void setReleaseID(String releaseID) {
		this.releaseID = releaseID;
	}
	public Map<String, Integer> getProgramReplica() {
		return programReplica;
	}
	public void setProgramReplica(Map<String, Integer> programReplica) {
		this.programReplica = programReplica;
	}
}
