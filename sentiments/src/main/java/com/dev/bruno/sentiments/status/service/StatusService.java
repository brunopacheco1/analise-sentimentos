package com.dev.bruno.sentiments.status.service;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.dev.bruno.sentiments.status.dao.StatusDAO;
import com.dev.bruno.sentiments.status.exception.AppException;
import com.dev.bruno.sentiments.status.model.Status;
import com.dev.bruno.sentiments.status.stream.StatusStreamProducer;

@Stateless
public class StatusService {

	@Inject
	private StatusDAO dao;
	
	@Inject
	private StatusStreamProducer topic;
	
	public void insert(Object id, String text, Date date, String source) {
		if (id == null || text == null || date == null || source == null) {
			throw new AppException("id, text, date e source são campos obrigatórios.");
		}
		
		Status status = new Status();
		
		status.setId(hash(id + "_" + source));
		status.setText(text);
		status.setDate(date);
		status.setSource(source);
		
		topic.sendToInsert(status);
	}
	
	public void insert(Status status) {
		if (status == null) {
			throw new AppException("Status não informado.");
		}
		
		if (status.getId() == null || status.getText() == null || status.getDate() == null || status.getSource() == null) {
			throw new AppException("id, text, date e source são campos obrigatórios.");
		}
		
		dao.insert(status);
	}

	public void update(Status status) {
		if (status == null) {
			throw new AppException("Status não informado.");
		}

		if (status.getId() == null) {
			throw new AppException("id é campo obrigatório.");
		}
		
		dao.update(status);
	}
	
	public void reprocessMachineSentiment() {
		List<Status> statusResult = dao.list();
		
		statusResult.forEach(status -> topic.sendToUpdate(status));
	}
	
	public void processNullMachineSentiment() {
		List<Status> statusResult = dao.listNullMachineSentiment(1000);
		
		statusResult.forEach(status -> topic.sendToUpdate(status));
	}

	public List<Status> listNullHumanSentiment(Integer limit) {
		return dao.listNullHumanSentiment(limit);
	}

	public File getFile() throws IOException {
		List<Status> statusResult = dao.listNotNullHumanSentiment();
		
		Path path = Files.createTempFile("status", ".csv");
		
		List<String> lines = statusResult.stream().map(status -> status.getHumanSentiment() + ";" + status.getText().replaceAll("\"", "").replaceAll(";", "").replaceAll("\n", " ").replaceAll("\r", " ").replaceAll("\\s+", " ")).collect(Collectors.toList());
		
		Files.write(path, lines);
		
		return path.toFile();
	}
	
	private String hash(String text) {
		try {	
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(text.getBytes(),0,text.length());
		
			return new BigInteger(1,m.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}