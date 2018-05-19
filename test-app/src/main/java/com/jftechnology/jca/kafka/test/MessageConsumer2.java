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

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.jboss.ejb3.annotation.ResourceAdapter;

import com.jftechnology.jca.kafka.api.KafkaListener;

/**
 * @author stephen.flynn@jftechnology.com
 * @since 1.0
 */
@MessageDriven(
        activationConfig = {@ActivationConfigProperty(propertyName = "clientId", propertyValue = "MessageConsumer2"),
                @ActivationConfigProperty(propertyName = "groupId", propertyValue = "test-group-2"),
                @ActivationConfigProperty(propertyName = "topics", propertyValue = "test-2"),
                @ActivationConfigProperty(propertyName = "poolSize", propertyValue = "2")})
@ResourceAdapter("com.jftechnology.jca.kafka.rar")
public class MessageConsumer2 implements KafkaListener<String, String> {

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public void onMessage(ConsumerRecords<String, String> records) {

        for (ConsumerRecord<String, String> record : records) {

            System.out.printf("MessageConsumer2 :: %s %n", record.value());
        }
    }
}
