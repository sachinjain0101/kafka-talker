package com.bullhorn.rest;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.bullhorn.data.QData;
import com.bullhorn.data.Topic;
import com.bullhorn.service.Admin;
import com.bullhorn.service.Consumer;
import com.bullhorn.service.Producer;

import io.swagger.annotations.Api;

@RestController
@Api(value = "Base controller for Kafka Talker")
@RequestMapping("/topics")
public class KafkaTalker extends ResourceSupport {

	@Autowired
	Admin client;

	@Autowired
	Consumer consumer;

	@Autowired
	Producer producer;

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
	@ResponseBody
	@GetMapping
	public List<Topic> listTopics() {
		List<Topic> topicList = new ArrayList<Topic>();
		try {
			return topicList = client.getAvailableTopics();
		} catch (UnknownHostException | InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return topicList;
	}
	
	@RequestMapping(value="/hateoas",method = RequestMethod.GET, produces = "application/hal+json")
	@ResponseBody
	@GetMapping
	public ResponseEntity<Resources<Resource<Topic>>> listTopicswithLinks() {
		List<Topic> topicList = new ArrayList<Topic>();
		List<Resource<Topic>> topicResources = new ArrayList<Resource<Topic>>();
		Link selfLink = linkTo(KafkaTalker.class).withSelfRel();

		try {
			topicList = client.getAvailableTopics();
		} catch (UnknownHostException | InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		for (Topic T : topicList) {

			Links links = new Links(
					linkTo(KafkaTalker.class).slash(T.getTopic()).slash("publish").withRel(T.getTopic()),
					linkTo(KafkaTalker.class).slash(T.getTopic()).slash("consume").withRel(T.getTopic()));
			topicResources.add(new Resource<Topic>(T, links));

		}

		Resources<Resource<Topic>> resourceList = new Resources<Resource<Topic>>(topicResources, selfLink);

		return new ResponseEntity<Resources<Resource<Topic>>>(resourceList, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<List<Topic>> deleteTopics(@RequestParam(value = "names") String names) {
		List<Topic> outLst = null;
		try {
			List<String> lst = Arrays.asList(names.split(","));
			outLst = client.deleteTopics(lst);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(outLst, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(outLst, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.PUT)
	public ResponseEntity<List<Topic>> createTopics(@RequestParam(value = "names") String names) {
		List<Topic> outLst = null;
		try {
			List<String> lst = Arrays.asList(names.split(","));
			outLst = client.createTopics(lst);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(outLst, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(outLst, HttpStatus.OK);
	}

	@RequestMapping(value = "/{topic}/publish", method = RequestMethod.POST, consumes = "application/json")
	@ResponseBody
	public QData publish(@PathVariable String topic, @RequestBody QData data) {
		try {
			return producer.sendData(data);
		} catch (Exception e) {
			return new QData("Error", e.getMessage());
		}
	}

	@RequestMapping(value = "/{topic}/consume", method = RequestMethod.GET)
	@ResponseBody
	public List<QData> consume(@PathVariable String topic) {
		try {
			return consumer.recieveData(topic);
		} catch (Exception e) {
			return Arrays.asList(new QData("Error", e.getMessage()));
		}
	}

	@RequestMapping(value = "test", method = RequestMethod.GET)
	@ResponseBody
	public QData checkService() {
		return new QData("Test", "KafkaTalker is running");
	}

}
