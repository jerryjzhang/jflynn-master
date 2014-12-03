package com.tencent.jflynn.service.impl;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import com.tencent.jflynn.dao.ArtifactDao;
import com.tencent.jflynn.domain.Process;
import com.tencent.jflynn.domain.Program;
import com.tencent.jflynn.dto.ProcessRequest;
import com.tencent.jflynn.dto.scheduler.ExtendedProgram;
import com.tencent.jflynn.dto.scheduler.ScheduleRequest;
import com.tencent.jflynn.service.ProcessService;
import com.tencent.jflynn.utils.ShellCommandExecutor;

@Service
public class ProcessServiceImpl implements ProcessService {
	private static final Logger LOG = Logger.getLogger(ProcessServiceImpl.class);
			
	@Value("${schedulerUrl}")
	private String schedulerUrl;
	@Value("${workMode:standalone}")
	private String workMode;
	
	@Autowired
	private ArtifactDao artifactDao;
	
	protected RestOperations restClient = new RestTemplate();
	
	public void schedule(String appName, ScheduleRequest req){
//		if("standalone".equals(workMode)){
//			scheduleLocal(req);
//			return;
//		}
		boolean success = restClient.postForEntity(schedulerUrl+"/apps/scale", req, Boolean.class).getBody();
		LOG.info("Response from scheduler for ACTION schedule: " + success);
	}
	
	public void stop(String appName, ProcessRequest req){
		if(req == null || req.isEmpty()){
			stopByApp(appName);
		}else if(req.getProcessId() != null){
			stop(req.getProcessId());
		}else if(req.getProgramName() != null){
			stopByProgram(appName, req.getProgramName());
		}
	}
	
	public Process[] list(String appName, ProcessRequest req){
		Process[] processes = restClient.getForEntity(schedulerUrl+"/apps/process/"+appName, Process[].class).getBody();
		LOG.info("Response from scheduler for ACTION list: " + processes);
		return processes;
	}
	
	public void stopByApp(String appName){
		boolean success = restClient.postForEntity(schedulerUrl+"/apps/kill/"+appName, null, Boolean.class).getBody();
		LOG.info("Response from scheduler for ACTION stopApp: " + success);
	}
	
	public void stopByProgram(String appName, String programName){
		boolean success = restClient.postForEntity(schedulerUrl+"/programs/kill/"+appName+"/"+programName, null, Boolean.class).getBody();
		LOG.info("Response from scheduler for ACTION stopProgram: " + success);
	}
	
	public void scheduleLocal(ScheduleRequest req){
		//simulate replicationController and agent logic
		for(Map.Entry<String,ExtendedProgram> e : req.getPrograms().entrySet()){
			String programName = e.getKey();
			ExtendedProgram program = e.getValue();
			for(int i=1;i<=program.getReplica();i++){
				Program ptype = program.getProgram();
				if(ptype == null)continue;
				StringBuilder cmd = new StringBuilder();
				cmd.append("docker run -d -P -name ");
				cmd.append(programName + i);
				cmd.append(" ");
				if(ptype.getEntrypoint() != null){
					cmd.append(" -entrypoint " + ptype.getEntrypoint());
					cmd.append(" ");
				}
				for(Map.Entry<String,String> env : req.getAppEnv().entrySet()){
					cmd.append(" -e " + env.getKey() + "=" + env.getValue());
					cmd.append(" ");
				}
				for(Map.Entry<String,String> env : ptype.getEnv().entrySet()){
					cmd.append(" -e " + env.getKey() + "=" + env.getValue());
					cmd.append(" ");
				}
				cmd.append(" " + req.getImageUri() + " ");
				if(ptype.getCmd() != null){
					cmd.append(" " + ptype.getCmd());
				}
				ShellCommandExecutor.execute(cmd.toString());
			}
		}
	}

	public void stop(String processId) {
		// TODO Auto-generated method stub
		
	}
}
