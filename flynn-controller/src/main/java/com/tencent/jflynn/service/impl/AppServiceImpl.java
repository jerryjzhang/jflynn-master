package com.tencent.jflynn.service.impl;

import java.sql.Timestamp;
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
	
	private static final Pattern PATTERN_TYPES = Pattern.compile(".*declares types.* -> (.+) \n");
	
	public void createApp(App app){
		appDao.insert(app);
	}
	
	public App getAppByName(String appName){
		return appDao.queryByName(appName);
	}
	
	public static void main(String [] args){
		String out = "Procfile declares types -> web, db \n";
		Matcher m = PATTERN_TYPES.matcher(out);
		String [] processTypes = null;
		if(m.matches()){
			processTypes = m.group(1).split(", ");
			System.out.println(processTypes);
		}
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
		if(m.matches()){
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
		release.getEnv().put("SLUG_URL", httpServerUrl + "/slug/" + fileName + ".tgz");
		if(processTypes != null){
			for(String type : processTypes){
				type = type.trim();
				ProcessType ptype = new ProcessType();
				ptype.setCmd("start " + type);
				release.getProcesses().put(type, ptype);
			}
		}
	}
//	
//	public void deployApp(App app, AppRequest req){
//		String fileName = app.getName() + "-" + System.currentTimeMillis();
//		String appBasePath = "/tmp/" + fileName;
//		
//		//svn export code and create tarball
//		//String cmd = "svn export " + req.getSvnURL() + " " + appBasePath;
//		String cmd = "docker run -i -a stdout -a stderr -v /tmp:/tmp "
//				+ "-e SVN_URL=" + req.getSvnURL() + " -e APP_NAME=" + fileName + " " + svnImage;
//		ShellCommandExecutor.execute(cmd);
//		
//		//docker run slugbuilder to create slug.tgz
//		cmd = "cat " + appBasePath + ".tar";
//		PipedOutputStream out = new PipedOutputStream();
//		ShellCommandExecutor.execute(cmd, null, out);
//		PipedInputStream in = null;
//		try{
//		    in = new PipedInputStream(out);
//		}catch(IOException e){
//			e.printStackTrace();
//			return;
//		}
//		
//		cmd = "docker run -i -e HTTP_SERVER_URL=" + httpServerUrl + 
//				" -a stdin " + slugBuilderImage +
//				" - > " + appBasePath + ".tgz";
//		String output = ShellCommandExecutor.execute(cmd, in, null);
//		//Grep output and extract process types
//		Matcher m = PATTERN_TYPES.matcher(output);
//		String [] processTypes = null;
//		if(m.matches()){
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
//		Release release = new Release();
//		release.setId(IdGenerator.generate());
//		release.setAppID(app.getId());
//		release.setArtifactID(artifact.getId());
//		release.setTag(req.getComment());
//		release.getEnv().put("SLUG_URL", httpServerUrl + "/slug/" + fileName + ".tgz");
//		int lastestVersion = app.getLatestVersion();
//		release.setVersion(lastestVersion + 1);
//		if(processTypes != null){
//			for(String type : processTypes){
//				type = type.trim();
//				ProcessType ptype = new ProcessType();
//				ptype.setCmd("start " + type);
//				release.getProcesses().put(type, ptype);
//			}
//		}
//		releaseDao.insert(release);
//		LOG.info("Created release for appName=" + app.getName() + " release=" + release);
//		
//		//set app current release to the newly created release
//		app.setReleaseID(release.getId());
//		app.setLatestVersion(release.getVersion());
//		appDao.update(app);
//	}
	
	public void scaleApp(App app, Release release, Formation formation){
		formation.setAppID(app.getId());
		formation.setReleaseID(release.getId());
		formationDao.save(formation);
		
		for(Map.Entry<String,Integer> e : formation.getProcesses().entrySet()){
			for(int i=1;i<=e.getValue();i++){
				String cmd = "docker run -e SLUG_URL=" + release.getEnv().get("SLUG_URL") 
						+ " " + slugRunnerImage + " " + release.getProcesses().get(e.getKey()).getCmd();
				ShellCommandExecutor.execute(cmd);
			}
		}
	}
	
	public List<Release> getAppReleases(App app) {
		return releaseDao.queryByAppId(app.getId());
	}
	
	
}
