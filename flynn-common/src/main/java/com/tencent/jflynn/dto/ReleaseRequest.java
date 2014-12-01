package com.tencent.jflynn.dto;

import java.util.Map;

import com.tencent.jflynn.domain.Program;

public class ReleaseRequest {
	private String svnURL;
	private String imageURI;
	private Map<String,String> appEnv;
	private Program[] programs;
	
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
	public Program[] getPrograms() {
		return programs;
	}
	public void setPrograms(Program[] programs) {
		this.programs = programs;
	}
}
