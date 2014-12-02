package com.tencent.jflynn.controller;

import static org.junit.Assert.*;

import java.util.HashMap;

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
import com.tencent.jflynn.domain.Release;
import com.tencent.jflynn.dto.ReleaseRequest;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = JFlynnMain.class)
@WebAppConfiguration
@IntegrationTest("CONFIG_MODE:DEV")
public class ReleaseIntegrationTest {
	private RestTemplate restTemplate = new TestRestTemplate();
	private final String appName = "myapp";
	private final String baseURL = "http://localhost:58080";
	
	public ReleaseIntegrationTest(){
		System.setProperty("CONFIG_MODE", "DEV");
	}
	
	@Before
	public void setUp(){
		//create app
		restTemplate.postForEntity(baseURL+"/apps/create/" + appName, 
				null, String.class).getBody();
	}
	
	@After
	public void tearsDown(){
		restTemplate.delete(baseURL+"/apps/delete/" + appName);
	}
	
	@Test
	public void testList(){
		//deploy app
		ReleaseRequest req = new ReleaseRequest();
		req.setImageURI("tegdsf/routercenter");
		req.setComment("deploy new code");
		restTemplate.postForEntity(baseURL+"/apps/deploy/"+appName, req, Void.class);
		//should return one release under this app
		Release[] releases = restTemplate.getForEntity(baseURL+"/releases/list/app/"+appName, Release[].class).getBody();
		assertNotNull(releases);
		assertEquals(1, releases.length);
		
		//redeploy with added env variables
		req = new ReleaseRequest();
		req.setAppEnv(new HashMap<String,String>());
		req.getAppEnv().put("DB_URL", "http://localhost:3006");
		restTemplate.postForEntity(baseURL+"/apps/deploy/"+appName, req, Void.class);
		//should return two releases under this app
		releases = restTemplate.getForEntity(baseURL+"/releases/list/app/"+appName, Release[].class).getBody();
		assertNotNull(releases);
		assertEquals(2, releases.length);
	}
	
	@Test
	public void testRollback(){
		//deploy app
		ReleaseRequest req = new ReleaseRequest();
		req.setImageURI("tegdsf/routercenter");
		req.setComment("deploy new code");
		req.setAppEnv(new HashMap<String,String>());
		req.getAppEnv().put("VERSION", "1.0");
		restTemplate.postForEntity(baseURL+"/apps/deploy/"+appName, req, Void.class);
		//should return one release
		Release[] releases = restTemplate.getForEntity(baseURL+"/releases/list/app/"+appName, Release[].class).getBody();
		assertNotNull(releases);
		assertEquals(1, releases.length);
		//current active release is of version=1
		Release activeRel = restTemplate.getForEntity(baseURL+"/releases/get/app/"+appName, Release.class).getBody();
		assertEquals(1, activeRel.getVersion());
		assertEquals("1.0", activeRel.getAppEnv().get("VERSION"));
		
		//redeploy app
		req = new ReleaseRequest();
		req.setImageURI("tegdsf/routercenter");
		req.setComment("deploy new code");
		req.setAppEnv(new HashMap<String,String>());
		req.getAppEnv().put("VERSION", "2.0");
		restTemplate.postForEntity(baseURL+"/apps/deploy/"+appName, req, Void.class);
		//should return two releases
		releases = restTemplate.getForEntity(baseURL+"/releases/list/app/"+appName, Release[].class).getBody();
		assertNotNull(releases);
		assertEquals(2, releases.length);
		//current active release is of version=2
		activeRel = restTemplate.getForEntity(baseURL+"/releases/get/app/"+appName, Release.class).getBody();
		assertEquals(2, activeRel.getVersion());
		assertEquals("2.0", activeRel.getAppEnv().get("VERSION"));
		
		//rollback to version 1
		String releaseId = restTemplate.postForEntity(baseURL+"/releases/rollback/app/"+appName+"/version/1",
				null, String.class).getBody();
		assertNotNull(releaseId);
		//should return three release
		releases = restTemplate.getForEntity(baseURL+"/releases/list/app/"+appName, Release[].class).getBody();
		assertNotNull(releases);
		assertEquals(3, releases.length);
		//current active release is of version=3, but the content is identical to that of version=1
		activeRel = restTemplate.getForEntity(baseURL+"/releases/get/app/"+appName, Release.class).getBody();
		assertEquals(3, activeRel.getVersion());
		assertEquals("1.0", activeRel.getAppEnv().get("VERSION"));
	}
}
