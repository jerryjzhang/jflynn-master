package com.tencent.jflynn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tencent.jflynn.domain.App;
import com.tencent.jflynn.service.AppService;

@RestController
@RequestMapping("/apps")
public class AppController {
	@Autowired
	private AppService appService;
	
	@RequestMapping("/get/{appName}")
    public App get(@PathVariable("appName") String appName) {
    	App app = new App();
    	app.setId("junz");
    	app.setName(appName);
    	return app;
    }
}
