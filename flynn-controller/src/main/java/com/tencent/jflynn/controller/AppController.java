package com.tencent.jflynn.controller;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tencent.jflynn.domain.App;
import com.tencent.jflynn.domain.Release;
import com.tencent.jflynn.dto.AppRequest;
import com.tencent.jflynn.exception.ObjectNotFoundException;
import com.tencent.jflynn.service.AppService;
import com.tencent.jflynn.utils.IdGenerator;

@RestController
@RequestMapping("/apps")
public class AppController {
	@Autowired
	private AppService appService;
	
	@RequestMapping("/get/{appName}")
    public App get(@PathVariable("appName") String appName) {
    	App app = appService.getAppByName(appName);
    	if(app == null){
    		throw new ObjectNotFoundException();
    	}
    	return app;
    }
	
	@RequestMapping(value="/create/{appName}", method=RequestMethod.POST)
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
	public void deploy(@PathVariable("appName") String appName,
			@RequestBody AppRequest req){
		App app = appService.getAppByName(appName);
    	if(app == null){
    		throw new ObjectNotFoundException();
    	}
    	appService.deployApp(app, req);
	}
	
	@RequestMapping(value="/list/release/{appName}", method=RequestMethod.GET)
	public List<Release> getReleases(@PathVariable("appName") String appName){
		App app = appService.getAppByName(appName);
    	if(app == null){
    		throw new ObjectNotFoundException();
    	}
    	return appService.getAppReleases(app);
	}
	
//	@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR, reason="internal server error")  // 409
//	@ExceptionHandler(Exception.class)
//	public void handleException(){
//		//log exception
//		System.err.println("exceptions");
//	}
}
