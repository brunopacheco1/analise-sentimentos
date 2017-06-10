package com.dev.bruno.sentimentanalysis.tweets.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;

import com.dev.bruno.sentimentanalysis.tweets.dao.TweetDAO;
import com.dev.bruno.sentimentanalysis.tweets.exception.AppException;
import com.dev.bruno.sentimentanalysis.tweets.helper.JacksonConfig;
import com.dev.bruno.sentimentanalysis.tweets.model.Tweet;
import com.dev.bruno.sentimentanalysis.tweets.stream.TweetTopic;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

@Stateless
public class TweetService {

	@Inject
	private TweetDAO dao;
	
	@Inject
	private TweetTopic topic;
	
	@Resource(name="credentials.folder")
	private String credentialsFolder;
	
	@Resource(name="twitter.filters")
	private String filters;

	public void create(Long id, String text, Date date) {
		if (id == null || text == null || date == null) {
			throw new AppException("id, text e date são campos obrigatórios.");
		}
		
		Tweet tweet = new Tweet();
		
		tweet.setId(hash(String.valueOf(id)));
		tweet.setText(text);
		tweet.setDate(date);
		
		topic.send(tweet);
	}
	
	public void insert(Tweet tweet) {
		if (tweet == null) {
			throw new AppException("Tweet não informado.");
		}
		
		if (tweet.getId() == null || tweet.getText() == null || tweet.getDate() == null) {
			throw new AppException("id, text e date são campos obrigatórios.");
		}
		
		dao.insert(tweet);
	}

	public void update(Tweet tweet) {
		if (tweet == null) {
			throw new AppException("Tweet não informado.");
		}

		if (tweet.getId() == null || tweet.getText() == null || tweet.getDate() == null) {
			throw new AppException("id, text e date são campos obrigatórios.");
		}
		
		dao.update(tweet);
	}

	public Tweet get(String id) {
		if (id == null) {
			throw new AppException("id é campo obrigatório.");
		}
		
		return dao.get(id);
	}

	public List<Tweet> listNullHumanSentiment(Integer limit) {
		return dao.listNullHumanSentiment(limit);
	}
	
	private String hash(String text) {
		try {	
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(text.getBytes(),0,text.length());
		
			return new BigInteger(1,m.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public File getFile() throws IOException {
		List<Tweet> tweets = dao.listNotNullHumanSentiment();
		
		Path path = Files.createTempFile("tweets", ".csv");
		
		List<String> lines = tweets.stream().map(tweet -> tweet.getHumanSentiment() + ";" + tweet.getText().replaceAll("\"", "").replaceAll(";", "").replaceAll("\n", " ").replaceAll("\r", " ").replaceAll("\\s+", " ")).collect(Collectors.toList());
		
		Files.write(path, lines);
		
		return path.toFile();
	}
	
	@SuppressWarnings("unchecked")
	public void search() throws Exception {
		Twitter twitter = TwitterFactory.getSingleton();
		
		String json = new String(IOUtils.toByteArray(new FileInputStream(credentialsFolder + "/twitter.json")));
		
		Map<String, String> credentials = JacksonConfig.getObjectMapper().readValue(json, HashMap.class);
		
		twitter.setOAuthConsumer(credentials.get("apiKey"), credentials.get("apiSecret"));
		twitter.setOAuthAccessToken(new AccessToken(credentials.get("accessToken"), credentials.get("accessTokenSecret")));
        
		for(String filter : filters.split(";")) {
			Query query = new Query(filter);
			query.setLang("pt");
			query.setSince("2010-01-01");
		    QueryResult result = twitter.search(query);
		    while(query != null) {
		    	for(Status status : result.getTweets()) {
			    	if(status.isRetweet()) {
			    		continue;
			    	}
			    	
			    	Tweet tweet = new Tweet();
					
					tweet.setId(hash(String.valueOf(status.getId())));
					tweet.setText(status.getText());
					tweet.setDate(status.getCreatedAt());
					
					try {
						insert(tweet);
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