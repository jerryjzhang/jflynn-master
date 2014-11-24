package com.tencent.jflynn.service.impl;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tencent.jflynn.dao.AppDao;
import com.tencent.jflynn.dao.ArtifactDao;
import com.tencent.jflynn.dao.ReleaseDao;
import com.tencent.jflynn.domain.App;
import com.tencent.jflynn.domain.Artifact;
import com.tencent.jflynn.domain.Formation;
import com.tencent.jflynn.domain.ProcessType;
import com.tencent.jflynn.domain.Release;
import com.tencent.jflynn.dto.AppRequest;
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
	
	private static final Pattern PATTERN_TYPES = Pattern.compile("types.* -> (.+)\n");
	
	public void createApp(App app){
		appDao.insert(app);
	}
	
	public App getAppByName(String appName){
		return appDao.queryByName(appName);
	}
	
	public void deployApp(App app, AppRequest req){
		String appBasePath = "/tmp/" + app.getName() + "-" + System.currentTimeMillis();
		
		//svn export code and create tarball
		String cmd = "svn export " + req.getSvnURL() + " " + appBasePath;
		ShellCommandExecutor.execute(cmd);
		cmd = "tar cvf " + appBasePath + ".tar --directory=" + appBasePath + " .";
		ShellCommandExecutor.execute(cmd);
		
		//docker run slugbuilder to create slug.tgz
		cmd = "cat " + appBasePath + ".tar | docker run -i -v /tmp/buildpacks:/tmp/buildpacks " +
				"-e HTTP_SERVER_URL=http://192.168.19.131:8000 -a stdin -a stdout -a stderr flynn/slugbuilder " +
				"- > " + appBasePath + ".tgz";
		String output = ShellCommandExecutor.execute(cmd);
		//Grep output and extract process types
		Matcher m = PATTERN_TYPES.matcher(output);
		String [] processTypes = null;
		if(m.matches()){
			processTypes = m.group(1).split(", ");
		}
		
		//create artifact and release object
		Artifact artifact = new Artifact();
		artifact.setId(IdGenerator.generate());
		artifact.setUri("flynn/slugrunner");
		artifact.setCreateTime(new Timestamp(System.currentTimeMillis()));
		artifactDao.insert(artifact);
		LOG.info("Created artifact for appName=" + app.getName() + " artifact=" + artifact);
		
		Release release = new Release();
		release.setId(IdGenerator.generate());
		release.setAppID(app.getId());
		release.setArtifactID(artifact.getId());
		release.setTag(req.getComment());
		release.getEnv().put("SLUG_URL", "http://192.168.19.131:8000/slug"+appBasePath+".tgz");
		int lastestVersion = app.getLatestVersion();
		release.setVersion(lastestVersion + 1);
		if(processTypes != null){
			for(String type : processTypes){
				type = type.trim();
				ProcessType ptype = new ProcessType();
				ptype.setCmd("start " + type);
				release.getProcesses().put(type, ptype);
			}
		}
		releaseDao.insert(release);
		LOG.info("Created release for appName=" + app.getName() + " release=" + release);
		
		//set app current release to the newly created release
		app.setReleaseID(release.getId());
		app.setLatestVersion(release.getVersion());
		appDao.update(app);
	}
	
	public void scaleApp(App app, Release release, Formation formation){
		for(Map.Entry<String,Integer> e : formation.getProcesses().entrySet()){
			for(int i=1;i<=e.getValue();i++){
				String cmd = "docker run -e SLUG_URL=" + release.getEnv().get("SLUG_URL") 
						+ " flynn/slugrunner " + release.getProcesses().get(e.getKey()).getCmd();
				ShellCommandExecutor.execute(cmd);
			}
		}
	}
	
	public List<Release> getAppReleases(App app) {
		return releaseDao.queryByAppId(app.getId());
	}
	
	
}
