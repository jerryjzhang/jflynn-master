package com.tencent.jflynn.service.impl;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.tencent.jflynn.dao.ArtifactDao;
import com.tencent.jflynn.dao.FormationDao;
import com.tencent.jflynn.domain.Program;
import com.tencent.jflynn.dto.scheduler.ExtendedProgram;
import com.tencent.jflynn.dto.scheduler.ScheduleRequest;
import com.tencent.jflynn.service.SchedulerService;
import com.tencent.jflynn.utils.ShellCommandExecutor;

@Service
public class SchedulerServiceImpl implements SchedulerService {
	private static final Logger LOG = Logger.getLogger(SchedulerServiceImpl.class);
			
	@Value("${schedulerUrl}")
	private String schedulerUrl;
	@Value("${workMode:standalone}")
	private String workMode;
	
	@Autowired
	private ArtifactDao artifactDao;
	@Autowired
	private FormationDao formationDao;
	
	private RestTemplate restTemplate = new RestTemplate();
	
	public void schedule(ScheduleRequest req){
		if("standalone".equals(workMode)){
			scheduleLocal(req);
			return;
		}
		boolean success = restTemplate.postForEntity(schedulerUrl+"/apps/scale", req, Boolean.class).getBody();
		LOG.info("Response from scheduler for ACTION schedule: " + success);
	}
	
	public void stopApp(String appName){
		boolean success = restTemplate.postForEntity(schedulerUrl+"/apps/kill/"+appName, null, Boolean.class).getBody();
		LOG.info("Response from scheduler for ACTION stopApp: " + success);
	}
	
	public void stopProgram(String programName){
		boolean success = restTemplate.postForEntity(schedulerUrl+"/programs/kill/"+programName, null, Boolean.class).getBody();
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
}
