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
import com.tencent.jflynn.domain.Formation;
import com.tencent.jflynn.domain.Release;
import com.tencent.jflynn.dto.DeployRequest;
import com.tencent.jflynn.dto.ScaleRequest;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = JFlynnMain.class)
@WebAppConfiguration
@IntegrationTest("CONFIG_MODE:DEV")
public class AppIntegrationTest {
	private RestTemplate restTemplate = new TestRestTemplate();
	private final String appName = "myapp";
	private final String baseURL = "http://localhost:58080";
	
	public AppIntegrationTest(){
		System.setProperty("CONFIG_MODE", "DEV");
	}
	
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
		assertTrue(release.getPrograms().size() >= 1);
		assertNotNull(release.getAppEnv().get("SLUG_URL"));
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
		assertTrue(release.getPrograms().size() >= 1);
		assertNull(release.getAppEnv().get("SLUG_URL"));
	}
	
	@Test
	public void testDeployAppWithRelEnv(){
		//deploy app
		DeployRequest req = new DeployRequest();
		req.setDockerImage("tegdsf/routercenter");
		req.setAppEnv(new HashMap<String,String>());
		req.getAppEnv().put("URL", "http://dsf");
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
		assertTrue(release.getPrograms().size() >= 1);
		assertEquals(req.getAppEnv().get("URL"),
				release.getAppEnv().get("URL"));
	}
	
	@Test
	public void testDeployAppWithProcCmd(){
		//deploy app
		DeployRequest req = new DeployRequest();
		req.setDockerImage("tegdsf/routercenter");
		req.setProgramCmd(new HashMap<String,String>());
		req.getProgramCmd().put("web", "new cmd");
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
		assertTrue(release.getPrograms().size() >= 1);
		assertEquals(req.getProgramCmd().get("web"),
				release.getPrograms().get("web").getCmd());
	}
	
	@Test
	public void testDeployAppWithProcEpt(){
		//deploy app
		DeployRequest req = new DeployRequest();
		req.setDockerImage("tegdsf/routercenter");
		req.setProgramEpt(new HashMap<String,String>());
		req.getProgramEpt().put("web", "new entrypoint");
		
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
		assertTrue(release.getPrograms().size() >= 1);
		assertEquals(req.getProgramEpt().get("web"),
				release.getPrograms().get("web").getEntrypoint());
	}
	
	@Test
	public void testDeployAppWithProcEnv(){
		//deploy app
		DeployRequest req = new DeployRequest();
		req.setDockerImage("tegdsf/routercenter");
		req.setProgramEnv(new HashMap<String, Map<String,String>>());
		req.getProgramEnv().put("web", new HashMap<String,String>());
		req.getProgramEnv().get("web").put("URL", "http://dsf");
		
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
		assertTrue(release.getPrograms().size() >= 1);
		assertEquals(req.getProgramEnv().get("web").get("URL"),
				release.getPrograms().get("web").getEnv().get("URL"));
	}
	
	@Test
	public void testScaleApp(){
		//deploy app
		DeployRequest req = new DeployRequest();
		req.setDockerImage("tegdsf/routercenter");
		req.setProgramEnv(new HashMap<String, Map<String,String>>());
		req.getProgramEnv().put("web", new HashMap<String,String>());
		req.getProgramEnv().get("web").put("URL", "http://dsf");
		restTemplate.postForEntity(baseURL+"/apps/deploy/"+appName, req, Void.class);
		
		//scale app
		ScaleRequest sreq = new ScaleRequest();
		sreq.setProgramReplica(new HashMap<String,Integer>());
		sreq.getProgramReplica().put("web", 1);
		restTemplate.postForEntity(baseURL+"/apps/scale/"+appName, sreq, Void.class);
		
		//get app formation and check
		Formation formation = restTemplate.getForEntity(baseURL+"/formations/get/app/"+appName, Formation.class).getBody();
		assertNotNull(formation);
		assertNotNull(formation.getAppID());
		assertNotNull(formation.getReleaseID());
		assertTrue(formation.getProgramReplica().size() > 0);
		assertEquals(sreq.getProgramReplica().get("web"), 
				formation.getProgramReplica().get("web"));
	}
}
