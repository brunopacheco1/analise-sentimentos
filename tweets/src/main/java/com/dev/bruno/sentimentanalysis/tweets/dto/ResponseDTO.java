package com.dev.bruno.sentimentanalysis.tweets.dto;

public class ResponseDTO {

	private Boolean success = false;
	
	private String message;

	public ResponseDTO(Boolean success, String message) {
		super();
		
		this.success = success;
		this.message = message;
	}
	
	public ResponseDTO(Boolean success) {
		super();
		
		this.success = success;
	}
	
	public ResponseDTO(String message) {
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