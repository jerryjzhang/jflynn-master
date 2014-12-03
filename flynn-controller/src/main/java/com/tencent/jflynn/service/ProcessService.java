package com.tencent.jflynn.service;

import com.tencent.jflynn.dto.ProcessRequest;
import com.tencent.jflynn.domain.Process;
import com.tencent.jflynn.dto.scheduler.ScheduleRequest;

public interface ProcessService {
	public void schedule(String appName, ScheduleRequest req);
	public void stop(String appName, ProcessRequest req);
	public Process[] list(String appName, ProcessRequest req);
}
