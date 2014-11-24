package com.tencent.jflynn.dao;

import java.util.List;

import com.tencent.jflynn.domain.Release;

public interface ReleaseDao {
	public void insert(Release release);
	public List<Release> queryByAppId(String appID);
}
