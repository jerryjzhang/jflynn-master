package com.tencent.jflynn.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tencent.jflynn.dao.AppDao;
import com.tencent.jflynn.dao.ArtifactDao;
import com.tencent.jflynn.dao.FormationDao;
import com.tencent.jflynn.dao.ReleaseDao;
import com.tencent.jflynn.domain.App;
import com.tencent.jflynn.domain.Artifact;
import com.tencent.jflynn.domain.Formation;
import com.tencent.jflynn.domain.Program;
import com.tencent.jflynn.domain.Release;
import com.tencent.jflynn.dto.ReleaseRequest;
import com.tencent.jflynn.dto.ScaleRequest;
import com.tencent.jflynn.dto.StopAppRequest;
import com.tencent.jflynn.dto.scheduler.ExtendedProgram;
import com.tencent.jflynn.dto.scheduler.ScheduleRequest;
import com.tencent.jflynn.exception.ObjectNotFoundException;
import com.tencent.jflynn.service.AppService;
import com.tencent.jflynn.service.ReleaseService;
import com.tencent.jflynn.service.SchedulerService;

@Service
public class AppServiceImpl implements AppService {
	private static final Logger LOG = Logger.getLogger(AppServiceImpl.class);
	
	@Autowired
	private AppDao appDao;
	@Autowired
	private ReleaseDao releaseDao;
	@Autowired
	private ArtifactDao artifactDao;
	@Autowired
	private FormationDao formationDao;
	@Autowired
	private SchedulerService scheduler;
	@Autowired
	private ReleaseService releaseService;
	
	@Value("${httpServerUrl}")
	private String httpServerUrl;
	@Value("${svnImage:tegdsf/svn}")
	private String svnImage;
	@Value("${slugBuilderImage:tegdsf/slugbuilder}")
	private String slugBuilderImage;
	@Value("${slugRunnerImage:tegdsf/slugrunner}")
	private String slugRunnerImage;
	@Value("${slugBuildScript:slugBuild.sh}")
	private String slugBuildScript;
	
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
		
		scheduler.schedule(sreq);
		LOG.info("Scheduled programs for appName=" + app.getName());
	}

	public List<Release> getAppReleases(App app) {
		return releaseDao.queryByAppId(app.getId());
	}

	public Formation getFormationByAppId(String appId) {
		return formationDao.queryByAppId(appId);
	}

	public void stopApp(App app, StopAppRequest req) {
		if(req != null && req.getProgramName() != null){
			scheduler.stopProgram(req.getProgramName());
		}else{
			scheduler.stopApp(app.getName());
		}
	}

	public boolean rollback(App app, int version) {
		/* Check whether the application is currently stopped. */
		Formation formation = formationDao.queryByAppId(app.getId());
		if (formation == null) {
			throw new ObjectNotFoundException();
		}
		
		/* Just return false if there exists running programs. */
		for (Map.Entry<String, Integer> entry : formation.getProgramReplica().entrySet()) {
			if (entry.getValue() > 0) {
				return false;
			}
		}
		
		/* Validate the version information. */
		if (version <= 0 || version >= app.getLatestVersion()) {
			throw new ObjectNotFoundException();
		}
				
		Release release = releaseDao.queryByAppIdAndVersion(app.getId(), version);
		if (release == null) {
			throw new ObjectNotFoundException();
		}
		
		/* Composite the deploy request for this release. */
		ReleaseRequest request = new ReleaseRequest();
		request.setComment(release.getTag() + ", rollback to version " + version);
		request.setAppEnv(release.getAppEnv());
		
		/* Get the information from the old release. */
		if (release.getPrograms() != null && release.getPrograms().size() > 0) {
			Map<String, String> programEpt = new HashMap<String, String>();
			Map<String, Map<String, String>> programEnv = 
				new HashMap<String, Map<String, String>>();
			Map<String, String> programCmd = new HashMap<String, String>();
			for (Map.Entry<String, Program> entry : release.getPrograms().entrySet()) {
				programEpt.put(entry.getKey(), entry.getValue().getEntrypoint());
				programCmd.put(entry.getKey(), entry.getValue().getCmd());
				
				Map<String, String> envs = entry.getValue().getEnv();
				if (envs != null && envs.size() > 0) {
					Map<String, String> rebuildProgramEnv = new HashMap<String, String>();
					for (Map.Entry<String, String> envEntry : envs.entrySet()) {
						rebuildProgramEnv.put(envEntry.getKey(), envEntry.getValue());
					}
					
					programEnv.put(entry.getKey(), rebuildProgramEnv);
				}
			}
			
//			if (programEpt.size() > 0) {
//				request.setProgramEpt(programEpt);
//			}
//			
//			if (programEnv.size() > 0) {
//				request.setProgramEnv(programEnv);
//			}
//			
//			if (programCmd.size() > 0) {
//				request.setProgramCmd(programCmd);
//			}
		}
		
		Artifact artifact = artifactDao.queryById(release.getArtifactID());
		if (artifact == null) {
			throw new ObjectNotFoundException();
		}
		request.setImageURI(artifact.getUri());
		
		deployApp(app, request);
		
		/* TODO: currently always return true, should depend on the deploy result. */
		return true;
	}		
}
