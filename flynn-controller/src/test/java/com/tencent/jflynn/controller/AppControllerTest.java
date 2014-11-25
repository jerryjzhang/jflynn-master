package com.tencent.jflynn.controller;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

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
import com.tencent.jflynn.domain.App;
import com.tencent.jflynn.domain.Release;
import com.tencent.jflynn.dto.DeployRequest;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = JFlynnMain.class)
@WebAppConfiguration
@IntegrationTest("CONFIG_MODE:DEV")
public class AppControllerTest {
	private RestTemplate restTemplate = new TestRestTemplate();
	private final String baseURL = "http://localhost:8080";
	
	public AppControllerTest(){
		
	}
	
	private final String appName = "myapp";
	
	@Before
	public void init(){
		//create app
		restTemplate.postForEntity(baseURL+"/apps/create/" + appName, 
				null, String.class).getBody();
	}
	
	@Test
	public void testCreateApp(){
		//create app
		String appName = "new-myapp";
		String id = restTemplate.postForEntity(baseURL+"/apps/create/" + appName, 
				null, String.class).getBody();
		assertNotNull(id);
		//get app and check
		App app = restTemplate.getForEntity(baseURL+"/apps/get/" + appName, App.class).getBody();
		assertNotNull(app);
		assertEquals(appName, app.getName());
	}
	
	@Test
	public void testDeployAppWithSVN(){
		//deploy app
		DeployRequest req = new DeployRequest();
		req.setSvnURL("http://svn.com");
		req.setComment("deploy new code");
		restTemplate.postForEntity(baseURL+"/apps/deploy/"+appName, req, Void.class);
		
		//get app releases and check
		Release[] releases = restTemplate.getForEntity(baseURL+"/releases/get/app/"+appName, Release[].class).getBody();
		assertNotNull(releases);
		assertEquals(1, releases.length);
		Release release = releases[0];
		assertNotNull(release.getArtifactID());
		assertNotNull(release.getAppID());
		assertNotNull(release.getId());
		assertEquals(1, release.getVersion());
		assertEquals(req.getComment(), release.getTag());
		assertTrue(release.getProcesses().size() >= 1);
		assertNotNull(release.getEnv().get("SLUG_URL"));
	}
	
	@Test
	public void testDeployAppWithImage(){
		//deploy app
		DeployRequest req = new DeployRequest();
		req.setDockerImage("tegdsf/routercenter");
		req.setComment("deploy new code");
		restTemplate.postForEntity(baseURL+"/apps/deploy/"+appName, req, Void.class);
		
		//get app releases and check
		Release[] releases = restTemplate.getForEntity(baseURL+"/releases/get/app/"+appName, Release[].class).getBody();
		assertNotNull(releases);
		assertEquals(1, releases.length);
		Release release = releases[0];
		assertNotNull(release.getArtifactID());
		assertNotNull(release.getAppID());
		assertNotNull(release.getId());
		assertEquals(1, release.getVersion());
		assertEquals(req.getComment(), release.getTag());
		assertTrue(release.getProcesses().size() >= 1);
		assertNull(release.getEnv().get("SLUG_URL"));
	}
	
	@Test
	public void testDeployAppWithRelEnv(){
		//deploy app
		DeployRequest req = new DeployRequest();
		req.setDockerImage("tegdsf/routercenter");
		req.setReleaseEnv(new HashMap<String,String>());
		req.getReleaseEnv().put("URL", "http://dsf");
		restTemplate.postForEntity(baseURL+"/apps/deploy/"+appName, req, Void.class);
		
		//get app releases and check
		Release[] releases = restTemplate.getForEntity(baseURL+"/releases/get/app/"+appName, Release[].class).getBody();
		assertNotNull(releases);
		assertEquals(1, releases.length);
		Release release = releases[0];
		assertNotNull(release.getArtifactID());
		assertNotNull(release.getAppID());
		assertNotNull(release.getId());
		assertEquals(1, release.getVersion());
		assertTrue(release.getProcesses().size() >= 1);
		assertEquals(req.getReleaseEnv().get("URL"),
				release.getEnv().get("URL"));
	}
	
	@Test
	public void testDeployAppWithProcCmd(){
		//deploy app
		DeployRequest req = new DeployRequest();
		req.setDockerImage("tegdsf/routercenter");
		req.setProcessCmd(new HashMap<String,String>());
		req.getProcessCmd().put("web", "new cmd");
		restTemplate.postForEntity(baseURL+"/apps/deploy/"+appName, req, Void.class);
		
		//get app releases and check
		Release[] releases = restTemplate.getForEntity(baseURL+"/releases/get/app/"+appName, Release[].class).getBody();
		assertNotNull(releases);
		assertEquals(1, releases.length);
		Release release = releases[0];
		assertNotNull(release.getArtifactID());
		assertNotNull(release.getAppID());
		assertNotNull(release.getId());
		assertEquals(1, release.getVersion());
		assertTrue(release.getProcesses().size() >= 1);
		assertEquals(req.getProcessCmd().get("web"),
				release.getProcesses().get("web").getCmd());
	}
	
	@Test
	public void testDeployAppWithProcEpt(){
		//deploy app
		DeployRequest req = new DeployRequest();
		req.setDockerImage("tegdsf/routercenter");
		req.setProcessEpt(new HashMap<String,String>());
		req.getProcessEpt().put("web", "new entrypoint");
		
		restTemplate.postForEntity(baseURL+"/apps/deploy/"+appName, req, Void.class);
		//get app releases and check
		Release[] releases = restTemplate.getForEntity(baseURL+"/releases/get/app/"+appName, Release[].class).getBody();
		assertNotNull(releases);
		assertEquals(1, releases.length);
		Release release = releases[0];
		assertNotNull(release.getArtifactID());
		assertNotNull(release.getAppID());
		assertNotNull(release.getId());
		assertEquals(1, release.getVersion());
		assertTrue(release.getProcesses().size() >= 1);
		assertEquals(req.getProcessEpt().get("web"),
				release.getProcesses().get("web").getEntrypoint());
	}
	
	@Test
	public void testDeployAppWithProcEnv(){
		//deploy app
		DeployRequest req = new DeployRequest();
		req.setDockerImage("tegdsf/routercenter");
		req.setProcessEnv(new HashMap<String, Map<String,String>>());
		req.getProcessEnv().put("web", new HashMap<String,String>());
		req.getProcessEnv().get("web").put("URL", "http://dsf");
		
		restTemplate.postForEntity(baseURL+"/apps/deploy/"+appName, req, Void.class);
		//get app releases and check
		Release[] releases = restTemplate.getForEntity(baseURL+"/releases/get/app/"+appName, Release[].class).getBody();
		assertNotNull(releases);
		assertEquals(1, releases.length);
		Release release = releases[0];
		assertNotNull(release.getArtifactID());
		assertNotNull(release.getAppID());
		assertNotNull(release.getId());
		assertEquals(1, release.getVersion());
		assertTrue(release.getProcesses().size() >= 1);
		assertEquals(req.getProcessEnv().get("web").get("URL"),
				release.getProcesses().get("web").getEnv().get("URL"));
	}
}
