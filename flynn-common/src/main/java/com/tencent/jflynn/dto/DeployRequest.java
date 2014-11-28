package com.tencent.jflynn.dto;

import java.util.Map;

public class DeployRequest {
	private String svnURL;
	private String imageURI;
	private Map<String,String> appEnv;
	private Map<String,String> programCmd;
	private Map<String,String> programEpt;
	private Map<String,Map<String,String>> programEnv;
	
	private String comment;
	
	public String getSvnURL() {
		return svnURL;
	}
	public void setSvnURL(String svnURL) {
		this.svnURL = svnURL;
	}
	public String getImageURI() {
		return imageURI;
	}
	public void setImageURI(String dockerImage) {
		this.imageURI = dockerImage;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public Map<String, String> getAppEnv() {
		return appEnv;
	}
	public void setAppEnv(Map<String, String> releaseEnv) {
		this.appEnv = releaseEnv;
	}
	public Map<String, String> getProgramCmd() {
		return programCmd;
	}
	public void setProgramCmd(Map<String, String> processCmd) {
		this.programCmd = processCmd;
	}
	public Map<String, String> getProgramEpt() {
		return programEpt;
	}
	public void setProgramEpt(Map<String, String> processEpt) {
		this.programEpt = processEpt;
	}
	public Map<String, Map<String, String>> getProgramEnv() {
		return programEnv;
	}
	public void setProgramEnv(Map<String, Map<String, String>> processEnv) {
		this.programEnv = processEnv;
	}
}
