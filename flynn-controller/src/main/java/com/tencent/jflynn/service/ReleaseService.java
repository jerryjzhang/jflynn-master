package com.tencent.jflynn.service;

import java.util.List;

import com.tencent.jflynn.domain.App;
import com.tencent.jflynn.domain.Program;
import com.tencent.jflynn.domain.Release;
import com.tencent.jflynn.dto.ReleaseRequest;

public interface ReleaseService {
	public Release getReleaseById(String releaseId);
	public List<Program> getPrograms(String releaseId);
	public Release createRelease(App app, ReleaseRequest req);
}
