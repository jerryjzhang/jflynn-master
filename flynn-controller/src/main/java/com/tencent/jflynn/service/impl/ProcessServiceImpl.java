package com.tencent.jflynn.service.impl;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
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
@Profile("production")
public class ProcessServiceImpl implements ProcessService {
	private static final Logger LOG = Logger.getLogger(ProcessServiceImpl.class);
			
	@Value("${scheduler.url}")
	private String schedulerUrl;
	
	@Autowired
	private ArtifactDao artifactDao;
	
	protected RestOperations restClient = new RestTemplate();
	
	public void schedule(String appName, ScheduleRequest req){
		boolean success = restClient.postForEntity(schedulerUrl+"/apps/scale", req, Boolean.class).getBody();
		LOG.info("Response from scheduler for ACTION schedule: " + success);
	}
	
	public void stop(String appName, ProcessRequest req){
		String url = schedulerUrl+"/apps/kill/"+appName;
		if(req != null && req.getProcessId() != null){
			url += "?processId="+req.getProcessId();
		}else if(req != null && req.getProgramName() != null){
			url += "?programName="+req.getProgramName();
		}
		boolean success = restClient.postForEntity(url, null, Boolean.class).getBody();
		LOG.info("Response from scheduler for ACTION stopApp: " + success);
	}
	
	public Process[] list(String appName, ProcessRequest req){
		Process[] processes = restClient.getForEntity(schedulerUrl+"/apps/process/"+appName, Process[].class).getBody();
		LOG.info("Response from scheduler for ACTION list: " + processes);
		return processes;
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
}
