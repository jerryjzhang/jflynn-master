package com.tencent.jflynn.dto;

import java.util.Map;

import com.tencent.jflynn.domain.Program;

public class ReleaseRequest {
	private String svnURL;
	private String imageURI;
	private Map<String,String> appEnv;
	private Program[] savePrograms;
	private String[]  deletePrograms;
	private Integer baseVersion;
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
	public Program[] getSavePrograms() {
		return savePrograms;
	}
	public void setSavePrograms(Program[] programs) {
		this.savePrograms = programs;
	}
	public String[] getDeletePrograms() {
		return deletePrograms;
	}
	public void setDeletePrograms(String[] deletePrograms) {
		this.deletePrograms = deletePrograms;
	}
	public Integer getBaseVersion() {
		return baseVersion;
	}
	public void setBaseVersion(Integer baseVersion) {
		this.baseVersion = baseVersion;
	}
}
