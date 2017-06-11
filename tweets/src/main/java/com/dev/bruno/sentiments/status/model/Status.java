package com.dev.bruno.sentiments.status.model;

import java.io.Serializable;
import java.util.Date;

public class Status implements Serializable {

	private static final long serialVersionUID = -8238349604957197173L;

	private String id;
	
	private String text;
	
	private Date date;
	
	private Long humanSentiment;
	
	private Long machineSentiment;
	
	private String source;
	
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

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
}