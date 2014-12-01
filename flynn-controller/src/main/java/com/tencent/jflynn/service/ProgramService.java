package com.tencent.jflynn.service;

import java.util.List;

import com.tencent.jflynn.domain.Program;

public interface ProgramService {
	public List<Program> getPrograms(String releaseId);
}
