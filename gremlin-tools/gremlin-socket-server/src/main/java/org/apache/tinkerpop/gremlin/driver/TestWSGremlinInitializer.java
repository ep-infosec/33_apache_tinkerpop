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
package org.apache.tinkerpop.gremlin.driver;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.tinkerpop.gremlin.driver.message.RequestMessage;
import org.apache.tinkerpop.gremlin.driver.message.ResponseMessage;
import org.apache.tinkerpop.gremlin.driver.message.ResponseStatusCode;
import org.apache.tinkerpop.gremlin.driver.ser.GraphSONMessageSerializerV2d0;
import org.apache.tinkerpop.gremlin.driver.ser.SerializationException;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.List;
import java.util.UUID;


/**
 * Initializer which partially mimics the Gremlin Server. This initializer injects a handler in the
 * server pipeline that can be modified to send the desired response for a test case.
 * This handler identifies incoming requests with ids matching those in {@link SocketServerSettings}
 * and delivers the response which corresponds to the request id.
 */
public class TestWSGremlinInitializer extends TestWebSocketServerInitializer {
    private static final Logger logger = LoggerFactory.getLogger(TestWSGremlinInitializer.class);

    private final SocketServerSettings settings;

    private final String USER_AGENT_HEADER = "User-Agent";

    /**
     * Gremlin serializer used for serializing/deserializing the request/response. This should be same as client.
     */
    private static final GraphSONMessageSerializerV2d0 SERIALIZER = new GraphSONMessageSerializerV2d0();

    public TestWSGremlinInitializer(final SocketServerSettings settings) {
        this.settings = settings;
    }

    @Override
    public void postInit(ChannelPipeline pipeline) {
        pipeline.addLast(new ClientTestConfigurableHandler());
    }

    /**
     * Handler introduced in the server pipeline to configure expected response for test cases.
     */
    private class ClientTestConfigurableHandler extends MessageToMessageDecoder<BinaryWebSocketFrame> {
        private String userAgent = "";

        @Override
        protected void decode(final ChannelHandlerContext ctx, final BinaryWebSocketFrame frame, final List<Object> objects)
                throws Exception {
            final ByteBuf messageBytes = frame.content();
            final byte len = messageBytes.readByte();
            if (len <= 0) {
                objects.add(RequestMessage.INVALID);
                return;
            }

            final ByteBuf contentTypeBytes = ctx.alloc().buffer(len);
            try {
                messageBytes.readBytes(contentTypeBytes);
            } finally {
                contentTypeBytes.release();
            }
            final RequestMessage msg = SERIALIZER.deserializeRequest(messageBytes.discardReadBytes());

            if (msg.getRequestId().equals(settings.SINGLE_VERTEX_DELAYED_CLOSE_CONNECTION_REQUEST_ID)) {
                logger.info("sending vertex result frame");
                ctx.channel().writeAndFlush(new TextWebSocketFrame(returnSingleVertexResponse(
                        settings.SINGLE_VERTEX_DELAYED_CLOSE_CONNECTION_REQUEST_ID)));
                logger.info("waiting for 2 sec");
                Thread.sleep(2000);
                logger.info("sending close frame");
                ctx.channel().writeAndFlush(new CloseWebSocketFrame());
            } else if (msg.getRequestId().equals(settings.SINGLE_VERTEX_REQUEST_ID)) {
                logger.info("sending vertex result frame");
                ctx.channel().writeAndFlush(new TextWebSocketFrame(returnSingleVertexResponse(settings.SINGLE_VERTEX_REQUEST_ID)));
            } else if (msg.getRequestId().equals(settings.FAILED_AFTER_DELAY_REQUEST_ID)) {
                logger.info("waiting for 1 sec");
                Thread.sleep(1000);
                final ResponseMessage responseMessage = ResponseMessage.build(msg)
                        .code(ResponseStatusCode.SERVER_ERROR)
                        .statusAttributeException(new RuntimeException()).create();
                ctx.channel().writeAndFlush(new TextWebSocketFrame(SERIALIZER.serializeResponseAsString(responseMessage)));
            } else if (msg.getRequestId().equals(settings.CLOSE_CONNECTION_REQUEST_ID) || msg.getRequestId().equals(settings.CLOSE_CONNECTION_REQUEST_ID_2)) {
                Thread.sleep(1000);
                ctx.channel().writeAndFlush(new CloseWebSocketFrame());
            }
            else if (msg.getRequestId().equals(settings.USER_AGENT_REQUEST_ID)) {
                ctx.channel().writeAndFlush(new TextWebSocketFrame(returnSimpleStringResponse(settings.USER_AGENT_REQUEST_ID, userAgent)));
            }
        }

        private String returnSingleVertexResponse(final UUID requestID) throws SerializationException {
            final TinkerGraph graph = TinkerFactory.createClassic();
            final GraphTraversalSource g = graph.traversal();
            final Vertex[] t = {g.V().limit(1).next()};

            return SERIALIZER.serializeResponseAsString(ResponseMessage.build(requestID).result(t).create());
        }

        /**
         * Packages a string message into a ResponseMessage, serializes it, and returns the serialized string
         * @throws SerializationException
         */
        private String returnSimpleStringResponse(final UUID requestID, String message) throws SerializationException {
            return SERIALIZER.serializeResponseAsString(ResponseMessage.build(requestID).result(message).create());
        }

        /**
         * Captures and stores User-Agent if included in header
         */
        @Override
        public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
            if(evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
                WebSocketServerProtocolHandler.HandshakeComplete handshake = (WebSocketServerProtocolHandler.HandshakeComplete) evt;
                HttpHeaders requestHeaders = handshake.requestHeaders();
                if(requestHeaders.contains(USER_AGENT_HEADER)) {
                    userAgent = requestHeaders.get(USER_AGENT_HEADER);
                }
                else {
                    ctx.fireUserEventTriggered(evt);
                }
            }
        }
    }
}
