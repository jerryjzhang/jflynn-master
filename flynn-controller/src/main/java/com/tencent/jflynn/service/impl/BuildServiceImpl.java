package com.tencent.jflynn.service.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.tencent.jflynn.dto.BuildRequest;
import com.tencent.jflynn.dto.BuildResponse;
import com.tencent.jflynn.service.BuildService;
import com.tencent.jflynn.utils.ShellCommandExecutor;

@Service
@Profile("production")
public class BuildServiceImpl implements BuildService {
	private static final Logger LOG = Logger.getLogger(BuildServiceImpl.class);

	@Value("${blobstore.url}")
	private String httpServerUrl;
	@Value("${svnImage:tegdsf/svn}")
	private String svnImage;
	@Value("${slugBuilderImage:tegdsf/slugbuilder}")
	private String slugBuilderImage;
	@Value("${slugRunnerImage:tegdsf/slugrunner}")
	private String slugRunnerImage;
	@Value("${slugBuildScript:slugBuild.sh}")
	private String slugBuildScript;
	
	public BuildResponse buildImage(BuildRequest req){
		req.getContext().put("IMAGE_SVN", svnImage);
		req.getContext().put("HTTP_SERVER_URL", httpServerUrl);
		req.getContext().put("IMAGE_SLUGBUILDER", slugBuilderImage);
		
		String cmd = slugBuildScript;
		String out = ShellCommandExecutor.execute(cmd, req.getContext());
		LOG.info("Successfully built image for appName="+req.getAppName()+"\n" + out);
		
		return new BuildResponse(slugRunnerImage, out);
	}
}
