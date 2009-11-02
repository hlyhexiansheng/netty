/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.netty.example.uptime;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;


/**
 * Connects to a server periodically to measure and print the uptime of the
 * server.  This example demonstrates how to implement reliable reconnection
 * mechanism in Netty.
 *
 * @author The Netty Project (netty-dev@lists.jboss.org)
 * @author Trustin Lee (trustin@gmail.com)
 *
 * @version $Rev: 1685 $, $Date: 2009-08-28 16:15:49 +0900 (Fri, 28 Aug 2009) $
 */
public class UptimeClient {

    // Sleep 5 seconds before a reconnection attempt.
    static final int RECONNECT_DELAY = 5;

    // Reconnect when the server sends nothing for 10 seconds.
    private static final int READ_TIMEOUT = 10;

    public static void main(String[] args) throws Exception {
        // Print usage if no argument is specified.
        if (args.length != 2) {
            System.err.println(
                    "Usage: " + UptimeClient.class.getSimpleName() +
                    " <host> <port>");
            return;
        }

        // Parse options.
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        // Initialize the timer that schedules subsequent reconnection attempts.
        Timer timer = new HashedWheelTimer();

        // Configure the client.
        ClientBootstrap bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        bootstrap.getPipeline().addLast(
                "timeout", new ReadTimeoutHandler(timer, READ_TIMEOUT));
        bootstrap.getPipeline().addLast(
                "handler", new UptimeClientHandler(bootstrap, timer));

        bootstrap.setOption(
                "remoteAddress", new InetSocketAddress(host, port));

        // Initiate the first connection attempt - the rest is handled by
        // UptimeClientHandler.
        bootstrap.connect();
    }
}