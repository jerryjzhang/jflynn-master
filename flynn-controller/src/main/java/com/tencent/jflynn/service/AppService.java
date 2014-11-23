package com.tencent.jflynn.service;

import com.tencent.jflynn.domain.App;
import com.tencent.jflynn.dto.AppRequest;

public interface AppService {
	public void createApp(App app);
	public App getAppByName(String appName);
	public void deployApp(App app, AppRequest svnURL);
}
