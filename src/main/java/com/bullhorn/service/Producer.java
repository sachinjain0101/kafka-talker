package com.bullhorn.service;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bullhorn.config.BaseConfig;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;

public class Producer {

	private static final Logger LOGGER = LoggerFactory.getLogger(Producer.class);

    private KafkaProducer<Long, JsonNode> producer;

	@Autowired
	public Producer(@Qualifier("kafkaConfig")BaseConfig config) {
		LOGGER.info("Constructing the Producer : {}", config);
		
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
		props.put(ProducerConfig.CLIENT_ID_CONFIG, config.getHostName());
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());

		producer = new KafkaProducer<>(props);
	}

	public String sendData(String topicName, JsonNode data)
			throws InterruptedException, ExecutionException {
		long time = System.currentTimeMillis();
		
		final ProducerRecord<Long, JsonNode> record = new ProducerRecord<>(topicName, time, data);
		RecordMetadata metadata = producer.send(record, new Callback() {

			@Override
			public void onCompletion(RecordMetadata metadata, Exception exception) {
				if (exception != null) {
					exception.printStackTrace();
				}
				LOGGER.info("Sent:" + record.value() + ", Partition: " + metadata.partition() + ", Offset: "
						+ metadata.offset());
			}
		}).get();

		return "[" + metadata.partition() + metadata.offset() + "]" + data.toString();
	}

}
