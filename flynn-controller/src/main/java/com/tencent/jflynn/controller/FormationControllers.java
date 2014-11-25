package com.tencent.jflynn.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.tencent.jflynn.domain.App;
import com.tencent.jflynn.domain.Formation;
import com.tencent.jflynn.exception.ObjectNotFoundException;
import com.tencent.jflynn.service.AppService;
import com.tencent.jflynn.service.FormationService;

@RestController
@RequestMapping("/formations")
public class FormationControllers {
	@Autowired
	private FormationService formationService;
	
	@Autowired
	private AppService appService;
	
	@RequestMapping("/getAll")
    public List<Formation> getAll() {
    	return formationService.getAllFormations();
    }
	
	@RequestMapping(value="/get/app/{appName}", method=RequestMethod.GET, consumes="application/json")
	public Formation getAppFormation(@PathVariable("appName") String appName){
		App app = appService.getAppByName(appName);
    	if(app == null){
    		throw new ObjectNotFoundException();
    	}
		
		return formationService.getAppFormation(app.getId());
	}
}
