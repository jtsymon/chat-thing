/*
 * Copyright (C) 2014 jts
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.ObservableList;

/**
 *
 * @author jts
 */
public class ServerFinder extends Thread {

    private static abstract class AbstractFinder extends Thread {

        protected boolean _running = true;

        public void shutdown() {
            this._running = false;
            try {
                this.join();
            } catch (InterruptedException ie) {
                System.out.println(ie);
            }
        }
    }

    private class LocalFinder extends AbstractFinder {

        @Override
        public void run() {
            try {
                // send broadcast asking for servers on the local network
                MulticastSocket socket = new MulticastSocket();
                socket.setSoTimeout(100);
                DatagramPacket packet = new DatagramPacket(Server.MAGIC_PACKET, Server.MAGIC_PACKET.length, Server.BROADCAST_IP, Server.DEFAULT_PORT);
                socket.send(packet);
                byte[] buf = new byte[1024];
                long startTime = System.currentTimeMillis();
                while (this._running && System.currentTimeMillis() < startTime + 10000) {
                    try {
                        packet = new DatagramPacket(buf, buf.length);
                        socket.receive(packet);
                        final ServerInfo server = Encoding.decodeInfoBuffer(packet.getAddress(), buf);
                        Platform.runLater(() -> {
                            ServerFinder.this.servers.add(server);
                        });
                    } catch (SocketTimeoutException e) {
                        // do nothing (timeouts are enabled so we can stop the finder cleanly)
                    }
                }
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }

    private class RemoteFinder extends AbstractFinder {

        private final ServerSource source;

        public RemoteFinder(ServerSource source) {
            this.source = source;
        }

        @Override
        public void run() {
            try {
                final List<ServerInfo> servers = new LinkedList<>();
                HttpURLConnection connection = (HttpURLConnection) (new URL("http://" + this.source.getAddress()).openConnection());
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        try {
                            String[] tokens = line.split("\t");
                            if (tokens.length != 3) {
                                throw new IOException("Invalid server: '" + line + "'");
                            } else {
                                ServerInfo server = new ServerInfo(tokens[0],
                                        InetAddress.getByName(tokens[1]),
                                        Integer.parseInt(tokens[2])
                                );
                                servers.add(server);
                            }
                        } catch (IOException e) {
                            System.err.println(e);
                        }
                    }
                }
                Platform.runLater(() -> {
                    ServerFinder.this.servers.addAll(servers);
                });
            } catch (MalformedURLException e) {
                System.err.println(e);
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }

    private final List<AbstractFinder> finders = new LinkedList<>();
    private final ObservableList<ServerInfo> servers;
    private final ObservableList<ServerSource> sources;

    public ServerFinder(ObservableList<ServerInfo> servers, ObservableList<ServerSource> sources) {
        this.servers = servers;
        this.sources = sources;
        this.start();
    }

    @Override
    public void run() {
        final List<ServerInfo> foundServers = new LinkedList<>();
        for (ServerSource source : this.sources) {
            if (!source.getEnabled()) {
                continue;
            }
            AbstractFinder finder = source.isLocal ? new LocalFinder() : new RemoteFinder(source);
            this.finders.add(finder);
            finder.start();
        }
    }

    public void shutdown() {
        try {
            this.finders.forEach(finder -> {
                finder.shutdown();
            });
            this.join();
        } catch (InterruptedException ie) {
            System.out.println(ie);
        }
    }
}
