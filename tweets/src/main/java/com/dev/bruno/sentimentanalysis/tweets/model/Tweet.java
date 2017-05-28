package com.dev.bruno.sentimentanalysis.tweets.model;

import java.io.Serializable;

public class Tweet implements Serializable {

	private static final long serialVersionUID = -8238349604957197173L;

	private Long id;
	
	private String text;
	
	private Long sentiment;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Long getSentiment() {
		return sentiment;
	}

	public void setSentiment(Long sentiment) {
		this.sentiment = sentiment;
	}
}