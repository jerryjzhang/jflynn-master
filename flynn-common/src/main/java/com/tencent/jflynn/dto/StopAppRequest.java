package com.tencent.jflynn.dto;

public class StopAppRequest {
    private String programName;
    private String containerId;
    
    public void setProgramName(String programName) {
    	this.programName = programName;
    }
    
    public String getProgramName() {
    	return programName;
    }
    
    public void setContainerId(String containerId) {
    	this.containerId = containerId;
    }
    
    public String getContainerId() {
    	return containerId;
    }
    
    public boolean isStopProgram() {
    	return programName != null && containerId == null;
    }
    
    public boolean isStopContainer() {
    	return programName != null && containerId != null;
    }
}
