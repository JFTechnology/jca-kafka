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
package com.jftechnology.jca.kafka.producer;

import java.io.PrintWriter;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ConfigProperty;
import javax.resource.spi.ConnectionDefinition;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;

import com.jftechnology.jca.kafka.api.KafkaProducerFactory;

/**
 * @author stephen.flynn@jftechnology.com
 * @since 1.0
 */
@ConnectionDefinition(
        connection = Producer.class,
        connectionImpl = KafkaProducer.class,
        connectionFactory = KafkaProducerFactory.class,
        connectionFactoryImpl = KafkaProducerFactoryImpl.class)
public class KafkaManagedConnectionFactory implements ManagedConnectionFactory, ConnectionRequestInfo {

    /**
     * The serialVersionUID property.
     */
    private static final long serialVersionUID = 1L;

    private final UUID uuid = UUID.randomUUID();

    @ConfigProperty(
            type = String.class,
            defaultValue = "localhost:9092",
            description = ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)
    private String bootstrapServers = "localhost:9092";

    @ConfigProperty(
            type = String.class,
            defaultValue = "KafkaManagedConnectionFactory",
            description = ProducerConfig.CLIENT_ID_CONFIG)
    private String clientId = "KafkaManagedConnectionFactory";

    @ConfigProperty(
            type = String.class,
            description = ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            defaultValue = "org.apache.kafka.common.serialization.StringSerializer")
    private String valueSerializer = "org.apache.kafka.common.serialization.StringSerializer";

    @ConfigProperty(
            type = String.class,
            description = ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            defaultValue = "org.apache.kafka.common.serialization.StringSerializer")
    private String keySerializer = "org.apache.kafka.common.serialization.StringSerializer";

    @ConfigProperty(type = Long.class, description = ProducerConfig.BUFFER_MEMORY_CONFIG, defaultValue = "33554432")
    private Long bufferMemory = 33554432L;

    @ConfigProperty(type = String.class, description = ProducerConfig.ACKS_CONFIG, defaultValue = "1")
    private String acks = "1";

    @ConfigProperty(type = Integer.class, description = ProducerConfig.RETRIES_CONFIG, defaultValue = "0")
    private Integer retries = 0;

    @ConfigProperty(type = Long.class, description = ProducerConfig.RETRY_BACKOFF_MS_CONFIG, defaultValue = "100")
    private Long retryBackoffMs = 100L;

    @ConfigProperty(type = Long.class, description = ProducerConfig.BATCH_SIZE_CONFIG, defaultValue = "16384")
    private Long batchSize = 16384L;

    @ConfigProperty(type = Long.class, description = ProducerConfig.LINGER_MS_CONFIG, defaultValue = "0")
    private Long lingerMs = 0L;

    @ConfigProperty(type = Long.class, description = ProducerConfig.MAX_BLOCK_MS_CONFIG, defaultValue = "60000")
    private Long maxBlockMs = 60000L;

    @ConfigProperty(type = Long.class, description = ProducerConfig.MAX_REQUEST_SIZE_CONFIG, defaultValue = "1048576")
    private Long maxRequestSize = 1048576L;

    @ConfigProperty(type = Integer.class, description = ProducerConfig.RECEIVE_BUFFER_CONFIG, defaultValue = "32768")
    private Integer receiveBufferBytes = 32768;

    @ConfigProperty(
            type = Integer.class,
            description = ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG,
            defaultValue = "30000")
    private Integer requestTimeoutMs = 30000;

    @ConfigProperty(type = String.class, description = ProducerConfig.COMPRESSION_TYPE_CONFIG, defaultValue = "none")
    private String compressionType = "none";

    @ConfigProperty(
            type = Long.class,
            description = ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG,
            defaultValue = "540000")
    private Long connectionsMaxIdleMs = 540000L;

    @ConfigProperty(
            type = Integer.class,
            defaultValue = "5",
            description = ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION)
    private Integer maxInFlightRequestsPerConnection = 5;

    @ConfigProperty(type = Long.class, description = ProducerConfig.METADATA_MAX_AGE_CONFIG, defaultValue = "300000")
    private Long metadataMaxAgeMs = 300000L;

    @ConfigProperty(
            type = Long.class,
            description = ProducerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG,
            defaultValue = "100")
    private Long reconnectBackoffMs = 100L;

    private transient PrintWriter writer;

