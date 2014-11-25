package com.tencent.jflynn.dto;

import java.util.Map;

public class DeployRequest {
	private String svnURL;
	private String dockerImage;
	private Map<String,String> releaseEnv;
	private Map<String,String> processCmd;
	private Map<String,String> processEpt;
	private Map<String,Map<String,String>> processEnv;
	
	private String comment;
	
	public String getSvnURL() {
		return svnURL;
	}
	public void setSvnURL(String svnURL) {
		this.svnURL = svnURL;
	}
	public String getDockerImage() {
		return dockerImage;
	}
	public void setDockerImage(String dockerImage) {
		this.dockerImage = dockerImage;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public Map<String, String> getReleaseEnv() {
		return releaseEnv;
	}
	public void setReleaseEnv(Map<String, String> releaseEnv) {
		this.releaseEnv = releaseEnv;
	}
	public Map<String, String> getProcessCmd() {
		return processCmd;
	}
	public void setProcessCmd(Map<String, String> processCmd) {
		this.processCmd = processCmd;
	}
	public Map<String, String> getProcessEpt() {
		return processEpt;
	}
	public void setProcessEpt(Map<String, String> processEpt) {
		this.processEpt = processEpt;
	}
	public Map<String, Map<String, String>> getProcessEnv() {
		return processEnv;
	}
	public void setProcessEnv(Map<String, Map<String, String>> processEnv) {
		this.processEnv = processEnv;
	}
}
