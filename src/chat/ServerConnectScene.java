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
public class ServerConnectScene extends Scene {

    private static VBox init() {

        TextField address = new TextField();
        address.setPromptText("Server Address");
        address.setMaxWidth(200);

        TextField port = new TextField("50505");
        port.setPromptText("Server Port");
        port.setMaxWidth(200);

        TextField password = new TextField();
        password.setPromptText("Password");
        password.setMaxWidth(200);

        Button connect = new Button("Connect");
        connect.setOnAction((ActionEvent e) -> {
            try {
                Chat.setScene(new ServerScene(password.getText(), InetAddress.getByName(address.getText()), Integer.parseInt(port.getText())));
            } catch (IOException ioe) {
                System.err.println("Failed to connect to server: " + ioe.getLocalizedMessage());
            }
        });

        Button cancel = new Button("Cancel");
        cancel.setOnAction((ActionEvent e) -> {
            Chat.setScene(new MainScene());
        });

        VBox layout = new VBox(8);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(address, port, password, connect, cancel);

        return layout;
    }

    public ServerConnectScene() {
        super(init(), 600, 400);
    }
}
