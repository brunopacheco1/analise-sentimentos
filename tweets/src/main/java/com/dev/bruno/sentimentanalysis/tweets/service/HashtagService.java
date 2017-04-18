package com.dev.bruno.sentimentanalysis.tweets.service;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.dev.bruno.sentimentanalysis.tweets.dto.HashtagDTO;
import com.dev.bruno.sentimentanalysis.tweets.exception.AppException;

import twitter4j.FilterQuery;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;

@Singleton
public class HashtagService {
	
	private Set<String> hashtagsToTrack = new HashSet<>();
	
	private TwitterStream twitterStream;
	
	@PostConstruct
	private void init() {
		StatusListener listener = new TweetConsumer();
        
        twitterStream = new TwitterStreamFactory().getInstance();
        
        twitterStream.setOAuthConsumer("FdsFJN4AjOh9mTY8yko1A", "i3NubBHfmPxYRFFB4SLeZUUDf7actVV4H5vJf7kwo");
        twitterStream.setOAuthAccessToken(new AccessToken("34089292-YTr1PlyxxqNPmrtJGqni55GUIx6Lt9zOicN9FLLc5", "iGHdwwK1IAqRY3EDPb18cY8Zma7mi7gK8Gg3QCs6kGbWl"));
        
        twitterStream.addListener(listener);
	}
	
	@PreDestroy
	private void destroy() {
		twitterStream.shutdown();
	}

    public void process(HashtagDTO dto) throws AppException {
    	validar(dto);
    	
    	hashtagsToTrack.add(dto.getHashtag());
    	
    	twitterStream.shutdown();
    	
        FilterQuery filter = new FilterQuery();
        filter.track(hashtagsToTrack.toArray(new String [] {}));
        filter.language(new String[]{"pt"});
        twitterStream.filter(filter);
    }
	
	private void validar(HashtagDTO dto) throws AppException {
	    if(dto == null) {
	        throw new AppException("Hashtag é obrigatória.");
	    }
	    
	    if(StringUtils.isBlank(dto.getHashtag()) || !dto.getHashtag().startsWith("#")) {
	        throw new AppException("Uma hashtag é obrigatória.");
		}
	}
}