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
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 *
 * @author jts
 */
public class ServerCreatorScene extends Scene {

    private static VBox init() {
        TextField serverName = new TextField();
        serverName.setPromptText("Server Name");
        serverName.setMaxWidth(200);
        TextField serverPassword = new TextField();
        serverPassword.setPromptText("Server Password");
        serverPassword.setMaxWidth(200);
        Button accept = new Button("Accept");
        accept.setOnAction((ActionEvent e) -> {
            String name = serverName.getText().trim();
            if (name.length() > 0) {
                String password = serverPassword.getText();
                try {
                    int port = Chat.initServer(name, password).port;
                    Chat.setScene(new ServerScene(password, InetAddress.getLocalHost(), port));
                } catch (IOException ioe) {
                    System.err.println("Failed to create server: " + ioe.getLocalizedMessage());
                }
            }
        });
        Button cancel = new Button("Cancel");
        cancel.setOnAction((ActionEvent e) -> {
            Chat.setScene(new MainScene());
        });
        VBox layout = new VBox(8);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(serverName, serverPassword, accept, cancel);
        return layout;
    }

    public ServerCreatorScene() {
        super(init(), 600, 400);
    }
}
