package com.tencent.jflynn.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.tencent.jflynn.dto.BuildRequest;
import com.tencent.jflynn.dto.BuildResponse;
import com.tencent.jflynn.service.BuildService;

@Service
@Profile("test")
public class BuildServiceMock implements BuildService {
	@Value("${slugRunnerImage:tegdsf/slugrunner}")
	private String slugRunnerImage;
	
	public BuildResponse buildImage(BuildRequest req){
		return new BuildResponse(slugRunnerImage, "Procfile declares types -> web, db \n");
	}
}
