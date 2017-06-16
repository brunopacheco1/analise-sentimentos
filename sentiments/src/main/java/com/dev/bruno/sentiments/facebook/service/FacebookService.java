package com.dev.bruno.sentiments.facebook.service;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;

import com.dev.bruno.sentiments.status.helper.JacksonConfig;
import com.dev.bruno.sentiments.status.service.StatusService;

import facebook4j.Facebook;
import facebook4j.FacebookFactory;
import facebook4j.Post;
import facebook4j.ResponseList;
import facebook4j.auth.AccessToken;

@Stateless
public class FacebookService {

	@Inject
	private StatusService service;
	
	@Resource(name="credentials.folder")
	private String credentialsFolder;
	
	@Resource(name="search.filters")
	private String filters;

	@SuppressWarnings("unchecked")
	public void search() throws Exception {
		String json = new String(IOUtils.toByteArray(new FileInputStream(credentialsFolder + "/facebook.json")));
		
		Map<String, String> credentials = JacksonConfig.getObjectMapper().readValue(json, HashMap.class);
		
		Facebook facebook = new FacebookFactory().getInstance();
		
		facebook.setOAuthAppId(credentials.get("appId"), credentials.get("appSecret"));
		
		AccessToken accessToken = facebook.getOAuthAppAccessToken();
		
		facebook.setOAuthAccessToken(accessToken);
		
		for(String filter : filters.split(";")) {
			ResponseList<Post> results = facebook.searchPosts(filter);
			
			for(Post post : results) {
				service.insert(post.getId(), post.getDescription(), post.getCreatedTime(), "FACEBOOK");
			}
		}
	}
}