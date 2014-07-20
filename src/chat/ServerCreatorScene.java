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
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author jts
 */
public class ServerCreatorScene extends ChatScene {

    public ServerCreatorScene() {
        super(new VBox(), 600, 400);
        
        TextField serverNameField = new TextField();
        serverNameField.setPromptText("Server Name");
        serverNameField.setMaxWidth(200);
        
        TextField serverPasswordField = new TextField();
        serverPasswordField.setPromptText("Server Password");
        serverPasswordField.setMaxWidth(200);
        
        CheckBox publicCheckBox = new CheckBox("Public");
        
        VBox main = new VBox(8);
        main.setAlignment(Pos.CENTER);
        VBox.setVgrow(main, Priority.ALWAYS);
        main.getChildren().addAll(serverNameField, serverPasswordField, publicCheckBox);
        
        Button acceptButton = new Button("Accept");
        acceptButton.setPrefWidth(100);
        acceptButton.setOnAction((ActionEvent e) -> {
            String name = serverNameField.getText().trim();
            if (name.length() > 0) {
                String password = serverPasswordField.getText();
                boolean isPublic = publicCheckBox.isSelected();
                try {
                    int port = Chat.initServer(name, password, isPublic).port;
                    Chat.pushScene(new ServerScene(password, InetAddress.getLocalHost(), port));
                } catch (IOException ioe) {
                    System.err.println("Failed to create server: " + ioe.getLocalizedMessage());
                }
            }
        });
        
        HBox controls = ChatScene.defaultControls();
        controls.getChildren().add(0, acceptButton);
        
        VBox root = (VBox) this.getRoot();
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(main, controls);
    }

    @Override
    public void shutdown() {

    }
}
