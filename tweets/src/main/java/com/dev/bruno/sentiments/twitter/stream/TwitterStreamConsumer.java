package com.dev.bruno.sentiments.twitter.stream;

import com.dev.bruno.sentiments.status.helper.ServiceLocator;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

public class TwitterStreamConsumer implements StatusListener {
	
	private TwitterStream service = (TwitterStream) ServiceLocator.getInstance().lookup(TwitterStream.class);

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