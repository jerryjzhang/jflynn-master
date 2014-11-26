package com.tencent.jflynn.domain;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class Release {
	private String id;
	private String artifactID;
	private String appID;
	private int version;
	private String tag;
	private Timestamp createTime;
	private Map<String,String> appEnv = new HashMap<String,String>();
	private Map<String,Program> programs = new HashMap<String,Program>();
	
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
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	public Map<String, String> getAppEnv() {
		return appEnv;
	}
	public void setAppEnv(Map<String, String> env) {
		this.appEnv = env;
	}
	public Map<String, Program> getPrograms() {
		return programs;
	}
	public void setPrograms(Map<String, Program> processes) {
		this.programs = processes;
	}
	
	@Override
	public String toString() {
		return "Release [id=" + id + ", artifactID=" + artifactID + ", appID="
				+ appID + ", version=" + version + ", tag=" + tag + ", env="
				+ appEnv + ", processes=" + programs + "]";
	}
}
