package com.tencent.jflynn.dao.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.tencent.jflynn.dao.AppDao;
import com.tencent.jflynn.domain.App;

@Repository
public class AppDaoImpl implements AppDao {
	private Map<String, App> nameToApp = new HashMap<String, App>();
	private Map<String, App> idToApp = new HashMap<String, App>();
	
	public void insert(App app){
		nameToApp.put(app.getName(), app);
		idToApp.put(app.getId(), app);
	}
	
	public App queryByName(String appName){
		return nameToApp.get(appName);
	}
}
