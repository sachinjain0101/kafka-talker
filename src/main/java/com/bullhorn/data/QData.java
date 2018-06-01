package com.bullhorn.data;

import com.fasterxml.jackson.databind.JsonNode;

public class QData {

    private String topicName;
    private JsonNode data;

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public JsonNode getData() {
		return data;
	}

	public void setData(JsonNode data) {
		this.data = data;
	}

	public QData(String topicName, JsonNode data) {
		super();
		this.topicName = topicName;
		this.data = data;
	}

	public QData() {
		super();
	}

}
