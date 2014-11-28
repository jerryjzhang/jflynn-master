package com.tencent.jflynn.service.impl;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.tencent.jflynn.dao.AppDao;
import com.tencent.jflynn.dao.ArtifactDao;
import com.tencent.jflynn.dao.FormationDao;
import com.tencent.jflynn.dao.ReleaseDao;
import com.tencent.jflynn.domain.App;
import com.tencent.jflynn.domain.Artifact;
import com.tencent.jflynn.domain.Formation;
import com.tencent.jflynn.domain.Program;
import com.tencent.jflynn.domain.Release;
import com.tencent.jflynn.dto.DeployRequest;
import com.tencent.jflynn.dto.ScaleRequest;
import com.tencent.jflynn.dto.StopAppRequest;
import com.tencent.jflynn.dto.scheduler.ExtendedProgram;
import com.tencent.jflynn.dto.scheduler.ScheduleRequest;
import com.tencent.jflynn.exception.ObjectNotFoundException;
import com.tencent.jflynn.service.AppService;
import com.tencent.jflynn.utils.IdGenerator;
import com.tencent.jflynn.utils.ShellCommandExecutor;

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
	
	private RestTemplate restTemplate = new RestTemplate();
	
	@Value("${httpServerUrl:http://192.168.19.131:8000}")
	private String httpServerUrl;
	@Value("${svnImage:tegdsf/svn}")
	private String svnImage;
	@Value("${slugBuilderImage:flynn/slugbuilder}")
	private String slugBuilderImage;
	@Value("${slugRunnerImage:flynn/slugrunner}")
	private String slugRunnerImage;
	@Value("${slugBuildScript:slugBuild.sh}")
	private String slugBuildScript;
	@Value("${schedulerUrl}")
	private String schedulerUrl;
	@Value("${workMode:standalone}")
	private String workMode;
	
	private static final Pattern PATTERN_TYPES = Pattern.compile(".*declares types -> (.*)");
	
	public void createApp(App app){
		appDao.insert(app);
	}
	
	public App getAppByName(String appName){
		return appDao.queryByName(appName);
	}
	
	public Release deployApp(App app, DeployRequest req){
		Release release = null;
		if(app.getReleaseID() != null){
			release = releaseDao.queryById(app.getReleaseID());
		}
		if(release == null){
			release = new Release();
			release.setAppID(app.getId());
		}
		release.setId(IdGenerator.generate());
		release.setVersion(app.getLatestVersion() + 1);
		release.setTag(req.getComment());
		release.setCreateTime(new Timestamp(System.currentTimeMillis()));
		app.setLatestVersion(release.getVersion());
		app.setReleaseID(release.getId());
		
		//build new artifact if either svnURL or dockerImage is specified
		if(req.getSvnURL() != null){
			handleSvnDeploy(app, release, req);
		}else if(req.getDockerImage() != null){
			handleImageDeploy(app, release, req);
		}
		
		//update release environment variables
		if(req.getAppEnv() != null){
			for(Map.Entry<String, String> e : req.getAppEnv().entrySet()){
				release.getAppEnv().put(e.getKey(), e.getValue());
			}
		}
		
		//update process cmd 
		if(req.getProgramCmd() != null){
			for(Map.Entry<String, String> e : req.getProgramCmd().entrySet()){
				String procName = e.getKey();
				Program procType = release.getPrograms().get(procName);
				if(procType == null){
					procType = new Program();
					release.getPrograms().put(procName, procType);
				}
				procType.setCmd(e.getValue());
			}
		}
		
		//update process entrypoint
		if(req.getProgramEpt() != null){
			for(Map.Entry<String, String> e : req.getProgramEpt().entrySet()){
				String procName = e.getKey();
				Program procType = release.getPrograms().get(procName);
				if(procType == null){
					procType = new Program();
					release.getPrograms().put(procName, procType);
				}
				procType.setEntrypoint(e.getValue());
			}
		}		
		
		//update process environment variables
		if(req.getProgramEnv() != null){
			for(Map.Entry<String, Map<String,String>> e : req.getProgramEnv().entrySet()){
				String procName = e.getKey();
				Program procType = release.getPrograms().get(procName);
				if(procType == null){
					procType = new Program();
					release.getPrograms().put(procName, procType);
				}
				for(Map.Entry<String, String> env : e.getValue().entrySet()){
					procType.getEnv().put(env.getKey(), env.getValue());
				}
			}
		}		
		
		//if no processes in the release, create a "default" one
		if(release.getPrograms().size() == 0){
			release.getPrograms().put("default", new Program());
		}
		
		releaseDao.insert(release);
		LOG.info("Created release for appName=" + app.getName() + " release=" + release);
		appDao.update(app);
		LOG.info("Updated appName=" + app.getName() + " set current releaseId=" + app.getReleaseID());
		
		return release;
	}
	
	private void handleImageDeploy(App app, Release release, DeployRequest req){
		//create artifact and release object
		Artifact artifact = new Artifact();
		artifact.setId(IdGenerator.generate());
		artifact.setUri(req.getDockerImage());
		artifact.setCreateTime(new Timestamp(System.currentTimeMillis()));
		artifactDao.insert(artifact);
		LOG.info("Created artifact for appName=" + app.getName() + " artifact=" + artifact);
		
		release.setArtifactID(artifact.getId());
	}
	
	private void handleSvnDeploy(App app, Release release, DeployRequest req){
		String fileName = app.getName() + "-" + System.currentTimeMillis();
		Map<String,String> env = new HashMap<String,String>();
		env.put("SVN_URL", req.getSvnURL());
		env.put("APP_NAME", fileName);
		env.put("IMAGE_SVN", svnImage);
		env.put("HTTP_SERVER_URL", httpServerUrl);
		env.put("IMAGE_SLUGBUILDER", slugBuilderImage);
		
		String cmd = slugBuildScript;
		String out = ShellCommandExecutor.execute(cmd, env);
		System.out.println(out);
		//Grep output and extract process types
		Matcher m = PATTERN_TYPES.matcher(out);
		String [] processTypes = null;
		if(m.find()){
			processTypes = m.group(1).split(", ");
		}
		
		//create artifact and release object
		Artifact artifact = new Artifact();
		artifact.setId(IdGenerator.generate());
		artifact.setUri(slugRunnerImage);
		artifact.setCreateTime(new Timestamp(System.currentTimeMillis()));
		artifactDao.insert(artifact);
		LOG.info("Created artifact for appName=" + app.getName() + " artifact=" + artifact);
		
		release.setArtifactID(artifact.getId());
		release.getAppEnv().put("SLUG_URL", httpServerUrl + "/slugs/" + fileName + ".tgz");
		if(processTypes != null){
			for(String type : processTypes){
				type = type.trim();
				Program ptype = new Program();
				ptype.setCmd("start " + type);
				release.getPrograms().put(type, ptype);
			}
		}
	}
	
	public void scaleApp(App app, Release release, ScaleRequest req){
		if("standalone".equals(workMode)){
			scaleAppLocal(app, release, req);
			return;
		}
		
		ScheduleRequest sreq = new ScheduleRequest();
		Artifact artifact = artifactDao.queryById(release.getArtifactID());
		sreq.setAppName(app.getName());
		sreq.setAppEnv(release.getAppEnv());
		sreq.setImageUri(artifact.getUri());
		for(Map.Entry<String,Integer> e : req.getProgramReplica().entrySet()){
			String programName = e.getKey();
			ExtendedProgram ep = new ExtendedProgram();
			ep.setReplica(e.getValue());
			ep.setProgram(release.getPrograms().get(programName));
			sreq.getPrograms().put(programName, ep);
		}
		
		String success = restTemplate.postForEntity(schedulerUrl+"/apps/scale/"+app.getName(), sreq, String.class).getBody();
		LOG.info("Scheduled programs for appName=" + app.getName() + " response: " + success);
	}
	
	public void scaleAppLocal(App app, Release release, ScaleRequest req){
		Formation formation = formationDao.queryByAppId(app.getId());
		if(formation == null){
			formation = new Formation();
			formation.setAppID(app.getId());
		}
		
		formation.setReleaseID(release.getId());
		for(Map.Entry<String,Integer> e : req.getProgramReplica().entrySet()){
			Program ptype = release.getPrograms().get(e.getKey());
			if(ptype == null)continue;
			formation.getProgramReplica().put(e.getKey(), e.getValue());
		}
		formationDao.save(formation);
		
		//simulate replicationController and agent logic
		for(Map.Entry<String,Integer> e : req.getProgramReplica().entrySet()){
			for(int i=1;i<=e.getValue();i++){
				Program ptype = release.getPrograms().get(e.getKey());
				if(ptype == null)continue;
				StringBuilder cmd = new StringBuilder();
				cmd.append("docker run -d -P ");
				if(ptype.getEntrypoint() != null){
					cmd.append(" -entrypoint " + ptype.getEntrypoint());
					cmd.append(" ");
				}
				for(Map.Entry<String,String> env : release.getAppEnv().entrySet()){
					cmd.append(" -e " + env.getKey() + "=" + env.getValue());
					cmd.append(" ");
				}
				for(Map.Entry<String,String> env : ptype.getEnv().entrySet()){
					cmd.append(" -e " + env.getKey() + "=" + env.getValue());
					cmd.append(" ");
				}
				Artifact artifact = artifactDao.queryById(release.getArtifactID());
				cmd.append(" " + artifact.getUri() + " ");
				if(ptype.getCmd() != null){
					cmd.append(" " + ptype.getCmd());
				}
				ShellCommandExecutor.execute(cmd.toString());
			}
		}
	}
	
	public List<Release> getAppReleases(App app) {
		return releaseDao.queryByAppId(app.getId());
	}

	public Formation getFormationByAppId(String appId) {
		return formationDao.queryByAppId(appId);
	}

	public boolean stopApp(App app, StopAppRequest request) {
		Release release = releaseDao.queryById(app.getReleaseID());
		if (release == null) {
			throw new ObjectNotFoundException();
		}
		
		/* Stop the whole app. */
		if (request == null) {
			Map<String, Program> programs = release.getPrograms();
			if (programs == null || programs.size() == 0) {
				return false;
			}
		
			/* TODO: should call gaia API to stop programs. */
			ScaleRequest scaleRequest = new ScaleRequest();
			Map<String, Integer> replicas = new HashMap<String, Integer>();
			Set<String> programNames = programs.keySet();
			for (String programName : programNames) {			
				replicas.put(programName, 0);
			}
			scaleRequest.setProgramReplica(replicas);
			
			scaleApp(app, release, scaleRequest);
			
			return true;
		}
		
		/* No program or container is specified to stop, which is illegal. */
		if (request.getProgramName() == null && request.getContainerId() == null) {
			return false;
		}
		
		/* Stop a program for an application. */
		if (request.isStopProgram()) {
			Map<String, Program> programs = release.getPrograms();
			if (programs == null || programs.size() == 0) {
				return false;
			}
			
			/* TODO: should call gaia API to stop this program. */
			ScaleRequest scaleRequest = new ScaleRequest();
			Map<String, Integer> replicas = new HashMap<String, Integer>();
			replicas.put(request.getProgramName(), 0);
			scaleRequest.setProgramReplica(replicas);
			
			scaleApp(app, release, scaleRequest);
		}
		
		/* Stop a specified container. */
		if (request.isStopContainer()) {
			/* TODO: wait for layer0 API. */
		}
		
		return false;
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
		DeployRequest request = new DeployRequest();
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
			
			if (programEpt.size() > 0) {
				request.setProgramEpt(programEpt);
			}
			
			if (programEnv.size() > 0) {
				request.setProgramEnv(programEnv);
			}
			
			if (programCmd.size() > 0) {
				request.setProgramCmd(programCmd);
			}
		}
		
		Artifact artifact = artifactDao.queryById(release.getArtifactID());
		if (artifact == null) {
			throw new ObjectNotFoundException();
		}
		request.setDockerImage(artifact.getUri());
		
		deployApp(app, request);
		
		/* TODO: currently always return true, should depend on the deploy result. */
		return true;
	}		
}
