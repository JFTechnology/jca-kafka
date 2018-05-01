/*
 * Copyright 2018 JF Technology (UK) Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jftechnology.jca.kafka.consumer;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.spi.Activation;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;

import org.apache.kafka.clients.consumer.ConsumerConfig;

import com.jftechnology.jca.kafka.api.KafkaListener;

/**
 * @author stephen.flynn@jftechnology.com
 * @since 1.0
 */
@Activation(messageListeners = KafkaListener.class)
public class KafkaActivationSpec implements ActivationSpec {

    private KafkaResourceAdapter ra;

    private String bootstrapServers;

    private String keyDeserializer;

    private String valueDeserializer;

    private String clientId;

    private String groupId;

    private Integer autoCommitIntervalMs;

    private Boolean enableAutoCommit = false;

    private String topics;

    private Integer poolSize = 1;

    private Long pollInterval = 5L;

    private Long initialPollDelay = 2000L;

    private Integer fetchMinBytes = 1;

    private Integer maxPartitionFetchBytes;

    private Integer heartbeatIntervalMs;

    private Integer sessionTimeoutMs;

    private String autoOffsetReset;

    private Integer connectionsMaxIdleMs;

    private Integer receiveBufferBytes;

    private Integer requestTimeoutMs;

    private Boolean checkCRCs = true;

    private Integer fetchMaxWaitMs;

    private Long metadataMaxAgeMs;

    private Integer reconnectBackoffMs;

    private Integer retryBackoffMs;

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public void validate() throws InvalidPropertyException {

        Map<String, Object> properties = getConsumerProperties();

        validate(properties, ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG);
        validate(properties, ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG);
        validate(properties, ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG);
        validate(properties, ConsumerConfig.CLIENT_ID_CONFIG);
    }

    /**
     * @since 1.0
     */
    public void validate(Map<String, Object> properties, String name) throws InvalidPropertyException {

        if (properties.get(name) == null) {

            throw new InvalidPropertyException(String.format("Missing required Kafka consumer property  : '%s'", name));
        }
    }

