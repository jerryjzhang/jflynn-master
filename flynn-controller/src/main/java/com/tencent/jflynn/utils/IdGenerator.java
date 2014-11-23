package com.tencent.jflynn.utils;

public class IdGenerator {
	public static String generate(){
		return Long.toString(System.currentTimeMillis());
	}
}
