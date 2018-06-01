package com.bullhorn;

import java.lang.Thread.UncaughtExceptionHandler;

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
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
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

	@Bean(name = "kafkaConfig")
    public BaseConfig configInit() {
        BaseConfig config = new BaseConfig(env.getProperty("kafka.bootstrapServers"), env.getProperty("kafka.partitionCount"),
				env.getProperty("kafka.replicationFactor"), env.getProperty("kafka.groupId"),
				env.getProperty("kafka.srcTopicName"), env.getProperty("kafka.destTopicName"),
				env.getProperty("kafka.consumerTimeOut"));

		config.setInitVector(env.getProperty("kafka.initVector"));
		config.setSecurityKey(env.getProperty("kafka.securityKey"));
		LOGGER.debug("{}",config.toString());
		return config;
	}

	@Bean(name = "consumer")
	@DependsOn("kafkaConfig")
	public Consumer consumerInit() {
        LOGGER.debug("****** Consumer Constructed");
        return new Consumer(configInit());
	}

	@Bean(name = "producer")
	@DependsOn("consumer")
	public Producer producerInit() {
        LOGGER.debug("****** Producer Constructed");
        return new Producer(configInit());
	}

	@Bean(name = "admin")
	@DependsOn("producer")
	public Admin adminClientInit() {
        LOGGER.debug("****** Admin Constructed");
        return new Admin(configInit());
	}


    @Bean(name = "consumer-async-svc")
    @DependsOn("admin")
    public ConsumerAsyncService consumerAsycSvcInit() {
        LOGGER.debug("****** ConsumerAsyncService Constructed");
        return new ConsumerAsyncService(consumerInit());
    }

	@EventListener
	public void init(ContextRefreshedEvent event) {
		LOGGER.info("Startng Consumer Thread");
        consumerAsycSvcInit().executeAsynchronously();
	}

	@PreDestroy
	public void destroy() {
		addKafkaConsumerShutdownHook();
	}

	public void addKafkaConsumerShutdownHook() {
        Consumer consumer = consumerInit();
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
