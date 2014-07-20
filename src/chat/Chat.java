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
import java.nio.charset.Charset;
import java.util.Stack;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 *
 * @author jts
 *
 * TODO: Implement error messages when a connection gets rejected (say why)
 */
public class Chat extends Application {

    public static final Charset charset = Charset.forName("UTF-8");

    private static Chat _instance;
    private static String _username = "";
    private static Server _server;
    private Stage stage;
    private static final Stack<ChatScene> _sceneStack = new Stack<>();

    public static String getUsername() {
        return _username;
    }

    public static Server initServer(String name, String password, boolean isPublic) throws IOException {
        _server = new Server(name, password, isPublic);
        return _server;
    }

    public static Server getServer() {
        return _server;
    }

    public static void killServer() {
        if (_server != null) {
            _server.shutdown();
            _server = null;
        }
    }

    public static boolean setUsername(String username) {
        username = username.trim();
        if (username.length() > 0) {
            _username = username;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Swaps the current scene for a new scene
     *
     * @param scene
     */
    public static void setScene(ChatScene scene) {
        _sceneStack.pop();
        _sceneStack.push(scene);
        _instance.stage.setScene(scene);
    }

    /**
     * Pushes the current scene onto the stack, and sets the scene to a new scene
     *
     * @param scene
     */
    public static void pushScene(ChatScene scene) {
        _sceneStack.push(scene);
        _instance.stage.setScene(scene);
    }

    /**
     * Sets the current scene to the scene from the top of the stack
     */
    public static void popScene() {
        _sceneStack.pop().shutdown();
        if (_sceneStack.empty()) {
            _sceneStack.push(new MainScene());
        }
        _instance.stage.setScene(_sceneStack.peek());
    }

    @Override
    public void start(Stage primaryStage) {
        _instance = this;
        this.stage = primaryStage;
        this.stage.setTitle("Hello World!");
        pushScene(new MainScene());
        this.stage.show();
    }

    @Override
    public void stop() {
        killServer();
        while (!_sceneStack.empty()) {
            _sceneStack.pop().shutdown();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
