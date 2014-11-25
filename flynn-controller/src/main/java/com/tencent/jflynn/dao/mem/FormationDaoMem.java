package com.tencent.jflynn.dao.mem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.tencent.jflynn.dao.FormationDao;
import com.tencent.jflynn.domain.Formation;

@Repository
public class FormationDaoMem implements FormationDao{
	private Map<String,Formation> appIdToFormation = 
			new HashMap<String,Formation>();
	
	public void save(Formation formation){
		appIdToFormation.put(formation.getAppID(), formation);
	}
	
	public List<Formation> queryAll(){
		return new ArrayList<Formation>(appIdToFormation.values());
	}
	
	public Formation queryByAppId(String appId){
		return appIdToFormation.get(appId);
	}
}
