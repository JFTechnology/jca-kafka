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

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionMetaData;

/**
 * @author stephen.flynn@jftechnology.com
 * @since 1.0
 */
public class KafkaManagedConnectionMetaData implements ManagedConnectionMetaData {

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public String getEISProductName() throws ResourceException {

        return "Apache Kafka";
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public String getEISProductVersion() throws ResourceException {

        return "1.1";
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public int getMaxConnections() throws ResourceException {

        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public String getUserName() throws ResourceException {

        return "anonymous";
    }
}
