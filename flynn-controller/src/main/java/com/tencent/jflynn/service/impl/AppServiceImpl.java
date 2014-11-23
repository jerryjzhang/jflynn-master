package com.tencent.jflynn.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tencent.jflynn.dao.AppDao;
import com.tencent.jflynn.domain.App;
import com.tencent.jflynn.domain.Release;
import com.tencent.jflynn.dto.AppRequest;
import com.tencent.jflynn.service.AppService;
import com.tencent.jflynn.utils.ShellCommandExecutor;

@Service
public class AppServiceImpl implements AppService {
	@Autowired
	private AppDao appDao;
	
	public void createApp(App app){
		appDao.insert(app);
	}
	
	public App getAppByName(String appName){
		return appDao.queryByName(appName);
	}
	
	public void deployApp(App app, AppRequest req){
		String appBasePath = "/tmp/" + app.getName() + "-" + System.currentTimeMillis();
		//svn export code and create tarball
		String cmd = "svn export " + req.getSvnURL() + " " + appBasePath;
		ShellCommandExecutor.execute(cmd);
		cmd = "tar cvf " + appBasePath + ".tar --directory=" + appBasePath + " .";
		ShellCommandExecutor.execute(cmd);
		//docker run slugbuilder to create slug.tgz
		cmd = "cat " + appBasePath + ".tar | docker run -i -v /tmp/buildpacks:/tmp/buildpacks " +
				"-e HTTP_SERVER_URL=http://192.168.59.103:8080 -a stdin flynn/slugbuilder " +
				"- > " + appBasePath + ".tgz";
		ShellCommandExecutor.execute(cmd);
		//create artifact and release object
	}
	
	public List<Release> getAppReleases(App app){
		return null;
	}
	
	
}
