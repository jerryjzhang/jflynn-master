package com.tencent.jflynn.service;

import java.util.List;

import com.tencent.jflynn.domain.App;
import com.tencent.jflynn.domain.Release;
import com.tencent.jflynn.dto.ReleaseRequest;
import com.tencent.jflynn.dto.ScaleRequest;
import com.tencent.jflynn.dto.StopAppRequest;

public interface AppService {
	public void createApp(App app);
	public void deleteApp(App app);
	public App getAppByName(String appName);
	public Release deployApp(App app, ReleaseRequest req);
	public List<Release> getAppReleases(App app);
	public void scaleApp(App app, Release release, ScaleRequest req);
	public void stopApp(App app, StopAppRequest request);
	public boolean rollback(App app, int version);
}
