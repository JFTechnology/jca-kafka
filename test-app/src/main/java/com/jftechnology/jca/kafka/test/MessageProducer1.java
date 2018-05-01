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
package com.jftechnology.jca.kafka.test;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Schedule;
import javax.ejb.Stateless;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.jftechnology.jca.kafka.api.KafkaProducerFactory;

/**
 * @author stephen.flynn@jftechnology.com
 * @since 1.0
 */
@Stateless
public class MessageProducer1 {

    private static final Logger LOGGER = Logger.getLogger(MessageProducer1.class.getName());

    private static final String[] TOPICS = new String[] {"test-1", "test-2", "test-3"};

    private static final String[] KEYS = new String[] {"key-1", "key-2", "key-3", "key-4"};

    @Resource(mappedName = "java:/eis/kafka/DefaultProducerFactory")
    private KafkaProducerFactory<String, String> factory;

    @Schedule(second = "*/1", hour = "*", minute = "*", persistent = false)
    public void sendMessage() throws Exception {

        try {

            Producer<String, String> conn = factory.createProducer();

            for (String topic : TOPICS) {
                for (String key : KEYS) {
                    for (int i = 0; i < 4; i++) {

                        conn.send(new ProducerRecord<String, String>(topic, key,
                                String.format("Hello %s / %d / key %s", topic, i, key)));
                    }
                }
            }

            conn.flush();

        } catch (Exception e) {

            LOGGER.log(Level.SEVERE, "Send failed", e);
        }
    }
}
