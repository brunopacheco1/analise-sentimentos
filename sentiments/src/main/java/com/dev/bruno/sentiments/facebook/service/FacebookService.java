package com.dev.bruno.sentiments.facebook.service;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import facebook4j.Reading;
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

		Set<String> allPosts = new HashSet<>();

		for (String page : pages.split(";")) {
			PagableList<Post> posts = facebook.getFeed(page, new Reading().fields("id"));
			Paging<Post> paging;
			do {
				for (Post post : posts) {
					allPosts.add(post.getId());
				}

				paging = posts.getPaging();
			} while ((paging != null) && ((posts = facebook.fetchNext(paging)) != null));
			
			PagableList<Post> taggedPosts = facebook.getTagged(page, new Reading().fields("id"));
			paging = null;
			do {
				for (Post post : taggedPosts) {
					Post realPost = facebook.getPost(post.getId(), new Reading().fields("id", "message", "created_time"));
					
					if (realPost.getMessage() == null || realPost.getCreatedTime() == null) {
						continue;
					}

					//INSERINDO POSTS ONDE O ECAD FOI TAGEADO
					service.insert(realPost.getId(), realPost.getMessage(), realPost.getCreatedTime(), "FACEBOOK");
				}

				paging = taggedPosts.getPaging();
			} while ((paging != null) && ((taggedPosts = facebook.fetchNext(paging)) != null));
		}

		for (String postId : allPosts) {
			PagableList<Comment> comments = facebook.getPostComments(postId, new Reading().fields("id", "message", "created_time"));
			
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