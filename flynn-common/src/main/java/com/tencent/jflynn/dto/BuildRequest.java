package com.tencent.jflynn.dto;

import java.util.HashMap;
import java.util.Map;

public class BuildRequest {
	private String appName;
	Map<String,String> context = new HashMap<String,String>();
	
	public BuildRequest(){
	}

	public BuildRequest(String name, Map<String,String> context){
		this.appName = name;
		this.context = context;
	}
	
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public Map<String, String> getContext() {
		return context;
	}
	public void setContext(Map<String, String> context) {
		this.context = context;
	}
}
