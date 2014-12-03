package com.tencent.jflynn.dto;

public class BuildResponse {
	private String imageUri;
	private String buildOutput;
	
	public BuildResponse(){
	}
	
	public BuildResponse(String uri, String out){
		this.imageUri = uri;
		this.buildOutput = out;
	}
	
	public String getImageUri() {
		return imageUri;
	}
	public void setImageUri(String imageUri) {
		this.imageUri = imageUri;
	}
	public String getBuildOutput() {
		return buildOutput;
	}
	public void setBuildOutput(String buildOutput) {
		this.buildOutput = buildOutput;
	}
}
