package com.dev.bruno.sentiments.status.model;

import java.io.Serializable;

public class Response implements Serializable {

	private static final long serialVersionUID = -3907110847929957239L;

	private Boolean success = false;
	
	private String message;

	public Response(Boolean success, String message) {
		super();
		
		this.success = success;
		this.message = message;
	}
	
	public Response(Boolean success) {
		super();
		
		this.success = success;
	}
	
	public Response(String message) {
		super();
		
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}
}