package com.tencent.jflynn.controller;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.tencent.jflynn.domain.App;
import com.tencent.jflynn.domain.Release;
import com.tencent.jflynn.dto.DeployRequest;
import com.tencent.jflynn.dto.ScaleRequest;
import com.tencent.jflynn.dto.StopAppRequest;
import com.tencent.jflynn.exception.ObjectNotFoundException;
import com.tencent.jflynn.service.AppService;
import com.tencent.jflynn.service.ReleaseService;
import com.tencent.jflynn.utils.IdGenerator;

@RestController
@RequestMapping("/apps")
public class AppController {
	@Autowired
	private AppService appService;
	
	@Autowired
	private ReleaseService releaseService;
	
	@RequestMapping("/get/{appName}")
    public App get(@PathVariable("appName") String appName) {
    	App app = appService.getAppByName(appName);
    	if(app == null){
    		throw new ObjectNotFoundException();
    	}
    	return app;
    }
	
	@RequestMapping(value="/create/{appName}", method=RequestMethod.POST, produces="application/json")
	public String create(@PathVariable("appName") String appName){
		App app = new App();
		app.setId(IdGenerator.generate());
		app.setName(appName);
		app.setCreateTime(new Timestamp(System.currentTimeMillis()));
		
		//create it using service
		appService.createApp(app);
		
		return app.getId();
	}
	
	@RequestMapping(value="/deploy/{appName}", method=RequestMethod.POST, consumes="application/json")
	public String deploy(@PathVariable("appName") String appName,
			@RequestBody DeployRequest req){
		App app = appService.getAppByName(appName);
    	if(app == null){
    		throw new ObjectNotFoundException();
    	}
    	Release release = appService.deployApp(app, req);
    	
    	return release.getId();
	}
	
	@RequestMapping(value="/scale/{appName}", method=RequestMethod.POST, consumes="application/json")
	public void scale(@PathVariable("appName") String appName,
			@RequestBody ScaleRequest req){
		App app = appService.getAppByName(appName);
		Release release = null;
		if(app == null || 
				app.getReleaseID() == null || 
				(release = releaseService.getReleaseById(app.getReleaseID())) == null){
			throw new ObjectNotFoundException();
		}
		
		appService.scaleApp(app, release, req);
	}
	
	/* Stop an application. */
	@RequestMapping(value="/stop/{appName}", method=RequestMethod.POST, consumes="application/json")
	public void stopApp(@PathVariable("appName") String appName,
			            @RequestBody StopAppRequest request) {
		App app = appService.getAppByName(appName);
		if (app == null) {
			throw new ObjectNotFoundException();
		}
		
		appService.stopApp(app, request);				
	}
	
	/* Rollback the current application to an old version. */
	@RequestMapping(value="/rollback/{appName}/{version}")
	public boolean rollback(@PathVariable("appName") String appName,
			                @PathVariable("version") int version) {
		App app = appService.getAppByName(appName);
		if (app == null) {
			throw new ObjectNotFoundException();
		}
		
		return appService.rollback(app, version);
	}
	
//	@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR, reason="internal server error")  // 409
//	@ExceptionHandler(Exception.class)
//	public void handleException(){
//		//log exception
//		System.err.println("exceptions");
//	}
}
