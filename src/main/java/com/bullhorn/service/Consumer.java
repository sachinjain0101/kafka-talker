package com.bullhorn.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bullhorn.config.BaseConfig;
import com.bullhorn.data.QData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;

public class Consumer implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(Consumer.class);

	public volatile boolean closing = false;
	public volatile List<QData> consumedData = new ArrayList<QData>();
	public final String SEP = "|";

	private final KafkaConsumer<Long, String> consumer;

	@Autowired
	public Consumer(@Qualifier("kafkaConfig")BaseConfig config) {
		//this.config = config;
		LOGGER.info("Constructing the Consumer : {}", config);

		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
		props.put(ConsumerConfig.CLIENT_ID_CONFIG, config.getHostName());
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, 10000);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getGroupId());
		props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 5000);
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

		this.consumer = new KafkaConsumer<>(props);
	}
	
	public synchronized List<QData> recieveData(String topicName) {
		LOGGER.info("Getting data from the list");
		List<QData> dat = consumedData.stream().filter(p -> p.getTopicName().equals((topicName)))
				.collect(Collectors.toList());

		consumedData.removeIf((o) -> o.getTopicName().equals(topicName));
		return dat;
	}

	public void stop() {
		LOGGER.info("Signalling shut down for Consumer [status {}]",closing);
		closing = true;
		consumer.wakeup();
	}

	public void run() {
		recieveData();
	}

	public void recieveData() {
		LOGGER.info("Started recieving data... {}", closing);

		try {
			synchronized (consumer) {
	            this.consumer.subscribe(Collections.singletonList("recalcs"));
	        }

			while (!closing) {
				
				try {
					ConsumerRecords<Long, String> records = consumer.poll(10000);
					LOGGER.info("Waiting for data... {} - {}", System.currentTimeMillis(), closing);
					
					for (ConsumerRecord<Long, String> record : records) {
						LOGGER.info("topic = {} offset = {}, key = {}, value ={}\n", record.topic(),record.offset(), record.key(), record.value());
						//lst.add(record.value());
						consumedData.add(new QData(record.topic(), new ObjectMapper().readTree(record.value())));
					}
					
					for (TopicPartition tp : consumer.assignment())
						System.out.println("Committing offset at position:" + consumer.position(tp));
					
					consumer.commitSync();
					
				} catch (final WakeupException e) {
					LOGGER.info("Consumer closing - caught exception: " + e);
				} catch (final KafkaException e) {
					LOGGER.error("Sleeping for 2s - Consumer has caught: " + e);
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e1) {
						LOGGER.warn("Consumer closing - caught exception: " + e1);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		} finally {
			consumer.close(5, TimeUnit.SECONDS);
			LOGGER.info("Closed consumer and we are done");
		}
	}

}
