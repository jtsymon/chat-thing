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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author jts
 */
public abstract class ChatScene extends Scene {
    
    public static HBox defaultControls() {
        HBox controls = new HBox(8);
        controls.setAlignment(Pos.CENTER_RIGHT);
        VBox.setVgrow(controls, Priority.NEVER);
        Button backButton = new Button("Back");
        backButton.setPrefWidth(100);
        backButton.setOnAction((ActionEvent e) -> {
            Chat.popScene();
        });
        controls.getChildren().add(backButton);
        return controls;
    }
    
    public ChatScene(Parent root, double width, double height) {
        super(root, width, height);
    }

    /**
     * Prepare to be closed, clean up any background threads
     */
    public abstract void shutdown();
}
