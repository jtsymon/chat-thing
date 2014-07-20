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
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 *
 * @author jts
 */
public class ServerScene extends ChatScene {

    public class ServerConnection extends Thread {

        private final Socket socket;
        private boolean _running = true;
        private final String password;

        public ServerConnection(InetAddress addr, String password, int port) throws IOException {
            this.socket = new Socket(addr, port);
            this.socket.setSoTimeout(100);
            this.password = password;
            this.start();
        }

        private void handleMessage(byte[] buf, int n) {
            if (buf[0] == ControlMessages.CONTROL_MESSAGE_ID) {

                int length = buf[2];

                switch (buf[1]) {
                    case ControlMessages.USER_JOINED:
                    case ControlMessages.USER_JOINED_SILENT:
                        final String joined_name = new String(buf, 3, length, Chat.charset);
                        Platform.runLater(() -> {
                            ServerScene.this.peers.add(joined_name);
                            if (buf[1] == ControlMessages.USER_JOINED) {
                                ServerScene.this.messages.appendText(joined_name + " joined the chat\n");
                            }
                        });
                        break;
                    case ControlMessages.USER_LEFT:
                        final String left_name = new String(buf, 3, length, Chat.charset);
                        Platform.runLater(() -> {
                            ServerScene.this.peers.remove(left_name);
                            ServerScene.this.messages.appendText(left_name + " left the chat\n");
                        });
                        break;
                    case ControlMessages.CHAT_NAME:
                        final String chat_name = new String(buf, 3, length, Chat.charset);
                        Platform.runLater(() -> {
                            ServerScene.this.serverName.setText(chat_name);
                        });
                        break;
                    case ControlMessages.USER_RENAME:
                        for (int i = 3; i < n; i++) {
                            if (buf[i] == 0) {
                                final String prevName = new String(buf, 3, i - 3, Chat.charset);
                                final String newName = new String(buf, i + 1, length - i + 2, Chat.charset);
                                Platform.runLater(() -> {
                                    int index = ServerScene.this.peers.indexOf(prevName);
                                    ServerScene.this.peers.set(index, newName);
                                    ServerScene.this.messages.appendText(prevName + " changed their name to " + newName + "\n");
                                });
                                break;
                            }
                        }
                }
            } else {
                final String message = new String(buf, 0, n, Chat.charset);
                Platform.runLater(() -> {
                    ServerScene.this.messages.appendText(message + "\n");
                });
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
                Chat.popScene();
            }
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

    private final ObservableList<String> peers = FXCollections.observableArrayList();
    public final ServerConnection serverConnection;
    private final TextArea messages;
    private final Label serverName;

    public ServerScene(String password, InetAddress addr, int port) throws IOException {
        super(new VBox(), 600, 400);

        if (password.length() > Server.MAX_PASSWORD_LENGTH) {
            throw new IOException("Server password too long!");
        }

        this.serverConnection = new ServerConnection(addr, password, port);

        this.messages = new TextArea("");
        this.messages.setEditable(false);
        VBox.setVgrow(this.messages, Priority.ALWAYS);
        
        VBox sidebar = new VBox();
        sidebar.setMaxWidth(200);
        sidebar.setPrefWidth(200);
        sidebar.setAlignment(Pos.TOP_RIGHT);
        serverName = new Label();
        serverName.setPrefWidth(200);
        serverName.setPadding(new Insets(5));
        serverName.setFont(Font.font("sans-serif", FontWeight.BOLD, 12));
        ListView<String> peersView = new ListView<>(this.peers);
        VBox.setVgrow(peersView, Priority.ALWAYS);
        sidebar.getChildren().addAll(serverName, peersView);
        
        HBox main = new HBox();
        HBox.setHgrow(this.messages, Priority.ALWAYS);
        HBox.setHgrow(sidebar, Priority.NEVER);
        VBox.setVgrow(main, Priority.ALWAYS);
        main.getChildren().addAll(this.messages, sidebar);
        
        
        TextField inputField = new TextField();
        inputField.setOnAction((ActionEvent e) -> {
            if (inputField.getText().trim().length() > 0) {
                this.serverConnection.send(inputField.getText());
            }
            inputField.setText("");
        });
        HBox.setHgrow(inputField, Priority.ALWAYS);
        HBox controls = ChatScene.defaultControls();
        controls.getChildren().add(0, inputField);
        
        ((VBox) this.getRoot()).getChildren().addAll(main, controls);
    }

    @Override
    public void shutdown() {
        this.serverConnection.shutdown();
    }
}
