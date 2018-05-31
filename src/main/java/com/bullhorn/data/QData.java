package com.bullhorn.data;

public class QData {

	String topicName;
	String data;

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public QData(String topicName, String data) {
		super();
		this.topicName = topicName;
		this.data = data;
	}

	public QData() {
		super();
	}

}
