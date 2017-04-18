package com.dev.bruno.sentimentanalysis.tweets.dto;

import java.io.Serializable;

public class HashtagDTO implements Serializable {

	private static final long serialVersionUID = 6435974476117737767L;
	
	private String hashtag;

	public String getHashtag() {
		return hashtag;
	}

	public void setHashtag(String hashtag) {
		this.hashtag = hashtag;
	}
}