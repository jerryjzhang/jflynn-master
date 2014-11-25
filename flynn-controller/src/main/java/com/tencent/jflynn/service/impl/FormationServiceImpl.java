package com.tencent.jflynn.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tencent.jflynn.dao.FormationDao;
import com.tencent.jflynn.domain.Formation;
import com.tencent.jflynn.service.FormationService;

@Service
public class FormationServiceImpl implements FormationService{
	@Autowired
	private FormationDao formationDao;
	
	public List<Formation> getAllFormations(){
		return formationDao.queryAll();
	}
	
	public Formation getAppFormation(String appId){
		return formationDao.queryByAppId(appId);
	}
}
