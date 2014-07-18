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
import javafx.scene.layout.VBox;

/**
 *
 * @author jts
 */
public class ServerBrowserScene extends Scene {

    private static VBox init() {
        Button back = new Button("Back");
        back.setOnAction((ActionEvent e) -> {
            Chat.setScene(new MainScene());
        });
        VBox layout = new VBox(8);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().add(back);
        return layout;
    }

    public ServerBrowserScene() {
        super(init(), 600, 400);
    }
}
