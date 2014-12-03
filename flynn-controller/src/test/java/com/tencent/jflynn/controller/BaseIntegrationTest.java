package com.tencent.jflynn.controller;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import com.tencent.jflynn.boot.JFlynnMain;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringApplicationConfiguration(classes = JFlynnMain.class)
@IntegrationTest("spring.profiles.active:test")
public abstract class BaseIntegrationTest {
	protected RestTemplate restTemplate = new TestRestTemplate();
	protected final String appName = "myapp";
	protected final String baseURL = "http://localhost:58080";
	
	protected MockMvc mockMvc;

	@Autowired
	protected WebApplicationContext wac;
	
	public BaseIntegrationTest(){
		System.setProperty("CONFIG_MODE", "DEV");
	}

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.mockMvc = webAppContextSetup(wac).build();
	}
}
