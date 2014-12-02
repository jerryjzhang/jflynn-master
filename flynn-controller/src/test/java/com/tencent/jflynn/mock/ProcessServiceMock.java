package com.tencent.jflynn.mock;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import com.tencent.jflynn.dto.ProcessRequest;
import com.tencent.jflynn.dto.scheduler.ScheduleRequest;
import com.tencent.jflynn.service.impl.ProcessServiceImpl;
import com.tencent.jflynn.domain.Process;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProcessServiceMock extends ProcessServiceImpl{
	public ProcessServiceMock(){
		super();
		restClient = mock(RestOperations.class);
		when(restClient.postForEntity(isA(String.class), isA(ScheduleRequest.class), eq(Boolean.class))).thenReturn(
				new ResponseEntity<Boolean>(true ,HttpStatus.OK));
		when(restClient.postForEntity(isA(String.class), eq(null), eq(Boolean.class))).thenReturn(
				new ResponseEntity<Boolean>(true ,HttpStatus.OK));
		
		Process[] processes = new Process[1];
		Process process = new Process();
		process.setAppName("dsf");
		process.setNodeHost("localhost");
		process.setNodePort("8080");
		processes[0] = process;
		when(restClient.postForEntity(isA(String.class), isA(ProcessRequest.class), eq(Process[].class))).thenReturn(
				new ResponseEntity<Process[]>(processes ,HttpStatus.OK));
	}
}
