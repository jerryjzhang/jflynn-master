package com.tencent.jflynn.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.tencent.jflynn.domain.App;
import com.tencent.jflynn.domain.Release;
import com.tencent.jflynn.exception.ObjectNotFoundException;
import com.tencent.jflynn.service.AppService;

@RestController
@RequestMapping("/releases")
public class ReleaseController {
	@Autowired
	private AppService appService;
	
	@RequestMapping(value="/get/app/{appName}", method=RequestMethod.GET)
	public Release[] list(@PathVariable("appName") String appName){
		App app = appService.getAppByName(appName);
    	if(app == null){
    		throw new ObjectNotFoundException();
    	}
    	List<Release> releases = appService.getAppReleases(app);
    	return releases.toArray(new Release[releases.size()]);
	}
	
	@RequestMapping(value="/rollback/app/{appName}/version/{releaseVersion}")
	public boolean rollback(@PathVariable("appName") String appName,
			                @PathVariable("releaseVersion") int releaseVersion) {
		App app = appService.getAppByName(appName);
		if (app == null) {
			throw new ObjectNotFoundException();
		}
		
		return appService.rollback(app, releaseVersion);
	}
}
