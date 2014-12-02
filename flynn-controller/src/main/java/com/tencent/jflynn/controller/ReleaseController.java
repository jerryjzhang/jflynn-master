package com.tencent.jflynn.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.tencent.jflynn.domain.App;
import com.tencent.jflynn.domain.Release;
import com.tencent.jflynn.dto.ReleaseRequest;
import com.tencent.jflynn.exception.ObjectNotFoundException;
import com.tencent.jflynn.service.AppService;
import com.tencent.jflynn.service.ReleaseService;

@RestController
@RequestMapping("/releases")
public class ReleaseController {
	@Autowired
	private AppService appService;
	@Autowired
	private ReleaseService releaseService;
	
	@RequestMapping(value="/list/app/{appName}", method=RequestMethod.GET)
	public Release[] list(@PathVariable("appName") String appName){
		App app = appService.getAppByName(appName);
    	if(app == null || app.getReleaseID() == null){
    		throw new ObjectNotFoundException();
    	}
    	List<Release> releases = releaseService.getReleasesByAppId(app.getId());
    	return releases.toArray(new Release[releases.size()]);
	}
	
	@RequestMapping(value="/get/app/{appName}", method=RequestMethod.GET)
	public Release getActive(@PathVariable("appName") String appName){
		App app = appService.getAppByName(appName);
    	if(app == null || app.getReleaseID() == null){
    		throw new ObjectNotFoundException();
    	}
    	return releaseService.getReleaseById(app.getReleaseID());
	}
	
	@RequestMapping(value="/rollback/app/{appName}/version/{releaseVersion}", method=RequestMethod.POST)
	public String rollback(@PathVariable("appName") String appName,
			                @PathVariable("releaseVersion") int releaseVersion) {
		App app = appService.getAppByName(appName);
		if (app == null || app.getReleaseID() == null) {
			throw new ObjectNotFoundException();
		}
		
    	ReleaseRequest req = new ReleaseRequest();
    	req.setBaseVersion(releaseVersion);
    	
    	Release release = appService.deployApp(app, req);
    	return release.getId();
	}
}
