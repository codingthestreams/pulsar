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
import org.apache.pulsar.broker.service.BrokerService;
import org.apache.pulsar.common.api.proto.BaseCommand;
import org.apache.pulsar.common.api.proto.CommandAssignTopic;
import org.apache.pulsar.common.api.proto.ServerError;
import org.apache.pulsar.common.protocol.Commands;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

public class RSocketHandler implements RSocket {

    private final BrokerService service;
    public RSocketHandler(BrokerService service) {
        this.service = service;
    }

    @Override
    public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
        return  Flux.from(payloads)
                .map(this::unmarshalCommand)
                .map(this::dispatchCommand);
    }

    protected BaseCommand handleAssignTopic(CommandAssignTopic assignTopic) {
        // TODO: Figure out the how the future/flux can work together. For now, block on the future.
        try {
            this.service.assignTopicAsync(assignTopic.getTopic()).join();
        } catch (Exception ex) {
            return Commands.newAssignTopicResponse(assignTopic.getRequestId(), ServerError.UnknownError,
                    ex.getMessage());
        }
        return Commands.newAssignTopicResponse(assignTopic.getRequestId(), null, null);
    }

    protected BaseCommand unmarshalCommand(Payload payload) {
        int cmdSize = (int) payload.data().readUnsignedInt();
        final BaseCommand cmd = new BaseCommand();
        cmd.parseFrom(payload.data(), cmdSize);
        return cmd;
    }

    protected Payload dispatchCommand(BaseCommand cmd) {
        switch (cmd.getType()) {
            case ASSIGN_TOPIC -> {
                checkArgument(cmd.hasAssignTopic());
                return DefaultPayload.create(Commands.serializeWithSize(handleAssignTopic(cmd.getAssignTopic())));
            }
            default -> throw new UnsupportedOperationException("Operation for command " + cmd.getType()
                    + " is not supported");
        }
    }
}
