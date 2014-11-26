package com.tencent.jflynn.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;

public class ShellCommandExecutor {
	public static String execute(String cmd){
		return execute(cmd, null, null, null, null);
	}
	
	public static String execute(String cmd, Map<String,String> env){
		return execute(cmd, null, null, null, env);
	}
	
	public static String execute(String cmd, File workDir){
		return execute(cmd, workDir, null, null, null);
	}
	
	public static String execute(String cmd, InputStream in,  OutputStream out){
		return execute(cmd, null, in, out, null);
	}
	
	public static String execute(String cmd, File workDir, InputStream in, OutputStream out, Map<String,String> env){
		if("DEV".equals(System.getenv("CONFIG_MODE"))
				|| "DEV".equals(System.getProperty("CONFIG_MODE"))){
			CommandLine cl = CommandLine.parse(cmd);
			System.out.println("Executing... " + cl);
			return "Procfile declares types -> web, db \n";
		}
		
		Executor exec = new DefaultExecutor();
		
		CommandLine cl = CommandLine.parse(cmd);
		System.out.println("Executing... " + cl);
		if(out == null){
			out = new ByteArrayOutputStream();
		}
		PumpStreamHandler handler = new PumpStreamHandler(out, null, in);
		exec.setStreamHandler(handler);
		if(workDir != null){
			exec.setWorkingDirectory(workDir);
		}
		try{
			int ret = exec.execute(cl, env);
			if(ret != 0){
				throw new RuntimeException("Exit code is " + ret);
			}
		}catch(ExecuteException e){
			throw new RuntimeException("Exception occurred: " + e.getMessage());
		}catch(IOException e){
			throw new RuntimeException("Exception occurred: " + e.getMessage());
		}
		return out.toString();
	}
}
