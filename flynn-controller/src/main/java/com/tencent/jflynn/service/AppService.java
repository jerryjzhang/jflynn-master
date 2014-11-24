package com.tencent.jflynn.service;

import java.util.List;

import com.tencent.jflynn.domain.App;
import com.tencent.jflynn.domain.Formation;
import com.tencent.jflynn.domain.Release;
import com.tencent.jflynn.dto.AppRequest;

public interface AppService {
	public void createApp(App app);
	public App getAppByName(String appName);
	public void deployApp(App app, AppRequest svnURL);
	public List<Release> getAppReleases(App app);
	public void scaleApp(App app, Release release, Formation formation);
}
