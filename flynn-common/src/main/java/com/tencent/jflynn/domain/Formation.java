package com.tencent.jflynn.domain;

import java.util.HashMap;
import java.util.Map;

public class Formation {
	private String releaseID;
	private Map<String,Integer> processes = new HashMap<String,Integer>();
	public String getReleaseID() {
		return releaseID;
	}
	public void setReleaseID(String releaseID) {
		this.releaseID = releaseID;
	}
	public Map<String, Integer> getProcesses() {
		return processes;
	}
	public void setProcesses(Map<String, Integer> processes) {
		this.processes = processes;
	}
}
