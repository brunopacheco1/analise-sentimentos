package com.dev.bruno.sentiments.status.helper;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
public class AppInitializer {

	@Resource(name="app.context")
	private String appContext;
	
	@PostConstruct
	private void initializeApp() {
		ServiceLocator.getInstance().setAppContext(appContext);
	}
}