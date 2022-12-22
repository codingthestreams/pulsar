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

import io.rsocket.ConnectionSetupPayload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import java.util.concurrent.ConcurrentMap;
import org.apache.pulsar.broker.service.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class RSocketAcceptor implements SocketAcceptor {
    private static final Logger log = LoggerFactory.getLogger(RSocketServer.class);
    private final BrokerService service;
    private final ConcurrentMap<String, RSocket> clientConnections;
    public RSocketAcceptor(BrokerService service, ConcurrentMap<String, RSocket> clientConnections) {
        this.service = service;
        this.clientConnections = clientConnections;
    }

    @Override
    public Mono<RSocket> accept(ConnectionSetupPayload connectionSetupPayload, RSocket sendingSocket) {
        String id = connectionSetupPayload.getDataUtf8();
        log.info("Accepting a new client connection with ID {}", id);

        if (this.clientConnections.compute(
                id, (__, old) -> old == null || old.isDisposed() ? sendingSocket : old)
                == sendingSocket) {
            return  Mono.just(new RSocketHandler(service, id, sendingSocket));
        }

        return Mono.error(
                new IllegalStateException("There is already a client connected with the same ID"));
    }
}
