package com.dev.bruno.sentimentanalysis.tweets.resource;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.dev.bruno.sentimentanalysis.tweets.dto.HashtagDTO;
import com.dev.bruno.sentimentanalysis.tweets.dto.ResponseDTO;
import com.dev.bruno.sentimentanalysis.tweets.service.HashtagService;

@Produces(MediaType.APPLICATION_JSON)
@Stateless
@Path("/hashtag")
public class HashtagResource {

	@Inject
	private HashtagService service;
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public ResponseDTO send(HashtagDTO dto) throws Exception {
		service.process(dto);
		
		return new ResponseDTO(true);
	}
}