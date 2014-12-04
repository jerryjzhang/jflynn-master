package com.tencent.jflynn.dao.mem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.tencent.jflynn.dao.AppDao;
import com.tencent.jflynn.domain.App;

@Repository
public class AppDaoMem implements AppDao {
	private Map<String, App> nameToApp = new HashMap<String, App>();
	private Map<String, App> idToApp = new HashMap<String, App>();
	
	public void insert(App app){
		nameToApp.put(app.getName(), app);
		idToApp.put(app.getId(), app);
	}
	
	public List<App> queryAll(){
		return new ArrayList<App>(idToApp.values());
	}
	
	public App queryByName(String appName){
		return nameToApp.get(appName);
	}
	
	public void update(App app){
		nameToApp.put(app.getName(), app);
		idToApp.put(app.getId(), app);
	}
	
	public void delete(App app){
		nameToApp.remove(app.getName());
		idToApp.remove(app.getId());
	}
}
