package com.dev.bruno.sentimentanalysis.tweets.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Stateless;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.dev.bruno.sentimentanalysis.tweets.exception.AppException;
import com.dev.bruno.sentimentanalysis.tweets.model.Tweet;
import com.dev.bruno.sentimentanalysis.tweets.resource.JacksonConfig;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.OrderBy;
import com.google.cloud.datastore.Transaction;

@Stateless
public class TweetService {

	private Datastore datastore;

	private KeyFactory keyFactory;

	@Resource(name = "credentials.folder")
	private String credentialsFolder;

	private Producer<String, String> producer;
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	@PostConstruct
	private void init() {
		try {
			datastore = DatastoreOptions.newBuilder().setProjectId("sentimentalizer-169016")
					.setCredentials(ServiceAccountCredentials
							.fromStream(new FileInputStream(credentialsFolder + "/sentimentalizer.json")))
					.build().getService();

			keyFactory = datastore.newKeyFactory().setKind("tweet");
			
			Properties props = new Properties();
			props.load(new FileInputStream(credentialsFolder + "/kafka.properties"));
		    producer = new KafkaProducer<>(props);
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	@PreDestroy
	private void destroy() {
		producer.close();
	}

	public void insert(Tweet tweet) {
		if (tweet == null) {
			throw new AppException("Tweet não informado.");
		}

		if (tweet.getId() == null || tweet.getText() == null) {
			throw new AppException("id e text são campos obrigatórios.");
		}

		Transaction transaction = datastore.newTransaction();
		try {

			Key key = keyFactory.newKey(tweet.getId());

			FullEntity.Builder<Key> builder = FullEntity.newBuilder(key).set("id", tweet.getId()).set("text",
					tweet.getText());

			if (tweet.getHumanSentiment() != null) {
				builder.set("humanSentiment", tweet.getHumanSentiment());
			} else {
				builder.setNull("humanSentiment");
			}

			if (tweet.getMachineSentiment() != null) {
				builder.set("machineSentiment", tweet.getMachineSentiment());
			} else {
				builder.setNull("machineSentiment");
			}

			FullEntity<Key> entity = builder.build();

			transaction.add(entity);

			transaction.commit();
			
			String json = JacksonConfig.getObjectMapper().writeValueAsString(tweet);
			producer.send(new ProducerRecord<String, String>("tweets", json));
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
		}
	}

	public void update(Tweet tweet) {
		if (tweet == null) {
			throw new AppException("Tweet não informado.");
		}

		if (tweet.getId() == null) {
			throw new AppException("id e text são campos obrigatórios.");
		}

		Key key = keyFactory.newKey(tweet.getId());

		Entity entity = datastore.get(key);
		
		if(entity == null) {
			throw new AppException("Tweet não encontrado.");
		}

		Transaction transaction = datastore.newTransaction();

		try {
			Entity.Builder builder = Entity.newBuilder(entity);

			if (tweet.getHumanSentiment() != null) {
				builder.set("humanSentiment", tweet.getHumanSentiment());
			} else {
				builder.setNull("humanSentiment");
			}

			if (tweet.getMachineSentiment() != null) {
				builder.set("machineSentiment", tweet.getMachineSentiment());
			} else {
				builder.setNull("machineSentiment");
			}

			entity = builder.build();

			transaction.update(entity);

			transaction.commit();
		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}
		}
	}

	public Tweet get(Long id) {
		Key key = keyFactory.newKey(id);

		Entity entity = datastore.get(key);

		Tweet tweet = buildTweet(entity);

		return tweet;
	}

	public List<Tweet> list() {
		Query<Entity> query = Query.newEntityQueryBuilder().setKind("tweet")
				.setFilter(com.google.cloud.datastore.StructuredQuery.PropertyFilter.isNull("humanSentiment"))
				.setOrderBy(OrderBy.asc("id")).setLimit(20).build();

		QueryResults<Entity> result = datastore.run(query);

		List<Tweet> tweets = new ArrayList<>();
		while (result.hasNext()) {
			Entity entity = result.next();

			Tweet tweet = buildTweet(entity);

			tweets.add(tweet);
		}

		return tweets;
	}

	private Tweet buildTweet(Entity entity) {
		Tweet tweet = new Tweet();

		tweet.setId(entity.getString("id"));
		tweet.setText(entity.getString("text"));

		if (!entity.isNull("humanSentiment")) {
			tweet.setHumanSentiment(entity.getLong("humanSentiment"));
		}

		if (!entity.isNull("machineSentiment")) {
			tweet.setMachineSentiment(entity.getLong("machineSentiment"));
		}

		return tweet;
	}
}