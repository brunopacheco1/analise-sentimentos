package com.dev.bruno.sentimentanalysis.tweets.service;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

public class TweetStreamConsumer implements StatusListener {
	
	private TwitterStreamService service = (TwitterStreamService) ServiceLocator.getInstance().lookup(TwitterStreamService.class);

	@Override
	public void onStallWarning(StallWarning arg0) {
	}

	@Override
	public void onScrubGeo(long arg0, long arg1) {
	}

	public void onStatus(Status status) {
		service.process(status);
	}

	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
	}

	public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
	}

	public void onException(Exception ex) {
		ex.printStackTrace();
	}
}