    private transient KafkaProducer<?, ?> producer;

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public Object createConnectionFactory() throws ResourceException {

        throw new NotSupportedException("Manager-less connection factories not supported");
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public Object createConnectionFactory(ConnectionManager manager) throws ResourceException {

        Producer<?, ?> producer = (Producer<?, ?>) manager.allocateConnection(this, this);

        return new KafkaProducerFactoryImpl<>(producer);
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public KafkaManagedConnection<?, ?> createManagedConnection(Subject subject, ConnectionRequestInfo requestInfo)
            throws ResourceException {

        if (producer == null) {

            producer = new KafkaProducer<>(getProducerProperties());
        }

        return new KafkaManagedConnection<>(producer, requestInfo);
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject,
            ConnectionRequestInfo requestInfo) throws ResourceException {

        return (ManagedConnection) connectionSet.toArray()[0];
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {

        writer = out;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public PrintWriter getLogWriter() throws ResourceException {

        return writer;
    }

    /**
     * @since 1.0
     */
    public Properties getProducerProperties() {

        Properties properties = new Properties();

        properties.setProperty(ProducerConfig.MAX_BLOCK_MS_CONFIG, Long.toString(getMaxBlockMs()));
        properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, Long.toString(getLingerMs()));
        properties.setProperty(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, Long.toString(getMaxRequestSize()));
        properties.setProperty(ProducerConfig.RECEIVE_BUFFER_CONFIG, Integer.toString(getReceiveBufferBytes()));
        properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Long.toString(getBatchSize()));
        properties.setProperty(ProducerConfig.RETRIES_CONFIG, Long.toString(getRetries()));
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
        properties.setProperty(ProducerConfig.CLIENT_ID_CONFIG, getClientId());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, getValueSerializer());
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, getKeySerializer());
        properties.setProperty(ProducerConfig.BUFFER_MEMORY_CONFIG, Long.toString(getBufferMemory()));
        properties.setProperty(ProducerConfig.ACKS_CONFIG, getAcks());
        properties.setProperty(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, Integer.toString(getRequestTimeoutMs()));
        properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, getCompressionType());
        properties.setProperty(ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, Long.toString(getConnectionsMaxIdleMs()));
        properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION,
                Integer.toString(getMaxInFlightRequestsPerConnection()));
        properties.setProperty(ProducerConfig.METADATA_MAX_AGE_CONFIG, Long.toString(getMetadataMaxAgeMs()));
        properties.setProperty(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, Long.toString(getRetryBackoffMs()));
        properties.setProperty(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, Long.toString(getReconnectBackoffMs()));

        return properties;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public String getAcks() {

        return acks;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public void setAcks(String acks) {

        this.acks = acks;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public Long getBatchSize() {

        return batchSize;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public void setBatchSize(Long batchSize) {

        this.batchSize = batchSize;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public String getBootstrapServers() {

        return bootstrapServers;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public void setBootstrapServers(String bootstrapServers) {

        this.bootstrapServers = bootstrapServers;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public Long getBufferMemory() {

        return bufferMemory;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public void setBufferMemory(Long bufferMemory) {

        this.bufferMemory = bufferMemory;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public String getClientId() {

        return clientId;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public void setClientId(String clientId) {

        this.clientId = clientId;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public String getCompressionType() {

        return compressionType;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public void setCompressionType(String compression) {

        this.compressionType = compression;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public Long getConnectionsMaxIdleMs() {

        return connectionsMaxIdleMs;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public void setConnectionsMaxIdleMs(Long connectionsMaxIdleMs) {

        this.connectionsMaxIdleMs = connectionsMaxIdleMs;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public String getKeySerializer() {

        return keySerializer;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public void setKeySerializer(String keyDeserializer) {

        this.keySerializer = keyDeserializer;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public Long getLingerMs() {

        return lingerMs;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public void setLingerMs(Long lingerMs) {

        this.lingerMs = lingerMs;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public Long getMaxBlockMs() {

        return maxBlockMs;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public void setMaxBlockMs(Long maxBlockMs) {

        this.maxBlockMs = maxBlockMs;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public Integer getMaxInFlightRequestsPerConnection() {

        return maxInFlightRequestsPerConnection;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public void setMaxInFlightRequestsPerConnection(Integer maxInFlightRequestsPerConnection) {

        this.maxInFlightRequestsPerConnection = maxInFlightRequestsPerConnection;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public Long getMaxRequestSize() {

        return maxRequestSize;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public void setMaxRequestSize(Long maxRequestSize) {

        this.maxRequestSize = maxRequestSize;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public String getValueSerializer() {

        return valueSerializer;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public void setValueSerializer(String valueDeserializer) {

        this.valueSerializer = valueDeserializer;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public Integer getRetries() {

        return retries;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public void setRetries(Integer retries) {

        this.retries = retries;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public Integer getReceiveBufferBytes() {

        return receiveBufferBytes;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public void setReceiveBufferBytes(Integer receiveBufferBytes) {

        this.receiveBufferBytes = receiveBufferBytes;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public Integer getRequestTimeoutMs() {

        return requestTimeoutMs;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public void setRequestTimeoutMs(Integer requestTimeoutMs) {

        this.requestTimeoutMs = requestTimeoutMs;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public Long getMetadataMaxAgeMs() {

        return metadataMaxAgeMs;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public void setMetadataMaxAgeMs(Long metadataMaxAgeMs) {

        this.metadataMaxAgeMs = metadataMaxAgeMs;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public Long getRetryBackoffMs() {

        return retryBackoffMs;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public void setRetryBackoffMs(Long retryBackoffMs) {

        this.retryBackoffMs = retryBackoffMs;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public Long getReconnectBackoffMs() {

        return reconnectBackoffMs;
    }

    /**
     * Configurable producer property
     * 
     * @since 1.0
     */
    public void setReconnectBackoffMs(Long reconnectBackoffMs) {

        this.reconnectBackoffMs = reconnectBackoffMs;
    }

    /**
     * REVIEW - Returns a hash code value for the object.
     * 
     * @return A hash code value for this object.
     * @since 1.0
     */
    @Override
    public int hashCode() {

        return uuid.hashCode();
    }

    /**
     * REVIEW - Indicates whether some other object is equal to this one.
     * 
     * @param other The reference object with which to compare.
     * @return true If this object is the same as the obj argument, false
     *         otherwise.
     * @since 1.0
     */
    @Override
    public boolean equals(Object other) {

        if (other == null) {

            return false;
        }

        if (other == this) {

            return true;
        }

        if (!(getClass().isInstance(other))) {

            return false;
        }

        return Objects.equals(uuid, ((KafkaManagedConnectionFactory) other).uuid);
    }
}
