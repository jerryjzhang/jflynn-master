package com.tencent.jflynn.service.impl;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tencent.jflynn.dao.AppDao;
import com.tencent.jflynn.dao.ArtifactDao;
import com.tencent.jflynn.dao.FormationDao;
import com.tencent.jflynn.domain.App;
import com.tencent.jflynn.domain.Artifact;
import com.tencent.jflynn.domain.Formation;
import com.tencent.jflynn.domain.Release;
import com.tencent.jflynn.dto.ReleaseRequest;
import com.tencent.jflynn.dto.ScaleRequest;
import com.tencent.jflynn.dto.scheduler.ExtendedProgram;
import com.tencent.jflynn.dto.scheduler.ScheduleRequest;
import com.tencent.jflynn.service.AppService;
import com.tencent.jflynn.service.ReleaseService;
import com.tencent.jflynn.service.ProcessService;

@Service
public class AppServiceImpl implements AppService {
	private static final Logger LOG = Logger.getLogger(AppServiceImpl.class);
	
	@Autowired
	private AppDao appDao;
	@Autowired
	private ArtifactDao artifactDao;
	@Autowired
	private FormationDao formationDao;
	@Autowired
	private ProcessService processService;
	@Autowired
	private ReleaseService releaseService;
	
	public void createApp(App app){
		appDao.insert(app);
	}
	
	public void deleteApp(App app){
		appDao.delete(app);
	}
	
	public App getAppByName(String appName){
		return appDao.queryByName(appName);
	}
	
	public Release deployApp(App app, ReleaseRequest req){
		Release release = releaseService.createRelease(app, req);
		app.setLatestVersion(release.getVersion());
		app.setReleaseID(release.getId());
		appDao.update(app);
		LOG.info("Updated appName=" + app.getName() + " set current releaseId=" + app.getReleaseID());
		
		return release;
	}

	public void scaleApp(App app, Release release, ScaleRequest req){
		ScheduleRequest sreq = new ScheduleRequest();
		Artifact artifact = artifactDao.queryById(release.getArtifactID());
		sreq.setAppName(app.getName());
		sreq.setAppEnv(release.getAppEnv());
		sreq.setImageUri(artifact.getUri());
		for(Map.Entry<String,Integer> e : req.getProgramReplica().entrySet()){
			String programName = e.getKey();
			if(release.getPrograms().get(programName) == null){
				continue;
			}
			ExtendedProgram ep = new ExtendedProgram();
			ep.setReplica(e.getValue());
			ep.setProgram(release.getPrograms().get(programName));
			sreq.getPrograms().put(programName, ep);
		}
		
		processService.schedule(app.getName(), sreq);
		LOG.info("Scheduled programs for appName=" + app.getName());
	}

	public Formation getFormationByAppId(String appId) {
		return formationDao.queryByAppId(appId);
	}
}
