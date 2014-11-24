package com.tencent.jflynn.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;

public class ShellCommandExecutor {
	public static String execute(String cmd){
		System.out.println(cmd);
		return "types -> web, db \n";
		//return execute(cmd, null);
	}
	
	public static String execute(String cmd, File workDir){
		Executor exec = new DefaultExecutor();
		
		CommandLine cl = CommandLine.parse(cmd);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PumpStreamHandler handler = new PumpStreamHandler(os);
		exec.setStreamHandler(handler);
		if(workDir != null){
			exec.setWorkingDirectory(workDir);
		}
		try{
			int ret = exec.execute(cl);
			if(ret != 0){
				throw new RuntimeException("Exit code is " + ret);
			}
		}catch(ExecuteException e){
			throw new RuntimeException("Exception occurred: " + e.getMessage());
		}catch(IOException e){
			throw new RuntimeException("Exception occurred: " + e.getMessage());
		}
		return os.toString();
	}
}
