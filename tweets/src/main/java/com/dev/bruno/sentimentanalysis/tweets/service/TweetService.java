package com.dev.bruno.sentimentanalysis.tweets.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.OrderBy;

@Stateless
public class TweetService {

	private Datastore datastore;

	private KeyFactory keyFactory;

	@Resource(name = "credentials.folder")
	private String credentialsFolder;

	@PostConstruct
	private void init() {
		try {
			datastore = DatastoreOptions.newBuilder().setProjectId("sentimentalizer-169016")
					.setCredentials(ServiceAccountCredentials
							.fromStream(new FileInputStream(credentialsFolder + "/sentimentalizer.json")))
					.build().getService();

			keyFactory = datastore.newKeyFactory().setKind("tweet");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void insert(com.dev.bruno.sentimentanalysis.tweets.model.Tweet tweet) {
		if (tweet == null) {
			throw new AppException("Tweet não informado.");
		}

		if (tweet.getId() == null || tweet.getText() == null) {
			throw new AppException("id e text são campos obrigatórios.");
		}

		Key key = keyFactory.newKey(tweet.getId());

		Entity.Builder builder = Entity.newBuilder(key).set("id", tweet.getId()).set("text", tweet.getText());
		
		if(tweet.getHumanSentiment() != null) {
			builder.set("humanSentiment", tweet.getHumanSentiment());
		} else {
			builder.setNull("humanSentiment");
		}
		
		if(tweet.getMachineSentiment() != null) {
			builder.set("machineSentiment", tweet.getMachineSentiment());
		} else {
			builder.setNull("machineSentiment");
		}

		Entity entity = builder.build();
		datastore.put(entity);
	}

	public List<Tweet> list() {
		Query<Entity> query = Query.newEntityQueryBuilder()
			    .setKind("tweet")
			    .setFilter(com.google.cloud.datastore.StructuredQuery.PropertyFilter.isNull("humanSentiment"))
			    .setOrderBy(OrderBy.asc("id")).setLimit(100)
			    .build();
		
		QueryResults<Entity> result = datastore.run(query);
		
		List<Tweet> tweets = new ArrayList<>();
		while (result.hasNext()) {
			Entity entity = result.next();
			
			com.dev.bruno.sentimentanalysis.tweets.model.Tweet tweet = new Tweet();
			tweet.setId(entity.getLong("id"));
			tweet.setText(entity.getString("text"));
			
			if(!entity.isNull("humanSentiment")) {
				tweet.setHumanSentiment(entity.getLong("humanSentiment"));
			}
			
			if(!entity.isNull("machineSentiment")) {
				tweet.setMachineSentiment(entity.getLong("machineSentiment"));
			}
			
			tweets.add(tweet);
		}
		
		return tweets;
	}
}