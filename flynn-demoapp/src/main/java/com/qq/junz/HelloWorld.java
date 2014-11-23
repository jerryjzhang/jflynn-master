package com.qq.junz;

public class HelloWorld {
	public static void main(String [] args){
		System.out.println("HelloWorld");
		try{
			Thread.sleep(1000000L);
		}catch(Exception e){
			System.exit(0);
		}
	}
}
