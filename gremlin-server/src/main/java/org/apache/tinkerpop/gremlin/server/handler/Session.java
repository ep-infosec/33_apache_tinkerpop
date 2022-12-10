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
package org.apache.tinkerpop.gremlin.server.handler;

import com.codahale.metrics.Timer;
import io.netty.channel.Channel;
import org.apache.tinkerpop.gremlin.server.GremlinServer;
import org.apache.tinkerpop.gremlin.server.util.MetricManager;

import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Requests that arrive through the {@link UnifiedHandler} are all processed within a {@code Session} implementation.
 * A session accepts a {@link SessionTask} as the work it is meant to perform and those tasks are constructed from
 * incoming requests. A session may be as short-lived as processing a single task before exiting, as with the
 * {@link SingleTaskSession} or may be longer-lived by handling multiple tasks, as with the {@link MultiTaskSession}.
 */
public interface Session extends Runnable {

    public static final Timer traversalOpTimer = MetricManager.INSTANCE.getTimer(name(GremlinServer.class, "op", "traversal"));
    public static final Timer evalOpTimer = MetricManager.INSTANCE.getTimer(name(GremlinServer.class, "op", "eval"));

    /**
     * Gets the identifier for the session.
     */
    String getSessionId();

    /**
     * Adds a task for session to complete.
     *
     * @throws RejectedExecutionException if the task cannot be queued
     */
    boolean submitTask(final SessionTask sessionTask) throws RejectedExecutionException;

    /**
     * Sets a reference to the job that will cancel this session if it exceeds its timeout period.
     */
    void setSessionCancelFuture(final ScheduledFuture<?> f);

    /**
     * Sets a reference to the job itself that is running this session.
     */
    void setSessionFuture(final Future<?> f);

    /**
     * Provides a general way to interrupt the session by way of a timeout.
     *
     * @param timeout the length of time that passed for the timeout to have triggered
     * @param causedBySession determines if the timeout triggered due to a particular request (i.e. {@code false} or
     *                        because of the session lifetime (i.e. {@code true}
     */
    void triggerTimeout(final long timeout, boolean causedBySession);

    /**
     * Determines if the supplied {@code Channel} object is the same as the one bound to the {@code Session}.
     */
    boolean isBoundTo(final Channel channel);

    /**
     * Determines if this session can accept additional tasks or not.
     */
    boolean isAcceptingTasks();

}
