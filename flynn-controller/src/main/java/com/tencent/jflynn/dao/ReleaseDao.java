package com.tencent.jflynn.dao;

import java.util.List;

import com.tencent.jflynn.domain.Program;
import com.tencent.jflynn.domain.Release;

public interface ReleaseDao {
	public void insert(Release release);
	public Release queryById(String id);
	public List<Release> queryByAppId(String appId);
	public Release queryByAppIdAndVersion(String appId, int version);
	public List<Program> queryPrograms(String releaseId);
}
