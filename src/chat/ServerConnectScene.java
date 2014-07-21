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
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author jts
 */
public class ServerConnectScene extends ChatScene {

    {
        name = "Direct Connect";
    }
    
    public ServerConnectScene() {
        super(new VBox(), 600, 400);
        
        TextField addressField = new TextField();
        addressField.setPromptText("Server Address");
        addressField.setMaxWidth(200);

        TextField portField = new TextField("50505");
        portField.setPromptText("Server Port");
        portField.setMaxWidth(200);

        TextField passwordField = new TextField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(200);
        
        VBox main = new VBox(8);
        main.setAlignment(Pos.CENTER);
        VBox.setVgrow(main, Priority.ALWAYS);
        main.getChildren().addAll(addressField, portField, passwordField);
        
        Button connectButton = new Button("Connect");
        connectButton.setPrefWidth(100);
        connectButton.setOnAction((ActionEvent e) -> {
            try {
                Chat.pushScene(new ServerScene(passwordField.getText(), InetAddress.getByName(addressField.getText()), Integer.parseInt(portField.getText())));
            } catch (IOException ioe) {
                System.err.println("Failed to connect to server: " + ioe.getLocalizedMessage());
            }
        });

        HBox controls = ChatScene.defaultControls();
        controls.getChildren().add(0, connectButton);
        
        VBox root = (VBox) this.getRoot();
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(main, controls);
    }

    @Override
    public void shutdown() {

    }
}
