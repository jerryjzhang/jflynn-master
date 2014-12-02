package com.tencent.jflynn.service;

import com.tencent.jflynn.domain.App;
import com.tencent.jflynn.domain.Release;
import com.tencent.jflynn.dto.ReleaseRequest;
import com.tencent.jflynn.dto.ScaleRequest;

public interface AppService {
	public void createApp(App app);
	public void deleteApp(App app);
	public Release deployApp(App app, ReleaseRequest req);
	public void scaleApp(App app, Release release, ScaleRequest req);
	public App getAppByName(String appName);
}
