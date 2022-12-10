/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.gremlin.structure.io.graphson;

import org.apache.tinkerpop.shaded.jackson.core.JsonParser;
import org.apache.tinkerpop.shaded.jackson.core.JsonProcessingException;
import org.apache.tinkerpop.shaded.jackson.databind.DeserializationContext;
import org.apache.tinkerpop.shaded.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Base class for creating deserializers which parses JSON to a {@code Map} to more easily reconstruct an object.
 * Generally speaking greater performance can be attained with deserializer development that directly uses the
 * {@code JsonParser}.
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public abstract class AbstractObjectDeserializer<T> extends StdDeserializer<T> {

    protected AbstractObjectDeserializer(final Class<T> clazz) {
        super(clazz);
    }

    @Override
    public T deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        jsonParser.nextToken();

        // This will automatically parse all typed stuff.
        final Map<String, Object> mapData = deserializationContext.readValue(jsonParser, LinkedHashMap.class);

        return createObject(mapData);
    }

    @Override
    public boolean isCachable() {
        return true;
    }

    public abstract T createObject(final Map<String, Object> data);
}