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
public class MainScene extends Scene {
    
    private static VBox init() {
        TextField username = new TextField(Chat.getUsername());
        username.setPromptText("Username");
        username.setMaxWidth(200);
        Button serverList = new Button("Servers");
        serverList.setOnAction((ActionEvent e) -> {
            if (Chat.setUsername(username.getText())) {
                Chat.setScene(new ServerBrowserScene());
            }
        });
        Button connectServer = new Button("Direct Connect");
        connectServer.setOnAction((ActionEvent e) -> {
            if (Chat.setUsername(username.getText())) {
                Chat.setScene(new ServerConnectScene());
            }
        });
        Button createServer = new Button("Create Server");
        createServer.setOnAction((ActionEvent e) -> {
            if (Chat.setUsername(username.getText())) {
                Chat.setScene(new ServerCreatorScene());
            }
        });
        VBox layout = new VBox(8);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(username, serverList, connectServer, createServer);
        return layout;
    }
    
    public MainScene() {
        super(init(), 600, 400);
    }
}
