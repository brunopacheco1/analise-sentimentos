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

import facebook4j.Comment;
import facebook4j.Facebook;
import facebook4j.FacebookFactory;
import facebook4j.PagableList;
import facebook4j.Paging;
import facebook4j.Post;
import facebook4j.auth.AccessToken;

@Stateless
public class FacebookService {

	@Inject
	private StatusService service;

	@Resource(name = "credentials.folder")
	private String credentialsFolder;

	@Resource(name = "facebook.pages")
	private String pages;

	@SuppressWarnings("unchecked")
	public void search() throws Exception {
		String json = new String(IOUtils.toByteArray(new FileInputStream(credentialsFolder + "/facebook.json")));

		Map<String, String> credentials = JacksonConfig.getObjectMapper().readValue(json, HashMap.class);

		Facebook facebook = new FacebookFactory().getInstance();

		facebook.setOAuthAppId(credentials.get("appId"), credentials.get("appSecret"));

		AccessToken accessToken = facebook.getOAuthAppAccessToken();

		facebook.setOAuthAccessToken(accessToken);

		Map<String, Post> allPosts = new HashMap<>();

		for (String page : pages.split(";")) {
			PagableList<Post> posts = facebook.getPosts(page);
			Paging<Post> paging;
			do {
				for (Post post : posts) {
					if (post.getMessage() == null || post.getCreatedTime() == null) {
						continue;
					}

					allPosts.put(post.getId(), post);
				}

				paging = posts.getPaging();
			} while ((paging != null) && ((posts = facebook.fetchNext(paging)) != null));
			
			PagableList<Post> taggedPosts = facebook.getTagged(page);
			paging = null;
			do {
				for (Post post : taggedPosts) {
					if (post.getMessage() == null || post.getCreatedTime() == null) {
						continue;
					}

					//INSERINDO POSTS ONDE O ECAD FOI TAGEADO
					service.insert(post.getId(), post.getMessage(), post.getCreatedTime(), "FACEBOOK");

					allPosts.put(post.getId(), post);
				}

				paging = taggedPosts.getPaging();
			} while ((paging != null) && ((taggedPosts = facebook.fetchNext(paging)) != null));
		}

		for (Post post : allPosts.values()) {
			PagableList<Comment> comments = post.getComments();
			Paging<Comment> paging;
			do {
				for (Comment comment : comments) {
					if (comment.getMessage() == null || comment.getCreatedTime() == null) {
						continue;
					}
					
					//INSERINDO COMENTARIOS DOS POSTS DO ECAD OU DOS POSTS QUE ESTE FOI TAGEADO
					service.insert(comment.getId(), comment.getMessage(), comment.getCreatedTime(), "FACEBOOK");
				}

				paging = comments.getPaging();
			} while ((paging != null) && ((comments = facebook.fetchNext(paging)) != null));
		}
	}
}