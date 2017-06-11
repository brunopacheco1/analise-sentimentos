package com.dev.bruno.sentiments.status.dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateless;

import com.dev.bruno.sentiments.status.exception.AppException;
import com.dev.bruno.sentiments.status.model.Status;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.cloud.datastore.Transaction;

@Stateless
public class StatusDAO {

	private Datastore datastore;
	
	private KeyFactory keyFactory;

	@Resource(name = "credentials.folder")
	private String credentialsFolder;
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	@PostConstruct
	private void init() {
		try {
			datastore = DatastoreOptions.newBuilder().setProjectId("sentimentalizer-169016").setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream(credentialsFolder + "/sentimentalizer.json"))).build().getService();

			keyFactory = datastore.newKeyFactory().setKind("status");
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	public void insert(Status status) {
		Transaction transaction = datastore.newTransaction();
		try {

			Key key = keyFactory.newKey(status.getId());

			FullEntity.Builder<Key> builder = FullEntity.newBuilder(key).set("id", status.getId()).set("text", status.getText()).set("date", status.getDate().getTime()).set("source", status.getSource());

			if (status.getHumanSentiment() != null) {
				builder.set("humanSentiment", status.getHumanSentiment());
			} else {
				builder.setNull("humanSentiment");
			}

			if (status.getMachineSentiment() != null) {
				builder.set("machineSentiment", status.getMachineSentiment());
			} else {
				builder.setNull("machineSentiment");
			}
			
			FullEntity<Key> entity = builder.build();

			transaction.add(entity);

			transaction.commit();
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
				
				throw new AppException("Falha na inserção do status.");
			}
		}
	}

	public void update(Status status) {
		Key key = keyFactory.newKey(status.getId());

		Entity entity = datastore.get(key);
		
		if(entity == null) {
			throw new AppException("Status não encontrado.");
		}

		Transaction transaction = datastore.newTransaction();

		try {
			Entity.Builder builder = Entity.newBuilder(entity);

			if (status.getHumanSentiment() != null) {
				builder.set("humanSentiment", status.getHumanSentiment());
			}

			if (status.getMachineSentiment() != null) {
				builder.set("machineSentiment", status.getMachineSentiment());
			}
			
			entity = builder.build();

			transaction.update(entity);

			transaction.commit();
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
				
				throw new AppException("Falha na atualização do status.");
			}
		}
	}

	public Status get(String id) {
		Key key = keyFactory.newKey(id);

		Entity entity = datastore.get(key);

		Status status = buildStatus(entity);

		return status;
	}

	public List<Status> listNullHumanSentiment(Integer limit) {
		Query<Entity> query = Query.newEntityQueryBuilder().setKind("status").setFilter(com.google.cloud.datastore.StructuredQuery.PropertyFilter.isNull("humanSentiment")).setLimit(limit).build();

		QueryResults<Entity> result = datastore.run(query);

		List<Status> statusResult = new ArrayList<>();
		while (result.hasNext()) {
			Entity entity = result.next();

			Status status = buildStatus(entity);

			statusResult.add(status);
		}

		return statusResult;
	}
	
	public List<Status> listNotNullHumanSentiment() {
		Query<Entity> query = Query.newEntityQueryBuilder().setKind("status").setFilter(CompositeFilter.and(PropertyFilter.ge("humanSentiment", 0), PropertyFilter.le("humanSentiment", 4))).build();

		QueryResults<Entity> result = datastore.run(query);

		List<Status> statusResult = new ArrayList<>();
		while (result.hasNext()) {
			Entity entity = result.next();

			Status status = buildStatus(entity);

			statusResult.add(status);
		}

		return statusResult;
	}
	
	public List<Status> listNullMachineSentiment(Integer limit) {
		Query<Entity> query = Query.newEntityQueryBuilder().setKind("status")
				.setFilter(com.google.cloud.datastore.StructuredQuery.PropertyFilter.isNull("machineSentiment")).setLimit(limit).build();

		QueryResults<Entity> result = datastore.run(query);

		List<Status> statusResult = new ArrayList<>();
		while (result.hasNext()) {
			Entity entity = result.next();

			Status status = buildStatus(entity);

			statusResult.add(status);
		}

		return statusResult;
	}

	private Status buildStatus(Entity entity) {
		Status status = new Status();

		status.setId(entity.getString("id"));
		status.setText(entity.getString("text"));
		status.setDate(new Date(entity.getLong("date")));
		status.setSource(entity.getString("source"));

		if (!entity.isNull("humanSentiment")) {
			status.setHumanSentiment(entity.getLong("humanSentiment"));
		}

		if (!entity.isNull("machineSentiment")) {
			status.setMachineSentiment(entity.getLong("machineSentiment"));
		}
		
		return status;
	}
}
