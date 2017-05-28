package com.dev.bruno.sentimentanalysis.tweets.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;

import com.dev.bruno.sentimentanalysis.tweets.model.Tweet;
import com.fasterxml.jackson.databind.ObjectMapper;

import twitter4j.FilterQuery;
import twitter4j.Status;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;

@Singleton
@Startup
public class TwitterStreamService {

	private TwitterStream twitterStream;
	
	@Inject
	private TweetService service;

	@Resource(name="credentials.folder")
	private String credentialsFolder;
	
	@Resource(name="twitter.filters")
	private String filters;
	
	@SuppressWarnings("unchecked")
	@PostConstruct
	private void init() {
		String json;
		try {
			json = new String(IOUtils.toByteArray(new FileInputStream(credentialsFolder + "/twitter.json")));
			
			ObjectMapper mapper = new ObjectMapper();
			
			Map<String, String> credentials = mapper.readValue(json, HashMap.class);
			
			StatusListener listener = new TweetStreamConsumer();
	        
	        twitterStream = new TwitterStreamFactory().getInstance();
	        
	        twitterStream.setOAuthConsumer(credentials.get("apiKey"), credentials.get("apiSecret"));
	        twitterStream.setOAuthAccessToken(new AccessToken(credentials.get("accessToken"), credentials.get("accessTokenSecret")));
	        
	        twitterStream.addListener(listener);
	        
	        //Filtros da pesquisa
	        FilterQuery filter = new FilterQuery();
	        filter.track(filters.split(";"));
	        filter.language(new String[]{"pt"});
	        twitterStream.filter(filter);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@PreDestroy
	private void destroy() {
		twitterStream.shutdown();
	}
	
	public void process(Status status) {
		
		Tweet tweet = new Tweet();
		
		tweet.setId(status.getId());
		tweet.setText(status.getText());
		
		service.insert(tweet);
	}
}