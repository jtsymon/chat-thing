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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jts21
 */
public class StressTest {

    public static void Test(InetAddress addr, int port, String password, int n) {
        List<ServerConnection> serverConnections = new LinkedList<>();
        try {
            for (int i = 0; i < n; i++) {
                final String name = "Test_" + i;
                final FileWriter writer = new FileWriter(new File("./" + name + ".txt"));
                Chat.setUsername(name);
                ServerConnection connection = new ServerConnection(addr, password, port,
                        (final String message) -> {
                            try {
                                writer.write(message + "\n");
                            } catch (IOException ex) {
                                Logger.getLogger(StressTest.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        },
                        (final String joinedName, final boolean display) -> {
                            if (display) {
                                try {
                                    writer.write(joinedName + " joined the chat" + "\n");
                                } catch (IOException ex) {
                                    Logger.getLogger(StressTest.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        },
                        (final String leftName) -> {
                            try {
                                writer.write(leftName + " left the chat" + "\n");
                            } catch (IOException ex) {
                                Logger.getLogger(StressTest.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        },
                        (final String newName) -> {
                            try {
                                writer.write("Chat name changed to " + newName + "\n");
                            } catch (IOException ex) {
                                Logger.getLogger(StressTest.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        },
                        (final String prevName, final String newName) -> {
                            try {
                                writer.write(prevName + " changed their name to " + newName + "\n");
                            } catch (IOException ex) {
                                Logger.getLogger(StressTest.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        },
                        (final boolean running) -> {
                            try {
                                writer.write(name + " Finished!" + "\n");
                                writer.close();
                            } catch (IOException ex) {
                                Logger.getLogger(StressTest.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            System.out.println(name + " Finished!");
                        });
                serverConnections.add(connection);
                connection.start();
                Thread.sleep(1);
            }
            System.in.read();
            for (ServerConnection connection : serverConnections) {
                connection.shutdown();
                Thread.sleep(1);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println(e);
        }
    }

    public static void main(String[] args) {
        try {
            Test(InetAddress.getLocalHost(), 50505, "", 100);
        } catch (UnknownHostException e) {
            System.err.println(e);
        }
    }
}
