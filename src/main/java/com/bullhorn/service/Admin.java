package com.bullhorn.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bullhorn.config.BaseConfig;
import com.bullhorn.data.Topic;
import org.springframework.beans.factory.annotation.Qualifier;

public class Admin{

	private static final Logger LOGGER = LoggerFactory.getLogger(Admin.class);

	private AdminClient client;

	private BaseConfig config;

	@Autowired
	public Admin(@Qualifier("kafkaConfig") BaseConfig config) {
		LOGGER.info("Constructing the Producer : {}", config);
		this.config = config;
		Properties props = new Properties();
		props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
		props.put(AdminClientConfig.CLIENT_ID_CONFIG, config.getHostName());

		this.client = KafkaAdminClient.create(props);
	}

	public List<Topic> getAvailableTopics() throws InterruptedException, ExecutionException {
		LOGGER.info("Getting available topics");
		ListTopicsResult lst = client.listTopics();
		List<Topic> outLst = new ArrayList<>();
		lst.names().get().forEach((v) -> outLst.add(new Topic(v)));
		return outLst;
	}

	public List<Topic> deleteTopics(List<String> topicNameLst)
			throws InterruptedException, ExecutionException {
		LOGGER.info("Deleting specifed topics : {}", topicNameLst);
		client.deleteTopics(topicNameLst);
		ListTopicsResult lst = client.listTopics();
		List<Topic> outLst = new ArrayList<>();
		lst.names().get().forEach((v) -> outLst.add(new Topic(v)));
		return outLst;
	}

	public List<Topic> createTopics(List<String> topicNameLst)
			throws InterruptedException, ExecutionException {
		LOGGER.info("Creating specifed topics : {}", topicNameLst);
		ListTopicsResult lst = client.listTopics();
		Set<String> existingTopics = lst.names().get();

		List<String> delLst = new ArrayList<>(topicNameLst);

		existingTopics.forEach((t) -> {
			LOGGER.info("{}, {}", t, delLst);
			if (topicNameLst.contains(t))
				delLst.remove(t);
		});

		client.deleteTopics(delLst);

		List<NewTopic> newTopics = new ArrayList<>();
		for (String topicName : topicNameLst) {
			newTopics.add(new NewTopic(topicName, config.getPartitionCount(), config.getReplicationFactor()));
		}

		client.createTopics(newTopics);

		lst = client.listTopics();
		List<Topic> outLst = new ArrayList<>();
		lst.names().get().forEach((v) -> outLst.add(new Topic(v)));
		return outLst;
	}

}
