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
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import org.apache.kafka.clients.producer.KafkaProducer;

import com.jftechnology.jca.kafka.consumer.KafkaResourceAdapter;

/**
 * @author stephen.flynn@jftechnology.com
 * @since 1.0
 */
public class KafkaManagedConnection<K, V> implements ManagedConnection {

    private static final Logger LOGGER = Logger.getLogger(KafkaResourceAdapter.class.getName());

    private final List<ConnectionEventListener> listeners = new LinkedList<>();

    private final ConnectionRequestInfo requestInfo;

    private KafkaProducer<K, V> producer;

    private PrintWriter writer;

    /**
     * @since 1.0
     */
    KafkaManagedConnection(KafkaProducer<K, V> producer, ConnectionRequestInfo requestInfo) {

        LOGGER.info("KafkaManagedConnection :: create");

        this.producer = producer;
        this.requestInfo = requestInfo;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo requestInfo) throws ResourceException {

        LOGGER.info("KafkaManagedConnection :: get connection");

        return producer;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public void destroy() throws ResourceException {

        LOGGER.info("Destroying managed connection...");

        producer.close(60, TimeUnit.SECONDS);

        LOGGER.info("Destroyed managed connection");
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public void cleanup() throws ResourceException {

        LOGGER.info("Cleaning up managed connection...");
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public void associateConnection(Object connection) throws ResourceException {

        throw new NotSupportedException();
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public XAResource getXAResource() throws ResourceException {

        throw new NotSupportedException("XA Resource not supported");
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {

        throw new NotSupportedException("Local Transaction not supported");
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public ManagedConnectionMetaData getMetaData() throws ResourceException {

        return new KafkaManagedConnectionMetaData();
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
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {

        LOGGER.info("addConnectionEventListener :: " + listener);

        listeners.add(listener);
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {

        LOGGER.info("removeConnectionEventListener :: " + listener);

        listeners.remove(listener);
    }

    /**
     * @since 1.0
     */
    public void fireConnectionEvent(ConnectionEvent event) {

        listeners.forEach(l -> l.connectionClosed(event));
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == null) {

            return false;
        }

        if (obj == this) {

            return true;
        }

        if (getClass() != obj.getClass()) {

            return false;
        }

        KafkaManagedConnection<?, ?> other = (KafkaManagedConnection<?, ?>) obj;

        return Objects.equals(this.requestInfo, other.requestInfo);
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public int hashCode() {

        return Objects.hash(this.requestInfo);
    }
}
