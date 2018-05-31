package com.bullhorn.config;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.stereotype.Component;

@Component
public class BaseConfig {

	private String bootstrapServers;
	private int partitionCount;
	private short replicationFactor;
	private String hostName = "NA";
	private String groupId;
	private String srcTopicName;
	private String destTopicName;
	private String securityKey;
	private String initVector;
	private int consumerTimeOut;

	public String getHostName() {
		return hostName;
	}

	public String getBootstrapServers() {
		return bootstrapServers;
	}

	public void setBootstrapServers(String bootstrapServers) {
		this.bootstrapServers = bootstrapServers;
	}

	public int getPartitionCount() {
		return partitionCount;
	}

	public void setPartitionCount(String partitionCount) {
		this.partitionCount = Integer.parseInt(partitionCount);
	}

	public short getReplicationFactor() {
		return replicationFactor;
	}

	public void setReplicationFactor(String replicationFactor) {
		this.replicationFactor = Short.parseShort(replicationFactor);
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getSrcTopicName() {
		return srcTopicName;
	}

	public void setSrcTopicName(String srcTopicName) {
		this.srcTopicName = srcTopicName;
	}

	public String getDestTopicName() {
		return destTopicName;
	}

	public void setDestTopicName(String destTopicName) {
		this.destTopicName = destTopicName;
	}

	public String getSecurityKey() {
		return securityKey;
	}

	public void setSecurityKey(String securityKey) {
		this.securityKey = securityKey;
	}

	public String getInitVector() {
		return initVector;
	}

	public void setInitVector(String initVector) {
		this.initVector = initVector;
	}

	public int getConsumerTimeOut() {
		return consumerTimeOut;
	}

	public void setConsumerTimeOut(int consumerTimeOut) {
		this.consumerTimeOut = consumerTimeOut;
	}

	public BaseConfig(String bootstrapServers, String partitionCount, String replicationFactor, String srcTopicName,
			String destTopicName) {
		super();
		this.bootstrapServers = bootstrapServers;
		this.partitionCount = Integer.parseInt(partitionCount);
		this.replicationFactor = Short.parseShort(replicationFactor);
		this.srcTopicName = srcTopicName;
		this.destTopicName = destTopicName;
		setHostName();
	}

	public BaseConfig(String bootstrapServers, String partitionCount, String replicationFactor, String groupId,
			String srcTopicName, String destTopicName) {
		super();
		this.bootstrapServers = bootstrapServers;
		this.partitionCount = Integer.parseInt(partitionCount);
		this.replicationFactor = Short.parseShort(replicationFactor);
		this.groupId = groupId;
		this.srcTopicName = srcTopicName;
		this.destTopicName = destTopicName;
		setHostName();
	}

	public BaseConfig(String bootstrapServers, String partitionCount, String replicationFactor) {
		super();
		this.bootstrapServers = bootstrapServers;
		this.partitionCount = Integer.parseInt(partitionCount);
		this.replicationFactor = Short.parseShort(replicationFactor);
		setHostName();
	}

	public BaseConfig(String bootstrapServers, String partitionCount, String replicationFactor, String groupId,
			String srcTopicName, String destTopicName, String consumerTimeOut) {
		super();
		this.bootstrapServers = bootstrapServers;
		this.partitionCount = Integer.parseInt(partitionCount);
		this.replicationFactor = Short.parseShort(replicationFactor);
		this.groupId = groupId;
		this.srcTopicName = srcTopicName;
		this.destTopicName = destTopicName;
		this.consumerTimeOut = Integer.parseInt(consumerTimeOut);
		setHostName();
	}

	public BaseConfig() {
		super();
	}

	private void setHostName() {
		try {
			this.hostName = InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "BaseConfig [bootstrapServers=" + bootstrapServers + ", partitionCount=" + partitionCount
				+ ", replicationFactor=" + replicationFactor + ", hostName=" + hostName + "]";
	}

}