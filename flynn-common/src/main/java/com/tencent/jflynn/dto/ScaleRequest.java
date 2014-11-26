package com.tencent.jflynn.dto;

import java.util.Map;

public class ScaleRequest {
	private Map<String,Integer> processReplica;

	public Map<String, Integer> getProcessReplica() {
		return processReplica;
	}

	public void setProcessReplica(Map<String, Integer> processReplica) {
		this.processReplica = processReplica;
	}
}
