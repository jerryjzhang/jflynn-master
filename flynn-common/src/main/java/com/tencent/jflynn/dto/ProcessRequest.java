package com.tencent.jflynn.dto;

public class ProcessRequest {
    private String programName;
    private String processId;
    
    public ProcessRequest(){
    }

    public ProcessRequest(String programName, String processId){
    	this.programName = programName;
    	this.processId = processId;
    }
    
    public void setProgramName(String programName) {
    	this.programName = programName;
    }
    
    public String getProgramName() {
    	return programName;
    }
    
    public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}
	
	public boolean isEmpty(){
		return programName == null && processId == null;
	}
}
