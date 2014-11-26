package com.tencent.jflynn.service.impl;

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.tencent.jflynn.domain.ProcessType;
import com.tencent.jflynn.domain.Release;
import com.tencent.jflynn.dto.DeployRequest;
import com.tencent.jflynn.dto.ScaleRequest;
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
	
	private static final Pattern PATTERN_TYPES = Pattern.compile(".*declares types -> (.*)");
	
	public void createApp(App app){
		appDao.insert(app);
	}
	
	public App getAppByName(String appName){
		return appDao.queryByName(appName);
	}
	
	public void deployApp(App app, DeployRequest req){
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
		if(req.getReleaseEnv() != null){
			for(Map.Entry<String, String> e : req.getReleaseEnv().entrySet()){
				release.getEnv().put(e.getKey(), e.getValue());
			}
		}
		
		//update process cmd 
		if(req.getProcessCmd() != null){
			for(Map.Entry<String, String> e : req.getProcessCmd().entrySet()){
				String procName = e.getKey();
				ProcessType procType = release.getProcesses().get(procName);
				if(procType == null){
					procType = new ProcessType();
					release.getProcesses().put(procName, procType);
				}
				procType.setCmd(e.getValue());
			}
		}
		
		//update process entrypoint
		if(req.getProcessEpt() != null){
			for(Map.Entry<String, String> e : req.getProcessEpt().entrySet()){
				String procName = e.getKey();
				ProcessType procType = release.getProcesses().get(procName);
				if(procType == null){
					procType = new ProcessType();
					release.getProcesses().put(procName, procType);
				}
				procType.setEntrypoint(e.getValue());
			}
		}
		
		//update process environment variables
		if(req.getProcessEnv() != null){
			for(Map.Entry<String, Map<String,String>> e : req.getProcessEnv().entrySet()){
				String procName = e.getKey();
				ProcessType procType = release.getProcesses().get(procName);
				if(procType == null){
					procType = new ProcessType();
					release.getProcesses().put(procName, procType);
				}
				for(Map.Entry<String, String> env : e.getValue().entrySet()){
					procType.getEnv().put(env.getKey(), env.getValue());
				}
			}
		}
		
		//if no processes in the release, create a "default" one
		if(release.getProcesses().size() == 0){
			release.getProcesses().put("default", new ProcessType());
		}
		
		releaseDao.insert(release);
		LOG.info("Created release for appName=" + app.getName() + " release=" + release);
		appDao.update(app);
		LOG.info("Updated appName=" + app.getName() + " set current releaseId=" + app.getReleaseID());
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
		release.getEnv().put("SLUG_URL", httpServerUrl + "/slugs/" + fileName + ".tgz");
		if(processTypes != null){
			for(String type : processTypes){
				type = type.trim();
				ProcessType ptype = new ProcessType();
				ptype.setCmd("start " + type);
				release.getProcesses().put(type, ptype);
			}
		}
	}
	
//	public void handleSvnDeploy(App app, Release release, DeployRequest req){
//		String fileName = app.getName() + "-" + System.currentTimeMillis();
//		
//		//svn export code and create tarball
//		String cmd = MessageFormat.format("docker run -i -v /tmp:/tmp -e SVN_URL={0} -e APP_NAME={1} {2}", 
//				req.getSvnURL(), fileName, svnImage);
//		ShellCommandExecutor.execute(cmd);
//		
//		//docker run slugbuilder to create slug.tgz
//		cmd = MessageFormat.format("cat /tmp/{0}.tar |  "
//				+ "docker run -i -e HTTP_SERVER_URL={1} "
//				+ "-a stdin -a stdout -a stderr "
//				+ "{2} {1}/slugs/{0}.tgz",
//				fileName, httpServerUrl, slugBuilderImage);
//		
//		String out = ShellCommandExecutor.execute("/bin/sh -c " + "\"" + cmd + "\"");
//		System.out.println(out);
//		//Grep output and extract process types
//		Matcher m = PATTERN_TYPES.matcher(out);
//		String [] processTypes = null;
//		if(m.find()){
//			processTypes = m.group(1).split(", ");
//		}
//		
//		//create artifact and release object
//		Artifact artifact = new Artifact();
//		artifact.setId(IdGenerator.generate());
//		artifact.setUri(slugRunnerImage);
//		artifact.setCreateTime(new Timestamp(System.currentTimeMillis()));
//		artifactDao.insert(artifact);
//		LOG.info("Created artifact for appName=" + app.getName() + " artifact=" + artifact);
//		
//		release.setArtifactID(artifact.getId());
//		release.getEnv().put("SLUG_URL", httpServerUrl + "/slugs/" + fileName + ".tgz");
//		if(processTypes != null){
//			for(String type : processTypes){
//				type = type.trim();
//				ProcessType ptype = new ProcessType();
//				ptype.setCmd("start " + type);
//				release.getProcesses().put(type, ptype);
//			}
//		}
//	}
	
	public void scaleApp(App app, Release release, ScaleRequest req){
		Formation formation = formationDao.queryByAppId(app.getId());
		if(formation == null){
			formation = new Formation();
			formation.setAppID(app.getId());
		}
		
		formation.setReleaseID(release.getId());
		for(Map.Entry<String,Integer> e : req.getProcessReplica().entrySet()){
			ProcessType ptype = release.getProcesses().get(e.getKey());
			if(ptype == null)continue;
			formation.getProcesses().put(e.getKey(), e.getValue());
		}
		formationDao.save(formation);
		
		//simulate replicationController and agent logic
		for(Map.Entry<String,Integer> e : req.getProcessReplica().entrySet()){
			for(int i=1;i<=e.getValue();i++){
				ProcessType ptype = release.getProcesses().get(e.getKey());
				if(ptype == null)continue;
				StringBuilder cmd = new StringBuilder();
				cmd.append("docker run -d -P ");
				if(ptype.getEntrypoint() != null){
					cmd.append(" -entrypoint " + ptype.getEntrypoint());
					cmd.append(" ");
				}
				for(Map.Entry<String,String> env : release.getEnv().entrySet()){
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
	
	
}
