package com.tencent.jflynn.dto.scheduler;

import com.tencent.jflynn.domain.Program;

public class ExtendedProgram {
	private Program program;
	private int replica;
	
	public Program getProgram() {
		return program;
	}
	public void setProgram(Program program) {
		this.program = program;
	}
	public int getReplica() {
		return replica;
	}
	public void setReplica(int replica) {
		this.replica = replica;
	}
}
