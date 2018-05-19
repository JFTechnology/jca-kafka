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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ConfigProperty;
import javax.resource.spi.Connector;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;

/**
 * @author stephen.flynn@jftechnology.com
 * @since 1.0
 */
@Connector(displayName = "Apache Kafka Resource Adapter", vendorName = "JF Technology (UK) Ltd", version = "1.1")
public class KafkaResourceAdapter implements ResourceAdapter, Serializable {

    /**
     * The serialVersionUID property.
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(KafkaResourceAdapter.class.getName());

    private final UUID uuid = UUID.randomUUID();

    private final Map<MessageEndpointFactory, List<KafkaTimerTask>> registeredFactories = new ConcurrentHashMap<>();

    private BootstrapContext context;

    @ConfigProperty(
            type = String.class,
            defaultValue = "localhost:9092",
            description = ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG)
    private String bootstrapServers;

    @ConfigProperty(
            type = String.class,
            defaultValue = "org.apache.kafka.common.serialization.StringDeserializer",
            description = ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)
    private String keySerializer;

    @ConfigProperty(
            type = String.class,
            defaultValue = "org.apache.kafka.common.serialization.StringSerializer",
            description = ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)
    private String valueSerializer;

    @ConfigProperty(
            type = String.class,
            defaultValue = "org.apache.kafka.common.serialization.StringDeserializer",
            description = ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG)
    private String keyDeserializer;

    @ConfigProperty(
            type = String.class,
            defaultValue = "org.apache.kafka.common.serialization.StringDeserializer",
            description = ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG)
    private String valueDeserializer;

    @ConfigProperty(type = Integer.class, defaultValue = "1000", description = ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG)
    private Integer fetchMaxWaitMs;

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {

        LOGGER.info(String.format("Adapter %s starting...", uuid));
        LOGGER.info(String.format("Adapter bootstrap servers %s", getBootstrapServers()));

        context = ctx;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public void stop() {

        LOGGER.info(String.format("Adapter %s stopping...", uuid));
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec)
            throws ResourceException {

        LOGGER.info("KafkaResourceAdapter :: endpoint activation :: " + endpointFactory.getActivationName());
        LOGGER.info("KafkaResourceAdapter :: endpoint activation :: " + endpointFactory.getEndpointClass());
        LOGGER.info("KafkaResourceAdapter :: endpoint activation :: " + endpointFactory);
        LOGGER.info("KafkaResourceAdapter :: endpoint activation :: " + spec);

        if (spec instanceof KafkaActivationSpec) {

            try {

                KafkaActivationSpec kafkaActivationSpec = (KafkaActivationSpec) spec;

                List<KafkaTimerTask> tasks = new ArrayList<>();

                for (int i = 0; i < kafkaActivationSpec.getPoolSize(); i++) {

                    KafkaTimerTask task = new KafkaTimerTask(endpointFactory, kafkaActivationSpec, context);

                    tasks.add(task);

                    context.createTimer().schedule(task, kafkaActivationSpec.getInitialPollDelay(),
                            kafkaActivationSpec.getPollInterval());
                }

                registeredFactories.put(endpointFactory, tasks);

            } catch (NoSuchMethodException | SecurityException e) {

                throw new ResourceException(e);
            }

        } else {

            LOGGER.warning("Unexpected endpoint activation spec class : " + spec.getClass().getName());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {

        LOGGER.info("KafkaResourceAdapter :: endpoint deactivation :: " + endpointFactory.getActivationName());
        LOGGER.info("KafkaResourceAdapter :: endpoint deactivation :: " + endpointFactory.getEndpointClass());
        LOGGER.info("KafkaResourceAdapter :: endpoint deactivation :: " + endpointFactory);
        LOGGER.info("KafkaResourceAdapter :: endpoint deactivation :: " + spec);

      
        List<KafkaTimerTask> tasks = registeredFactories.remove(endpointFactory);

        for (KafkaTimerTask task : tasks) {

            task.cancel();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException {

        return null;
    }

    /**
     * @since 1.0
     */
    public Map<String, Object> getConsumerProperties() {

        Map<String, Object> properties = new HashMap<>();

        // required properties
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, getValueDeserializer());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, getKeyDeserializer());

        properties.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, 300000L);
        properties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);

        return properties;
    }

    /**
     * @since 1.0
     */
    public Map<String, Object> getProducerProperties() {

        Map<String, Object> properties = new HashMap<>();

        // required properties
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, getValueSerializer());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, getKeySerializer());

        properties.put(ProducerConfig.METADATA_MAX_AGE_CONFIG, 300000L);

        return properties;
    }

    /**
     * Get the bootstrapServers property.
     *
     * @return Returns the bootstrapServers.
     * @since 1.0
     */
    public String getBootstrapServers() {

        return bootstrapServers;
    }

    /**
     * Set the bootstrapServers property.
     *
     * @param bootstrapServers The bootstrapServers to set.
     * @since 1.0
     */
    public void setBootstrapServers(String bootstrapServers) {

        this.bootstrapServers = bootstrapServers;
    }

    /**
     * Get the keyDeserializer property.
     *
     * @return Returns the keyDeserializer.
     * @since 1.0
     */
    public String getKeyDeserializer() {

        return keyDeserializer;
    }

    /**
     * Set the keyDeserializer property.
     *
     * @param keyDeserializer The keyDeserializer to set.
     * @since 1.0
     */
    public void setKeyDeserializer(String keyDeserializer) {

        this.keyDeserializer = keyDeserializer;
    }

    /**
     * Get the valueDeserializer property.
     *
     * @return Returns the valueDeserializer.
     * @since 1.0
     */
    public String getValueDeserializer() {

        return valueDeserializer;
    }

    /**
     * Set the valueDeserializer property.
     *
     * @param valueDeserializer The valueDeserializer to set.
     * @since 1.0
     */
    public void setValueDeserializer(String valueDeserializer) {

        this.valueDeserializer = valueDeserializer;
    }

    /**
     * Get the keySerializer property.
     *
     * @return Returns the keySerializer.
     * @since 1.0
     */
    public String getKeySerializer() {

        return keySerializer;
    }

    /**
     * Set the keySerializer property.
     *
     * @param keySerializer The keySerializer to set.
     * @since 1.0
     */
    public void setKeySerializer(String keySerializer) {

        this.keySerializer = keySerializer;
    }

    /**
     * Get the valueSerializer property.
     *
     * @return Returns the valueSerializer.
     * @since 1.0
     */
    public String getValueSerializer() {

        return valueSerializer;
    }

    /**
     * Set the valueSerializer property.
     *
     * @param valueSerializer The valueSerializer to set.
     * @since 1.0
     */
    public void setValueSerializer(String valueSerializer) {

        this.valueSerializer = valueSerializer;
    }

    /**
     * Get the fetchMaxWaitMs property.
     *
     * @return Returns the fetchMaxWaitMs.
     * @since 1.0
     */
    public Integer getFetchMaxWaitMs() {

        return fetchMaxWaitMs;
    }

    /**
     * Set the fetchMaxWaitMs property.
     *
     * @param fetchMaxWaitMs The fetchMaxWaitMs to set.
     * @since 1.0
     */
    public void setFetchMaxWaitMs(Integer fetchMaxWaitMs) {

        this.fetchMaxWaitMs = fetchMaxWaitMs;
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

        return uuid.equals(((KafkaResourceAdapter) other).uuid);
    }
}
