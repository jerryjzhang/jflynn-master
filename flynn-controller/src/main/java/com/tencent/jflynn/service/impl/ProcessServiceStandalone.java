package com.tencent.jflynn.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.tencent.jflynn.domain.Process;
import com.tencent.jflynn.dto.ProcessRequest;
import com.tencent.jflynn.dto.scheduler.ExtendedProgram;
import com.tencent.jflynn.dto.scheduler.ScheduleRequest;
import com.tencent.jflynn.service.ProcessService;

@Service
@Profile("standalone")
public class ProcessServiceStandalone implements ProcessService {
	private static final Logger LOG = Logger.getLogger(ProcessServiceStandalone.class);
	final DockerClient docker = 
			new DefaultDockerClient("unix:///var/run/docker.sock");
	private Map<String, List<Process>> processMap = 
			new HashMap<String,List<Process>>();
	
	public void schedule(String appName, ScheduleRequest req){
		for(ExtendedProgram ep : req.getPrograms().values()){
			List<Process> runningProcesses = getProcesses(appName, ep.getProgram().getName());
			if(runningProcesses == null){
				runningProcesses = new ArrayList<Process>();
				processMap.put(appName, runningProcesses);
			}
			int actualSize = runningProcesses.size();
			int expectedSize = ep.getReplica();
			if(actualSize < expectedSize){
				LOG.info("creating new containers");
				//create new container
				for(int i=1;i<=expectedSize-actualSize;i++){
					// Create container
					final ContainerConfig config = ContainerConfig.builder()
					    .image(req.getImageUri())
					    .build();
					try{
						final ContainerCreation creation = docker.createContainer(config);
						final String id = creation.id();
						docker.startContainer(id);
						Process process = new Process();
						process.setAppName(appName);
						process.setProgramName(ep.getProgram().getName());
						process.setProcessId(id);
						runningProcesses.add(process);
					}catch(DockerException e){
						e.printStackTrace();
					}catch(InterruptedException e){
						e.printStackTrace();
					}
				}
			}else if(actualSize > expectedSize){
				LOG.info("killing existing containers");
				//kill running containers
				for(int i=1;i<=actualSize-expectedSize;i++){
					Process process = runningProcesses.remove(0);
					try{
						docker.killContainer(process.getProcessId());
					}catch(DockerException e){
						e.printStackTrace();
					}catch(InterruptedException e){
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void stop(String appName, ProcessRequest req){
	}
	
	public Process[] list(String appName, ProcessRequest req){
		List<Process> runningProcesses = getProcesses(appName, req.getProgramName());
		return runningProcesses.toArray(new Process[runningProcesses.size()]);
	}
	
	private List<Process> getProcesses(String appName, String programName){
		List<Process> processes = processMap.get(appName);
		if(processes != null && programName != null){
			Iterator<Process> itr = processes.iterator();
			while(itr.hasNext()){
				Process p = itr.next();
				if(!p.getProgramName().equals(programName)){
					itr.remove();
				}
			}
		}
		return processes;
	}
}
