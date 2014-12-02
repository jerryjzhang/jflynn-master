package com.tencent.jflynn.controller;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.tencent.jflynn.mock.ProcessServiceMock;
import com.tencent.jflynn.service.ProcessService;
import com.tencent.jflynn.dto.ProcessRequest;

public class ProcessIntegrationTest extends BaseIntegrationTest {
	@Configuration
    public static class TestConfiguration {
        @Bean
        @Primary
        public ProcessService processService() {
            return new ProcessServiceMock();
        }
    }
	
	@Test
	public void testList(){
		String a = restTemplate.postForEntity(baseURL+"/processes/list/" + appName, 
				new ProcessRequest(), String.class).getBody();
		assertNotNull(a);
	}
	
	
}
