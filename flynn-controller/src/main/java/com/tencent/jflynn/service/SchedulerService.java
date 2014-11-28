package com.tencent.jflynn.service;

import com.tencent.jflynn.dto.scheduler.ScheduleRequest;

public interface SchedulerService {
	public void schedule(ScheduleRequest req);
	public void stopApp(String appName);
	public void stopProgram(String programName);
}
