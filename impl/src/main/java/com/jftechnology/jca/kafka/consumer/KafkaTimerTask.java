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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.resource.spi.BootstrapContext;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.WorkException;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

/**
 * @author stephen.flynn@jftechnology.com
 * @since 1.0
 */
class KafkaTimerTask extends TimerTask {

    private static final Logger LOGGER = Logger.getLogger(KafkaTimerTask.class.getName());

    private final String id;

    private final KafkaActivationSpec activationSpec;

    private final BootstrapContext bootstrapContext;

    private final MessageEndpointFactory endpointFactory;

    private final Method onMessageMethod;

    private final KafkaConsumer<?, ?> consumer;

    /**
     * @since 1.0
     */
    KafkaTimerTask(MessageEndpointFactory messageEndpointFactory, KafkaActivationSpec activationSpec,
            BootstrapContext bootstrapContext) throws NoSuchMethodException, SecurityException {

        this.endpointFactory = messageEndpointFactory;
        this.activationSpec = activationSpec;
        this.bootstrapContext = bootstrapContext;

        id = String.format("%s-%s", messageEndpointFactory.getEndpointClass().getSimpleName(), UUID.randomUUID());
        onMessageMethod = messageEndpointFactory.getEndpointClass().getMethod("onMessage", ConsumerRecords.class);

        Map<String, Object> properties = activationSpec.getConsumerProperties();

        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, id);

        // set up consumer and subscribe
        consumer = new KafkaConsumer<>(properties);

        if (activationSpec.getTopicPattern() != null && !activationSpec.getTopicPattern().trim().isEmpty()) {

            Pattern pattern = Pattern.compile(activationSpec.getTopicPattern());
            consumer.subscribe(pattern);
            LOGGER.info(String.format("KafkaTimerTask :: subscribed to topic pattern %s :: %s", pattern, id));

        } else {

            consumer.subscribe(activationSpec.getTopicList());
            LOGGER.info(String.format("KafkaTimerTask :: subscribed to topic list %s :: %s",
                    activationSpec.getTopicList(), id));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public void run() {

        LOGGER.fine("KafkaTimerTask :: run :: " + id);

        ConsumerRecords<?, ?> records = consumer.poll(activationSpec.getFetchMaxWaitMs());

        // if we got noting just return
        if (records.isEmpty()) {

            return;
        }

        try {

            // called synchronously
            bootstrapContext.getWorkManager().doWork(new KafkaWork(endpointFactory, records, onMessageMethod));

            // then manually committed if no error thrown by the message
            // endpoint
            consumer.commitAsync();

        } catch (WorkException ex) {

            LOGGER.log(Level.SEVERE, "Work manager failure", ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public boolean cancel() {

        LOGGER.info("KafkaTimerTask :: cancel :: " + id);

        return super.cancel();
    }
}
