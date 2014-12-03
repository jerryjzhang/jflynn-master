package com.tencent.jflynn.service;


import com.tencent.jflynn.dto.BuildRequest;
import com.tencent.jflynn.dto.BuildResponse;

public interface BuildService {
	public BuildResponse buildImage(BuildRequest req);
}
