package com.tencent.jflynn.service;

import com.tencent.jflynn.domain.App;

public interface AppService {
	public void createApp(App app);
	public App getAppByName(String appName);
}
