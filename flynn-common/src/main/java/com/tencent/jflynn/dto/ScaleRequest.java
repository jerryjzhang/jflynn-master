package com.tencent.jflynn.dto;

import java.util.Map;

public class ScaleRequest {
	private Map<String,Integer> programReplica;

	public Map<String, Integer> getProgramReplica() {
		return programReplica;
	}

	public void setProgramReplica(Map<String, Integer> processReplica) {
		this.programReplica = processReplica;
	}
}
