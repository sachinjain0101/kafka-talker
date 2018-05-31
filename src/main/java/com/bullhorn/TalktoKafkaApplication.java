package com.bullhorn;

import java.lang.Thread.UncaughtExceptionHandler;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;

import com.bullhorn.config.BaseConfig;
import com.bullhorn.consumer.ConsumerAsyncService;
import com.bullhorn.service.Admin;
import com.bullhorn.service.Consumer;
import com.bullhorn.service.Producer;

@SpringBootApplication
@PropertySource({ "classpath:kafka.properties" })
@EnableHypermediaSupport(type = HypermediaType.HAL)
public class TalktoKafkaApplication extends SpringBootServletInitializer{

	private static final Logger LOGGER = LoggerFactory.getLogger(TalktoKafkaApplication.class);

	@Autowired
	private Environment env;

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(TalktoKafkaApplication.class);
    }

	public static void main(String[] args) {
		SpringApplication.run(TalktoKafkaApplication.class, args);
	}

	@Autowired
	BaseConfig config;

	@Bean(name = "kafkaConfig")
	public BaseConfig configInit() {
		config = new BaseConfig(env.getProperty("kafka.bootstrapServers"), env.getProperty("kafka.partitionCount"),
				env.getProperty("kafka.replicationFactor"), env.getProperty("kafka.groupId"),
				env.getProperty("kafka.srcTopicName"), env.getProperty("kafka.destTopicName"),
				env.getProperty("kafka.consumerTimeOut"));

		config.setInitVector(env.getProperty("kafka.initVector"));
		config.setSecurityKey(env.getProperty("kafka.securityKey"));
		return config;
	}

	@Bean
	@DependsOn("kafkaConfig")
	public Consumer consumerInit() {
		return new Consumer(config);
	}

	@Bean
	@DependsOn("kafkaConfig")
	public Producer producerInit() {
		return new Producer(config);
	}

	@Bean
	@DependsOn("kafkaConfig")
	public Admin adminClientInit() {
		return new Admin(config);
	}

	@Autowired
	ConsumerAsyncService consumerAsycSvc;

	@Autowired
	Consumer consumer;

	@PostConstruct
	public void init() {
		LOGGER.info("Startng Consumer Thread");
		consumerAsycSvc.executeAsynchronously();
	}

	@PreDestroy
	public void destroy() {
		addKafkaConsumerShutdownHook();
	}

	public void addKafkaConsumerShutdownHook() {
		consumer.closing = true;
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				LOGGER.info("Shutdown received");
				if (consumer != null)
					consumer.stop();
			}
		});

		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				LOGGER.error("Uncaught Exception on " + t.getName() + " : " + e, e);
				if (consumer != null)
					consumer.stop();
			}
		});
		LOGGER.info("Consumer ShutdownHook Added");
	}
}
