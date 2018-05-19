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
package com.jftechnology.jca.kafka.serialization;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;

import org.apache.kafka.common.serialization.Serializer;

/**
 * JsonSerializer - provides simple {@link JsonObject} to byte array
 * serialization.
 *
 * @author stephen.flynn@jftechnology.com
 * @since 1.0
 */
public class JsonSerializer implements Serializer<JsonObject> {

    private static final JsonWriterFactory FACTORY = Json.createWriterFactory(null);

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public byte[] serialize(String topic, JsonObject data) {

        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);

        JsonWriter writer = FACTORY.createWriter(out);
        writer.writeObject(data);
        writer.close();

        return out.toByteArray();
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

        // no-op
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public void close() {

        // no-op
    }
}
