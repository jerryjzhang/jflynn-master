package com.tencent.jflynn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tencent.jflynn.dao.AppDao;
import com.tencent.jflynn.domain.App;
import com.tencent.jflynn.service.AppService;

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
}
