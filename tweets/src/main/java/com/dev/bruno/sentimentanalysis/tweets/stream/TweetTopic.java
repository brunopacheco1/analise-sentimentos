package com.dev.bruno.sentimentanalysis.tweets.stream;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.dev.bruno.sentimentanalysis.tweets.helper.JacksonConfig;
import com.dev.bruno.sentimentanalysis.tweets.model.Tweet;

@Singleton
@Startup
public class TweetTopic {

	@Resource(name = "credentials.folder")
	private String credentialsFolder;
	
	private Producer<String, String> producer;
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	@PostConstruct
	private void init() {
		try {
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
	
	public void send(Tweet tweet) {
		try {
			String json = JacksonConfig.getObjectMapper().writeValueAsString(tweet);
			producer.send(new ProducerRecord<String, String>("tweets-evaluation", json));
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}
}
