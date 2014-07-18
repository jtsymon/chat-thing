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
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author jts
 * 
 * TODO: 
 * Implement error messages when a connection gets rejected (say why)
 * Implement broadcasting to find servers on LAN
 */
public class Chat extends Application {

    public static final Charset charset = Charset.forName("UTF-8");

    private static Chat _instance;
    private static String _username = "";
    private static Server _server;
    private Stage stage;

    public static String getUsername() {
        return _username;
    }

    public static Server initServer(String name, String password) throws IOException {
        _server = new Server(name, password);
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
        if (_instance.stage.getScene() instanceof ServerScene) {
            ((ServerScene) _instance.stage.getScene()).serverConnection.shutdown();
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

    public static void setScene(Scene scene) {
        _instance.stage.setScene(scene);
    }

    @Override
    public void start(Stage primaryStage) {
        _instance = this;
        this.stage = primaryStage;
        this.stage.setTitle("Hello World!");
        this.stage.setScene(new MainScene());
        this.stage.show();
    }

    @Override
    public void stop() {
        this.killServer();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
