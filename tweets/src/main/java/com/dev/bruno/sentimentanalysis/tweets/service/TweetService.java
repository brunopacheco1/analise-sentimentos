package com.dev.bruno.sentimentanalysis.tweets.service;

import java.nio.ByteBuffer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateless;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.kinesis.producer.KinesisProducer;
import com.amazonaws.services.kinesis.producer.KinesisProducerConfiguration;

import twitter4j.Status;

@Stateless
public class TweetService {

	public static final String STREAM_NAME = "sentimentalizer";

	public static final String REGION = "us-east-1";

	private KinesisProducer producer;

	@PostConstruct
	private void init() {
		KinesisProducerConfiguration config = new KinesisProducerConfiguration();

		config.setRegion(REGION);

		config.setCredentialsProvider(new DefaultAWSCredentialsProviderChain());

		config.setMaxConnections(1);

		config.setRequestTimeout(60000);

		config.setRecordMaxBufferedTime(15000);

		producer = new KinesisProducer(config);
	}

	@PreDestroy
	private void destroy() {
		producer.destroy();
	}

	public void process(Status status) {
		String json = status.getSource();

		producer.addUserRecord(STREAM_NAME, String.valueOf(status.getId()), ByteBuffer.wrap(json.getBytes()));

		producer.flush();
	}
}