    /**
     * @since 1.0
     */
    public Map<String, Object> getConsumerProperties() {

        Map<String, Object> properties = new HashMap<>();

        // required properties
        add(properties, ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
        add(properties, ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, getKeyDeserializer());
        add(properties, ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, getValueDeserializer());

        add(properties, ConsumerConfig.CLIENT_ID_CONFIG, getClientId());
        add(properties, ConsumerConfig.GROUP_ID_CONFIG, getGroupId());
        add(properties, ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, getEnableAutoCommit());
        add(properties, ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, getFetchMaxWaitMs());
        add(properties, ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, getAutoCommitIntervalMs());
        add(properties, ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, getSessionTimeoutMs());
        add(properties, ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, getAutoOffsetReset());
        add(properties, ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, getConnectionsMaxIdleMs());
        add(properties, ConsumerConfig.RECEIVE_BUFFER_CONFIG, getReceiveBufferBytes());
        add(properties, ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, getRequestTimeoutMs());
        add(properties, ConsumerConfig.CHECK_CRCS_CONFIG, getCheckCRCs());
        add(properties, ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, getMaxPartitionFetchBytes());
        add(properties, ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, getHeartbeatIntervalMs());
        add(properties, ConsumerConfig.FETCH_MIN_BYTES_CONFIG, getFetchMinBytes());
        add(properties, ConsumerConfig.METADATA_MAX_AGE_CONFIG, getMetadataMaxAgeMs());
        add(properties, ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, getReconnectBackoffMs());
        add(properties, ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, getRetryBackoffMs());

        // fill in from defaults
        ra.getConsumerProperties().entrySet().forEach(e -> properties.putIfAbsent(e.getKey(), e.getValue()));

        return properties;
    }

    private void add(Map<String, Object> properties, String key, Object value) {

        if (value != null) {

            properties.put(key, value);
        }
    }

    /**
     * @since 1.0
     */
    public List<String> getTopicList() {

        if (getTopics() == null) {

            return Collections.emptyList();
        }

        return Arrays.asList(getTopics().split(","));
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public KafkaResourceAdapter getResourceAdapter() {

        return ra;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public void setResourceAdapter(ResourceAdapter ra) throws ResourceException {

        this.ra = (KafkaResourceAdapter) ra;

        setBootstrapServers(this.ra.getBootstrapServers());
        setKeyDeserializer(this.ra.getKeyDeserializer());
        setValueDeserializer(this.ra.getValueDeserializer());
        setFetchMaxWaitMs(this.ra.getFetchMaxWaitMs());
    }

    /**
     * @since 1.0
     */
    public Integer getAutoCommitIntervalMs() {

        return autoCommitIntervalMs;
    }

    /**
     * @since 1.0
     */
    public void setAutoCommitIntervalMs(Integer autoCommitIntervalMs) {

        this.autoCommitIntervalMs = autoCommitIntervalMs;
    }

    /**
     * @since 1.0
     */
    public String getBootstrapServers() {

        return bootstrapServers;
    }

    /**
     * @since 1.0
     */
    public void setBootstrapServers(String bootstrapServers) {

        this.bootstrapServers = bootstrapServers;
    }

    /**
     * @since 1.0
     */
    public String getClientId() {

        return clientId;
    }

    /**
     * @since 1.0
     */
    public void setClientId(String clientId) {

        this.clientId = clientId;
    }

    /**
     * @since 1.0
     */
    public Boolean getEnableAutoCommit() {

        return enableAutoCommit;
    }

    /**
     * @since 1.0
     */
    public void setEnableAutoCommit(Boolean enableAutoCommit) {

        this.enableAutoCommit = enableAutoCommit;
    }

    /**
     * @since 1.0
     */
    public String getGroupId() {

        return groupId;
    }

    /**
     * @since 1.0
     */
    public void setGroupId(String groupId) {

        this.groupId = groupId;
    }

    /**
     * @since 1.0
     */
    public String getValueDeserializer() {

        return valueDeserializer;
    }

    /**
     * @since 1.0
     */
    public void setValueDeserializer(String valueDeserializer) {

        this.valueDeserializer = valueDeserializer;
    }

    /**
     * @since 1.0
     */
    public String getKeyDeserializer() {

        return keyDeserializer;
    }

    /**
     * @since 1.0
     */
    public void setKeyDeserializer(String keyDeserializer) {

        this.keyDeserializer = keyDeserializer;
    }

    /**
     * Get the poolSize property.
     *
     * @return Returns the poolSize.
     * @since 1.0
     */
    public Integer getPoolSize() {

        return poolSize;
    }

    /**
     * Set the poolSize property.
     *
     * @param poolSize The poolSize to set.
     * @since 1.0
     */
    public void setPoolSize(Integer poolSize) {

        this.poolSize = poolSize;
    }

    /**
     * @since 1.0
     */
    public Long getPollInterval() {

        return pollInterval;
    }

    /**
     * @since 1.0
     */
    public void setPollInterval(Long pollInterval) {

        this.pollInterval = pollInterval;
    }

    /**
     * @since 1.0
     */
    public String getTopics() {

        return topics;
    }

    /**
     * @since 1.0
     */
    public void setTopics(String topics) {

        this.topics = topics;
    }

    /**
     * @since 1.0
     */
    public Long getInitialPollDelay() {

        return initialPollDelay;
    }

    /**
     * @since 1.0
     */
    public void setInitialPollDelay(Long initialPollDelay) {

        this.initialPollDelay = initialPollDelay;
    }

    /**
     * @since 1.0
     */
    public Integer getFetchMinBytes() {

        return fetchMinBytes;
    }

    /**
     * @since 1.0
     */
    public void setFetchMinBytes(Integer fetchMinBytes) {

        this.fetchMinBytes = fetchMinBytes;
    }

    /**
     * @since 1.0
     */
    public Integer getMaxPartitionFetchBytes() {

        return maxPartitionFetchBytes;
    }

    /**
     * @since 1.0
     */
    public void setMaxPartitionFetchBytes(Integer fetchMaxBytes) {

        this.maxPartitionFetchBytes = fetchMaxBytes;
    }

    /**
     * @since 1.0
     */
    public Integer getHeartbeatIntervalMs() {

        return heartbeatIntervalMs;
    }

    /**
     * @since 1.0
     */
    public void setHeartbeatIntervalMs(Integer heartbeatInterval) {

        this.heartbeatIntervalMs = heartbeatInterval;
    }

    /**
     * @since 1.0
     */
    public Integer getSessionTimeoutMs() {

        return sessionTimeoutMs;
    }

    /**
     * @since 1.0
     */
    public void setSessionTimeoutMs(Integer sessionTimeoutMs) {

        this.sessionTimeoutMs = sessionTimeoutMs;
    }

    /**
     * @since 1.0
     */
    public String getAutoOffsetReset() {

        return autoOffsetReset;
    }

    /**
     * @since 1.0
     */
    public void setAutoOffsetReset(String autoOffsetReset) {

        this.autoOffsetReset = autoOffsetReset;
    }

    /**
     * @since 1.0
     */
    public Integer getConnectionsMaxIdleMs() {

        return connectionsMaxIdleMs;
    }

    /**
     * @since 1.0
     */
    public void setConnectionsMaxIdleMs(Integer connectionsMaxIdle) {

        this.connectionsMaxIdleMs = connectionsMaxIdle;
    }

    /**
     * @since 1.0
     */
    public Integer getReceiveBufferBytes() {

        return receiveBufferBytes;
    }

    /**
     * @since 1.0
     */
    public void setReceiveBufferBytes(Integer receiveBuffer) {

        this.receiveBufferBytes = receiveBuffer;
    }

    /**
     * @since 1.0
     */
    public Integer getRequestTimeoutMs() {

        return requestTimeoutMs;
    }

    /**
     * @since 1.0
     */
    public void setRequestTimeoutMs(Integer requestTimeout) {

        this.requestTimeoutMs = requestTimeout;
    }

    /**
     * @since 1.0
     */
    public Boolean getCheckCRCs() {

        return checkCRCs;
    }

    /**
     * @since 1.0
     */
    public void setCheckCRCs(Boolean checkCRCs) {

        this.checkCRCs = checkCRCs;
    }

    /**
     * @since 1.0
     */
    public Integer getFetchMaxWaitMs() {

        return fetchMaxWaitMs;
    }

    /**
     * @since 1.0
     */
    public void setFetchMaxWaitMs(Integer fetchMaxWait) {

        this.fetchMaxWaitMs = fetchMaxWait;
    }

    /**
     * @since 1.0
     */
    public Long getMetadataMaxAgeMs() {

        return metadataMaxAgeMs;
    }

    /**
     * @since 1.0
     */
    public void setMetadataMaxAgeMs(Long metadataMaxAgeMs) {

        this.metadataMaxAgeMs = metadataMaxAgeMs;
    }

    /**
     * @since 1.0
     */
    public Integer getReconnectBackoffMs() {

        return reconnectBackoffMs;
    }

    /**
     * @since 1.0
     */
    public void setReconnectBackoffMs(Integer reconnectBackoffMs) {

        this.reconnectBackoffMs = reconnectBackoffMs;
    }

    /**
     * @since 1.0
     */
    public Integer getRetryBackoffMs() {

        return retryBackoffMs;
    }

    /**
     * @since 1.0
     */
    public void setRetryBackoffMs(Integer retryBackoffMs) {

        this.retryBackoffMs = retryBackoffMs;
    }
}
