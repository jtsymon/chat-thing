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

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 *
 * @author jts21
 */


public class ServerConnection extends Thread {
    
    public static interface CallbackString {
        public abstract void run(final String str);
    }
    
    public static interface CallbackBoolean {
        public abstract void run(boolean bool);
    }
    
    public static interface CallbackStringBoolean {
        public abstract void run(final String str, final boolean bool);
    }
    
    public static interface CallbackStringString {
        public abstract void run(final String str1, final String str2);
    }
    
    private final Socket socket;
        private boolean _running = true;
        private final String password;
        private final CallbackString messageCallback,
                userLeftCallback,
                chatNameCallback;
        private final CallbackStringBoolean userJoinedCallback;
        private final CallbackStringString userRenamedCallback;
        private final CallbackBoolean exitCallback;

        public ServerConnection(InetAddress addr, String password, int port,
                CallbackString messageCallback,
                CallbackStringBoolean userJoinedCallback,
                CallbackString userLeftCallback,
                CallbackString chatNameCallback,
                CallbackStringString userRenamedCallback,
                CallbackBoolean exitCallback) throws IOException {
            
            if (password.length() > Server.MAX_PASSWORD_LENGTH) {
                throw new IOException("Server password too long!");
            }
            
            this.socket = new Socket(addr, port);
            this.socket.setSoTimeout(100);
            this.password = password;
            this.messageCallback = messageCallback;
            this.userJoinedCallback = userJoinedCallback;
            this.userLeftCallback = userLeftCallback;
            this.chatNameCallback = chatNameCallback;
            this.userRenamedCallback = userRenamedCallback;
            this.exitCallback = exitCallback;
        }

        private void handleMessage(byte[] buf, int n) {
            if (buf[0] == ControlMessages.CONTROL_MESSAGE_ID) {

                int length = buf[2];

                switch (buf[1]) {
                    case ControlMessages.USER_JOINED:
                    case ControlMessages.USER_JOINED_SILENT:
                        this.userJoinedCallback.run(
                                new String(buf, 3, length, Chat.charset),
                                buf[1] == ControlMessages.USER_JOINED
                        );
                        break;
                    case ControlMessages.USER_LEFT:
                        this.userLeftCallback.run(
                                new String(buf, 3, length, Chat.charset)
                        );
                        break;
                    case ControlMessages.CHAT_NAME:
                        this.chatNameCallback.run(
                                new String(buf, 3, length, Chat.charset)
                        );
                        break;
                    case ControlMessages.USER_RENAME:
                        for (int i = 3; i < n; i++) {
                            if (buf[i] == 0) {
                                this.userRenamedCallback.run(
                                        new String(buf, 3, i - 3, Chat.charset),
                                        new String(buf, i + 1, length - i + 2, Chat.charset)
                                );
                                break;
                            }
                        }
                    break;
                }
            } else {
                this.messageCallback.run(new String(buf, 0, n, Chat.charset));
            }
        }

        @Override
        public void run() {
            try {
                InputStream is = this.socket.getInputStream();
                // identify with the server
                {
                    byte[] nameBuffer = Chat.getUsername().getBytes(Chat.charset);
                    byte[] passBuffer = this.password.getBytes(Chat.charset);
                    byte[] ident = new byte[1 + nameBuffer.length + 1 + passBuffer.length + 1];
                    ident[0] = (byte) nameBuffer.length;
                    ident[1] = (byte) passBuffer.length;
                    System.arraycopy(nameBuffer, 0, ident, 2, nameBuffer.length);
                    System.arraycopy(passBuffer, 0, ident, 2 + nameBuffer.length, passBuffer.length);
                    ident[1 + nameBuffer.length + 1 + passBuffer.length] = (byte) -1;
                    this.socket.getOutputStream().write(ident);
                }
                // ask for list of users
                this.socket.getOutputStream().write(new byte[]{5, 1, -1});
                byte[] buf = new byte[1024];
                byte[] swap = new byte[1024];
                int pos = 0;
                while (this._running) {
                    try {
                        int n = is.read(buf, pos, buf.length - pos);
                        if (n == -1) {
                            break;
                        }
                        for (int i = pos, end = pos + n; i < end; i++) {
                            if (buf[i] == -1) {
                                buf[i] = 0;
                                if (++i < end) {
                                    end -= i;
                                    System.arraycopy(buf, i, swap, 0, end);
                                    byte[] tmp = buf;
                                    handleMessage(buf, i);
                                    buf = swap;
                                    swap = tmp;
                                    i = 0;
                                } else {
                                    handleMessage(buf, i);
                                    break;
                                }
                            }
                        }
                    } catch (SocketTimeoutException e) {
                        // do nothing (timeouts are enabled so we can stop the server cleanly)
                    }
                }
            } catch (IOException e) {
                // do nothing (pop scene below)
            }
            this.exitCallback.run(this._running);
            Chat.killServer();
        }

        public void send(String message) {
            try {
                this.socket.getOutputStream().write(message.getBytes(Chat.charset));
                this.socket.getOutputStream().write(-1);
            } catch (IOException e) {
                System.err.println(e);
            }
        }

        public void shutdown() {
            this._running = false;
            try {
                this.socket.close();
                this.join();
            } catch (InterruptedException | IOException e) {
                System.err.println(e);
            }
        }
    }
