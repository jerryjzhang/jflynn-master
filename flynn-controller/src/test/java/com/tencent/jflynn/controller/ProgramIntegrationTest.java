package com.tencent.jflynn.controller;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import com.tencent.jflynn.boot.JFlynnMain;
import com.tencent.jflynn.domain.Program;
import com.tencent.jflynn.dto.ReleaseRequest;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = JFlynnMain.class)
@WebAppConfiguration
@IntegrationTest("spring.profiles.active:test")
public class ProgramIntegrationTest {
	private RestTemplate restTemplate = new TestRestTemplate();
	private final String appName = "myapp";
	private final String baseURL = "http://localhost:58080";
	
	public ProgramIntegrationTest(){
		System.setProperty("CONFIG_MODE", "DEV");
	}
	
	@Before
	public void setUp(){
		//create app
		restTemplate.postForEntity(baseURL+"/apps/create/" + appName, 
				null, String.class).getBody();
		//deploy app
		ReleaseRequest req = new ReleaseRequest();
		req.setImageURI("tegdsf/routercenter");
		req.setComment("deploy new code");
		restTemplate.postForEntity(baseURL+"/apps/deploy/"+appName, req, Void.class);
	}
	
	@After
	public void tearsDown(){
		restTemplate.delete(baseURL+"/apps/delete/" + appName);
	}
	
	@Test
	public void testSaveProgram(){
		Program program = new Program();
		program.setName("web");
		program.setCmd("start web");
		program.getEnv().put("DB_URL", "http://localhost:3006");
		String releaseId = restTemplate.postForEntity(baseURL+"/programs/save/app/" + appName, 
				program, String.class).getBody();
		assertNotNull(releaseId);
		
		Program[] programs = restTemplate.getForEntity(baseURL+"/programs/list/app/"+appName, Program[].class).getBody();
		assertNotNull(programs);
		assertEquals(2, programs.length);
		Program retProgram = null;
		for(Program p : programs){
			if(program.getName().equals(p.getName())){
				retProgram = p;
				break;
			}
		}
		assertEquals(program.getName(), retProgram.getName());
		assertEquals(program.getCmd(), retProgram.getCmd());
		assertEquals(program.getEntrypoint(), retProgram.getEntrypoint());
		assertEquals(program.getEnv().get("DB_URL"), retProgram.getEnv().get("DB_URL"));
	}
	
	@Test
	public void testDeleteProgram(){
		Program program = new Program();
		program.setName("web");
		program.setCmd("start web");
		program.getEnv().put("DB_URL", "http://localhost:3006");
		String releaseId = restTemplate.postForEntity(baseURL+"/programs/save/app/" + appName, 
				program, String.class).getBody();
		assertNotNull(releaseId);
		
		restTemplate.delete(baseURL+"/programs/delete/app/"+appName+"/program/"+program.getName());
		Program[] programs = restTemplate.getForEntity(baseURL+"/programs/list/app/"+appName, Program[].class).getBody();
		assertNotNull(programs);
		assertTrue(programs.length == 1);
	}
}
