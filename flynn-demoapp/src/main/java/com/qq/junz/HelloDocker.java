package com.qq.junz;

public class HelloDocker {
	public static void main(String [] args){
		System.out.println("HelloDocker");
		try{
			Thread.sleep(1000000L);
		}catch(Exception e){
			System.exit(0);
		}
	}
}
