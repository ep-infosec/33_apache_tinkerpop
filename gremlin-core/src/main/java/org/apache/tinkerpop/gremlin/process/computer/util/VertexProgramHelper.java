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
package org.apache.tinkerpop.gremlin.process.computer.util;

import org.apache.commons.configuration2.AbstractConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.process.computer.VertexComputeKey;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.EdgeVertexStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.VertexStep;
import org.apache.tinkerpop.gremlin.util.Serializer;

import java.io.IOException;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public final class VertexProgramHelper {

    private VertexProgramHelper() {
    }

    public static Set<String> vertexComputeKeysAsSet(final Set<VertexComputeKey> vertexComputeKeySet) {
        final Set<String> set = new HashSet<>(vertexComputeKeySet.size());
        for (final VertexComputeKey key : vertexComputeKeySet) {
            set.add(key.getKey());
        }
        return set;
    }

    public static boolean isTransientVertexComputeKey(final String key, final Set<VertexComputeKey> vertexComputeKeySet) {
        for (final VertexComputeKey vertexComputeKey : vertexComputeKeySet) {
            if (vertexComputeKey.getKey().equals(key))
                return vertexComputeKey.isTransient();
        }
        throw new IllegalArgumentException("Could not find key in vertex compute key set: " + key);
    }

    public static String[] vertexComputeKeysAsArray(final Set<VertexComputeKey> vertexComputeKeySet) {
        return VertexProgramHelper.vertexComputeKeysAsSet(vertexComputeKeySet).toArray(new String[vertexComputeKeySet.size()]);
    }

    public static void serialize(final Object object, final Configuration configuration, final String key) {
        try {
            configuration.setProperty(key, Base64.getEncoder().encodeToString(Serializer.serializeObject(object)));
        } catch (final IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public static <T> T deserialize(final Configuration configuration, final String key) {
        try {
            // a bit of a weird double try-catch here. Base64 can throw an IllegalArgumentException if given some
            // bad data to deserialize. that needs to be caught and then re-cast as a IOException so that downstream
            // systems can better catch and react to the error. giraph is the big hassle here it seems - see
            // GiraphGraphComputer.run() for more related notes on this specifically where
            // VertexProgram.createVertexProgram() is called as it has special handling for errors related to
            // deserialization. if not handled properly, giraph will hang in tests. i don't want to over-tweak this
            // code too much for two reasons (1) dont want to alter method signatures too much or mess with existing
            // logic within 3.2.x (2) giraph is dead in 3.4.x so no point to trying to make this a ton more elegant.
            try {
                return (T) Serializer.deserializeObject(Base64.getDecoder().decode(configuration.getString(key).getBytes()));
            } catch (IllegalArgumentException iae) {
                throw new IOException(iae.getMessage());
            }
        } catch (final IOException | ClassNotFoundException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public static <S, E> Traversal.Admin<S, E> reverse(final Traversal.Admin<S, E> traversal) {
        for (final Step step : traversal.getSteps()) {
            if (step instanceof VertexStep)
                ((VertexStep) step).reverseDirection();
            if (step instanceof EdgeVertexStep)
                ((EdgeVertexStep) step).reverseDirection();
        }
        return traversal;
    }

    public static void legalConfigurationKeyValueArray(final Object... configurationKeyValues) throws IllegalArgumentException {
        if (configurationKeyValues.length % 2 != 0)
            throw new IllegalArgumentException("The provided arguments must have a size that is a factor of 2");
        for (int i = 0; i < configurationKeyValues.length; i = i + 2) {
            if (!(configurationKeyValues[i] instanceof String))
                throw new IllegalArgumentException("The provided key/value array must have a String key on even array indices");
        }
    }
}
