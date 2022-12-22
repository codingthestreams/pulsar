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
package org.apache.pulsar.broker.service.rsocket;

import static com.google.common.base.Preconditions.checkArgument;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.util.DefaultPayload;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.pulsar.broker.service.BrokerService;
import org.apache.pulsar.common.api.proto.BaseCommand;
import org.apache.pulsar.common.api.proto.CommandAssignTopic;
import org.apache.pulsar.common.api.proto.ServerError;
import org.apache.pulsar.common.protocol.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class RSocketHandler implements RSocket {

    private static final Logger log = LoggerFactory.getLogger(RSocketHandler.class);

    private final BrokerService service;
    private final String connectionId;
    private final RSocket sendingSocket;
    /**
     * In-flight requests map: Request Id ==> Connection Id.
     */
    private final ConcurrentMap<Long, String> inFlightRequests;
    public RSocketHandler(BrokerService service, String connectionId, RSocket sendingSocket) {
        this.service = service;
        this.connectionId = connectionId;
        this.sendingSocket = sendingSocket;
        this.inFlightRequests = new ConcurrentHashMap<>();
    }

    @Override
    public Mono<Void> fireAndForget(Payload payload) {
        final BaseCommand cmd = unmarshalCommand(payload);
        dispatchCommand(cmd);
        return Mono.empty();
    }

    protected void handleAssignTopic(CommandAssignTopic assignTopic) {
        //TODO: Switch to a non-blocking model by handling eventual consistency for assign topic workflow
        this.inFlightRequests.put(assignTopic.getRequestId(), this.connectionId);
        this.service.assignTopicAsync(assignTopic.getTopic()).whenComplete((__, ex) -> {
            final BaseCommand cmd;
            if (ex != null) {
                cmd = Commands.newAssignTopicResponse(assignTopic.getRequestId(), ServerError.UnknownError,
                        ex.getMessage());
            } else {
                cmd = Commands.newAssignTopicResponse(assignTopic.getRequestId(), null, null);
            }

            final Payload assignTopicResponse =
                    DefaultPayload.create(Commands.serializeWithSize(cmd));
            this.sendingSocket.fireAndForget(assignTopicResponse).subscribe(null,
                    e -> log.error("failed to send fnf response to client {}", connectionId, e));
        }).join();
    }

    protected BaseCommand unmarshalCommand(Payload payload) {
        int cmdSize = (int) payload.data().readUnsignedInt();
        final BaseCommand cmd = new BaseCommand();
        cmd.parseFrom(payload.data(), cmdSize);
        return cmd;
    }

    protected void dispatchCommand(BaseCommand cmd) {
        switch (cmd.getType()) {
            case ASSIGN_TOPIC -> {
                checkArgument(cmd.hasAssignTopic());
                handleAssignTopic(cmd.getAssignTopic());
            }
            default -> throw new UnsupportedOperationException("Operation for command " + cmd.getType()
                    + " is not supported");
        }
    }
}
