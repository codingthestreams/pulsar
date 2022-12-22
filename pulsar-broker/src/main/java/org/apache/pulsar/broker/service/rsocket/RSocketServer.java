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

import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.transport.netty.server.TcpServerTransport;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.pulsar.broker.service.BrokerService;
/**
 * Reacts to incoming rsocket requests and engages with connected clients over a bidirectional channel.
 */
public class RSocketServer {

    private final BrokerService service;
    private final SocketAcceptor acceptor;
    private final ConcurrentHashMap<String, RSocket> clientConnections = new ConcurrentHashMap<>();
    public RSocketServer(BrokerService service) {
        this.service = service;
        this.acceptor = new RSocketAcceptor(service, clientConnections);
    }

    public void start() {
        io.rsocket.core.RSocketServer
                .create(this.acceptor)
                .bindNow(TcpServerTransport.create("localhost", 7500));
    }
}
