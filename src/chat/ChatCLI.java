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
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author jts21
 */


public class ChatCLI {
    
    private String chatName = "";
    private final ServerConnection serverConnection;
    private final List<String> peers = new LinkedList<>();
    
    public ChatCLI(String password, InetAddress addr, int port) throws IOException {
        this.serverConnection = new ServerConnection(addr, password, port,
                (final String message) -> {
                    System.out.println(message);
                }, (final String joinedName, final boolean display) -> {
                    this.peers.add(joinedName);
                    if (display) {
                        System.out.println(joinedName + " joined the chat");
                    }
                }, (final String leftName) -> {
                    this.peers.remove(leftName);
                    System.out.println(leftName + " left the chat");
                }, (final String newName) -> {
                    this.chatName = newName;
                }, (final String prevName, final String newName) -> {
                    int index = this.peers.indexOf(prevName);
                    this.peers.set(index, newName);
                    System.out.println(prevName + " changed their name to " + newName);
                }, (final boolean running) -> {
                    System.out.println("Server disconnected");
                    System.exit(0);
                }
        );
        this.serverConnection.start();
    }
    
    public void run() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        loop:
        while (true) {
            String line = reader.readLine();
            switch (line.toLowerCase()) {
                case "/quit":
                case "/q":
                case "/exit":
                    break loop;
                    
                case "/chat":
                    System.out.println("Server name: " + this.chatName);
                    break;
                    
                case "/peers":
                    synchronized(System.out) {
                        System.out.println("Peers:");
                        for (String peer : this.peers) {
                            System.out.println("    " + peer);
                        }
                    }
                    break;
                    
                default:
                    this.serverConnection.send(line);
                    break;
            }
        }
    }
}
