package org.apache.pulsar.discovery.service.rsocket;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.core.RSocketConnector;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.pulsar.common.api.proto.BaseCommand;
import org.apache.pulsar.common.api.proto.CommandAssignTopicResponse;
import org.apache.pulsar.common.protocol.Commands;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class BrokerClient {

    private final RSocket socket;
    private final Map<Long, CompletableFuture<Void>> topicAssignmentsFutures;

    public BrokerClient(Map<Long, CompletableFuture<Void>> topicAssignmentsFutures) {
        this.socket =
                RSocketConnector.connectWith(TcpClientTransport.create("localhost", 7500)).block();
        this.topicAssignmentsFutures = topicAssignmentsFutures;
    }

    public void sendCommand(BaseCommand cmd) {
        socket
                .requestChannel(Mono.just(DefaultPayload.create(Commands.serializeWithSize(cmd))))
                .map(this::unmarshalCommand)
                .doOnNext(this::handleCommand)
                .subscribe();
    }

    protected BaseCommand unmarshalCommand(Payload payload) {
        int cmdSize = (int) payload.data().readUnsignedInt();
        final BaseCommand cmd = new BaseCommand();
        cmd.parseFrom(payload.data(), cmdSize);
        return cmd;
    }

    protected void handleCommand(BaseCommand cmd) {
        switch (cmd.getType()) {
            case ASSIGN_TOPIC_RESPONSE -> {
                handleAssignTopicResponse(cmd.getAssignTopicResponse());
            }
            default -> throw new UnsupportedOperationException("Operation for command " + cmd.getType()
                    + " is not supported");        }
    }

    protected void handleAssignTopicResponse(CommandAssignTopicResponse cmd) {
        CompletableFuture<Void> assignTopicFuture = topicAssignmentsFutures.get(cmd.getRequestId());
        if (assignTopicFuture != null) {
            if (cmd.hasError()) {
                assignTopicFuture.completeExceptionally(new AssignTopicException(cmd.getMessage(), cmd.getError()));
            } else {
                assignTopicFuture.complete(null);
            }
        }
    }
}
