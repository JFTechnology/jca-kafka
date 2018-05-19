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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.Work;

import org.apache.kafka.clients.consumer.ConsumerRecords;

/**
 * @author stephen.flynn@jftechnology.com
 * @since 1.0
 */
class KafkaWork implements Work {

    private static final Logger LOGGER = Logger.getLogger(KafkaWork.class.getName());

    private final MessageEndpointFactory factory;

    private final ConsumerRecords<?, ?> records;

    private final Method onMessageMethod;

    private MessageEndpoint endpoint;

    /**
     * @since 1.0
     */
    KafkaWork(MessageEndpointFactory factory, ConsumerRecords<?, ?> records, Method onMessageMethod) {

        LOGGER.fine("KafkaWork :: create");

        this.factory = factory;
        this.records = records;
        this.onMessageMethod = onMessageMethod;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public void run() {

        LOGGER.fine("KafkaWork :: run");

        try {

            endpoint = factory.createEndpoint(null);
            endpoint.beforeDelivery(onMessageMethod);
            onMessageMethod.invoke(endpoint, records);
            endpoint.afterDelivery();

        } catch (ResourceException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException ex) {

            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public void release() {

        LOGGER.fine("KafkaWork :: release");

        if (endpoint != null) {

            endpoint.release();
        }
    }
}
