package com.dev.bruno.sentimentanalysis.tweets.model;

import java.io.Serializable;

public class Tweet implements Serializable {

	private static final long serialVersionUID = -8238349604957197173L;

	private String id;
	
	private String text;
	
	private Long humanSentiment;
	
	private Long machineSentiment;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Long getHumanSentiment() {
		return humanSentiment;
	}

	public void setHumanSentiment(Long humanSentiment) {
		this.humanSentiment = humanSentiment;
	}

	public Long getMachineSentiment() {
		return machineSentiment;
	}

	public void setMachineSentiment(Long machineSentiment) {
		this.machineSentiment = machineSentiment;
	}
}