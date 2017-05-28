package com.dev.bruno.sentimentanalysis.tweets.service;

import java.io.FileInputStream;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateless;

import com.dev.bruno.sentimentanalysis.tweets.exception.AppException;
import com.dev.bruno.sentimentanalysis.tweets.model.Tweet;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.LatLng;

@Stateless
public class TweetService {

	private Datastore datastore;

	private KeyFactory keyFactory;

	@Resource(name="credentials.folder")
	private String credentialsFolder;
	
	@PostConstruct
	private void init() {
        try {
			datastore = DatastoreOptions.newBuilder().setProjectId("sentimentalizer-169016")
				    .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream(credentialsFolder + "/sentimentalizer.json")))
				    .build()
				    .getService();
			
			keyFactory = datastore.newKeyFactory().setKind("tweet");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void process(Tweet tweet) {
		Key key = keyFactory.newKey(tweet.getId());
		
		Entity.Builder builder = Entity.newBuilder(key).set("id", tweet.getId()).set("text", tweet.getText()).setNull("sentiment");
		 
		if(tweet.getGeoLocation() != null) {
			builder.set("geoLocation", LatLng.of(tweet.getGeoLocation().getLatitude(), tweet.getGeoLocation().getLongitude()));
		}
		
		Entity entity = builder.build();
		datastore.put(entity);
	}

	public void update(Tweet tweet) {
		if(tweet == null) {
	        throw new AppException("Tweet não informado.");
	    }
	    
	}
}