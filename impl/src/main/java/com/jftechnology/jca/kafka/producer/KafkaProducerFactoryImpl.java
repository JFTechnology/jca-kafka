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

import java.io.Serializable;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.Referenceable;
import javax.resource.ResourceException;

import org.apache.kafka.clients.producer.Producer;

import com.jftechnology.jca.kafka.api.KafkaProducerFactory;

/**
 * @author stephen.flynn@jftechnology.com
 * @since 1.0
 */
class KafkaProducerFactoryImpl<K, V> implements KafkaProducerFactory<K, V>, Serializable, Referenceable {

    /**
     * The serialVersionUID property.
     */
    private static final long serialVersionUID = 1L;

    private final Producer<K, V> producer;

    private Reference reference;

    /**
     * @since 1.0
     */
    KafkaProducerFactoryImpl(Producer<K, V> producer) {

        this.producer = producer;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public Producer<K, V> createProducer() throws ResourceException {

        return producer;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public void setReference(Reference reference) {

        this.reference = reference;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public Reference getReference() throws NamingException {

        return reference;
    }
}
