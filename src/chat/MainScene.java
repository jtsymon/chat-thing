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
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 *
 * @author jts
 */
public class MainScene extends ChatScene {

    public MainScene() {
        super(new VBox(8), 600, 400);
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(200);
        Button serverListButton = new Button("Servers");
        serverListButton.setOnAction((ActionEvent e) -> {
            if (Chat.setUsername(usernameField.getText())) {
                Chat.pushScene(new ServerBrowserScene());
            }
        });
        Button connectServerButton = new Button("Direct Connect");
        connectServerButton.setOnAction((ActionEvent e) -> {
            if (Chat.setUsername(usernameField.getText())) {
                Chat.pushScene(new ServerConnectScene());
            }
        });
        Button createServerButton = new Button("Create Server");
        createServerButton.setOnAction((ActionEvent e) -> {
            if (Chat.setUsername(usernameField.getText())) {
                Chat.pushScene(new ServerCreatorScene());
            }
        });
        
        VBox root = (VBox)this.getRoot();
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(usernameField, serverListButton, connectServerButton, createServerButton);
    }

    @Override
    public void shutdown() {

    }
}
