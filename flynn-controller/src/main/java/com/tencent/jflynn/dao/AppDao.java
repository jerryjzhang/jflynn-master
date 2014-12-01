package com.tencent.jflynn.dao;

import com.tencent.jflynn.domain.App;

public interface AppDao {
	public void insert(App app);
	public App queryByName(String appName);
	public void update(App app);
	public void delete(App app);
}
