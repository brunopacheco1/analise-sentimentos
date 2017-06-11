package com.dev.bruno.sentiments.twitter.service;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;

import com.dev.bruno.sentiments.status.helper.JacksonConfig;
import com.dev.bruno.sentiments.status.service.StatusService;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

@Stateless
public class TwitterService {

	@Inject
	private StatusService service;
	
	@Resource(name="credentials.folder")
	private String credentialsFolder;
	
	@Resource(name="twitter.filters")
	private String filters;

	@SuppressWarnings("unchecked")
	public void search() throws Exception {
		Twitter twitter = TwitterFactory.getSingleton();
		
		String json = new String(IOUtils.toByteArray(new FileInputStream(credentialsFolder + "/twitter.json")));
		
		Map<String, String> credentials = JacksonConfig.getObjectMapper().readValue(json, HashMap.class);
		
		try {
			twitter.setOAuthConsumer(credentials.get("apiKey"), credentials.get("apiSecret"));
			twitter.setOAuthAccessToken(new AccessToken(credentials.get("accessToken"), credentials.get("accessTokenSecret")));
		} catch (Exception e) {}
		
		for(String filter : filters.split(";")) {
			Query query = new Query(filter);
			query.setLang("pt");
			query.setSince("2006-01-01");
		    QueryResult result = twitter.search(query);
		    while(query != null) {
		    	for(Status status : result.getTweets()) {
			    	if(status.isRetweet()) {
			    		continue;
			    	}
			    	
					try {
						service.insert(status.getId(), status.getText(), status.getCreatedAt(), "TWITTER");
					} catch (Exception e) {}
		    	}
		    	
		    	query = result.nextQuery();
		    	if(query != null) {
		    		result = twitter.search(query);
		    	}
		    }
		}
	}
}