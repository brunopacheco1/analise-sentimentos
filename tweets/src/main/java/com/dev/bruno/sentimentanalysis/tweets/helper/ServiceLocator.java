package com.dev.bruno.sentimentanalysis.tweets.helper;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class ServiceLocator {

	private ServiceLocator() {
		try {
			context = new InitialContext();
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	private Context context;
	private String appContext;

	public void setAppContext(String appContext) {
		this.appContext = appContext;
	}
	
	private static ServiceLocator locator;
	
	static {
		locator = new ServiceLocator();
	}
	
	public static ServiceLocator getInstance() {
		return locator;
	}
	
	public synchronized Object lookup(Class<?> type) {
		return lookup("java:global/" + appContext + "/" + type.getSimpleName());
	}
	
	public synchronized Object lookup(String jndiName) {
		try {
			return context.lookup(jndiName);
		} catch (NamingException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}