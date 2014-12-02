package com.tencent.jflynn.domain;

public class Process {
	private String appName;
	private String programName;
	private String processId;
	private int vCores;
	private int memory;
	private String nodeHost;
	private String nodePort;
	private int priority;
	private long startTime;
	
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getProgramName() {
		return programName;
	}
	public void setProgramName(String programName) {
		this.programName = programName;
	}
	public String getProcessId() {
		return processId;
	}
	public void setProcessId(String processId) {
		this.processId = processId;
	}
	public void setvCores(int vCores) {
		this.vCores = vCores;
	}
	public int getvCores() {
		return this.vCores;
	}
	
	public void setMemory(int memory) {
		this.memory = memory;
	}
	public int getMemory() {
		return this.memory;
	}
	
	public void setNodeHost(String nodeHost) {
		this.nodeHost = nodeHost;
	}
	public String getNodeHost() {
		return this.nodeHost;
	}
	
	public void setNodePort(String nodePort) {
		this.nodePort = nodePort;
	}
	public String getNodePort() {
		return this.nodePort;
	}
	
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public int getPriority() {
		return this.priority;
	}
	
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getStartTime() {
		return this.startTime;
	}
	
}