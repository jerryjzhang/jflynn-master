package com.tencent.jflynn.domain;

import java.util.HashMap;
import java.util.Map;

public class ProcessType {
	private String cmd;
	private String entrypoint;
	private Map<String,String> env = new HashMap<String,String>();
	public String getCmd() {
		return cmd;
	}
	public void setCmd(String cmd) {
		this.cmd = cmd;
	}
	public String getEntrypoint() {
		return entrypoint;
	}
	public void setEntrypoint(String entrypoint) {
		this.entrypoint = entrypoint;
	}
	public Map<String, String> getEnv() {
		return env;
	}
	public void setEnv(Map<String, String> env) {
		this.env = env;
	}
	
	@Override
	public String toString() {
		return "ProcessType [cmd=" + cmd + ", entrypoint=" + entrypoint
				+ ", env=" + env + "]";
	}
}
