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
import java.net.InetAddress;
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
    
    @Override
    public String getName() {
        return "Chat - " + this.serverName.getText();
    }

    private final ObservableList<String> peers = FXCollections.observableArrayList();
    public final ServerConnection serverConnection;
    private final TextArea messages;
    private final Label serverName;

    public ServerScene(String password, InetAddress addr, int port) throws IOException {
        super(new VBox(), 600, 400);

        this.serverConnection = new ServerConnection(addr, password, port,
                (final String message) -> {
                    Platform.runLater(() -> {
                        this.messages.appendText(message + "\n");
                    });
                }, (final String joinedName, final boolean display) -> {
                    Platform.runLater(() -> {
                        this.peers.add(joinedName);
                        if (display) {
                            this.messages.appendText(joinedName + " joined the chat\n");
                        }
                    });
                }, (final String leftName) -> {
                    Platform.runLater(() -> {
                        this.peers.remove(leftName);
                        this.messages.appendText(leftName + " left the chat\n");
                    });
                }, (final String chatName) -> {
                    Platform.runLater(() -> {
                        this.serverName.setText(chatName);
                        Chat.setName();
                    });
                }, (final String prevName, final String newName) -> {
                    Platform.runLater(() -> {
                        int index = this.peers.indexOf(prevName);
                        this.peers.set(index, newName);
                        this.messages.appendText(prevName + " changed their name to " + newName + "\n");
                    });
                }, (final boolean running) -> {
                    if (running) {
                        Chat.popScene();
                    }
                }
        );
        this.serverConnection.start();

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
