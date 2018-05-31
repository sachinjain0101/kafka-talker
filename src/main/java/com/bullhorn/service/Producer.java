package com.bullhorn.service;

import java.net.UnknownHostException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bullhorn.config.BaseConfig;
import com.bullhorn.data.QData;

public class Producer {

	private static final Logger LOGGER = LoggerFactory.getLogger(Producer.class);
	
	KafkaProducer<Long, String> producer;

	BaseConfig config = null;
	
	@Autowired
	public Producer(BaseConfig config) {
		this.config=config;
		LOGGER.info("Constructing the Producer : {}", this.config);
		
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.config.getBootstrapServers());
		props.put(ProducerConfig.CLIENT_ID_CONFIG, this.config.getHostName());
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

		producer = new KafkaProducer<Long, String>(props);
	}

	public QData sendData(QData data)
			throws InterruptedException, ExecutionException, UnknownHostException {
		long time = System.currentTimeMillis();
		
		final ProducerRecord<Long, String> record = new ProducerRecord<>(data.getTopicName(), time, data.getData());
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

		data.setData( "[" + metadata.partition() + metadata.offset() + "]" + data.getData());
		return data;
	}

}
