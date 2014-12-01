package com.tencent.jflynn.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.tencent.jflynn.domain.App;
import com.tencent.jflynn.domain.Program;
import com.tencent.jflynn.domain.Release;
import com.tencent.jflynn.dto.ReleaseRequest;
import com.tencent.jflynn.exception.ObjectNotFoundException;
import com.tencent.jflynn.service.AppService;
import com.tencent.jflynn.service.ReleaseService;


@RestController
@RequestMapping("/programs")
public class ProgramController {
	@Autowired
	private AppService appService;
	@Autowired
	private ReleaseService releaseService;
	
	@RequestMapping(value="/save/{appName}", method=RequestMethod.POST, consumes="application/json")
	public String save(@PathVariable("appName") String appName,
			@RequestBody Program program){
		App app = appService.getAppByName(appName);
    	if(app == null){
    		throw new ObjectNotFoundException();
    	}
    	ReleaseRequest req = new ReleaseRequest();
    	req.setPrograms(new Program[1]);
    	req.getPrograms()[0] = program;
    	Release release = appService.deployApp(app, req);
    	
    	return release.getId();
	}
	
	@RequestMapping(value="/list/{appName}", method=RequestMethod.POST, produces="application/json")
	public Program[] list(@PathVariable("appName") String appName){
		App app = appService.getAppByName(appName);
    	if(app == null || app.getReleaseID() == null){
    		throw new ObjectNotFoundException();
    	}
		
		List<Program> programs = releaseService.getPrograms(app.getReleaseID());
		
		return programs.toArray(new Program[programs.size()]);
	}
	
	@RequestMapping(value="/delete/{appName}", method=RequestMethod.DELETE)
	public void delete(@PathVariable("appName") String appName, @PathVariable("programName") String programName){
		
	}
}
