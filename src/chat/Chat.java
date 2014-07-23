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

import com.sun.javafx.application.ParametersImpl;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import javafx.application.Application;
import javafx.application.Platform;
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
    
    public static void setName(String name) {
        if (Platform.isFxApplicationThread()) {
            _instance.stage.setTitle(name);
        } else {
            Platform.runLater(() -> {
                setName(name);
            });
        }
    }
    
    public static void setName() {
        if (Platform.isFxApplicationThread()) {
            setName(_sceneStack.peek().getName());
        } else {
            Platform.runLater(() -> {
                setName(_sceneStack.peek().getName());
            });
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
        if (Platform.isFxApplicationThread()) {
            _sceneStack.pop();
            _sceneStack.push(scene);
            _instance.stage.setScene(scene);
            setName();
        } else {
            Platform.runLater(() -> {
                setScene(scene);
            });
        }
    }

    /**
     * Pushes the current scene onto the stack, and sets the scene to a new scene
     *
     * @param scene
     */
    public static void pushScene(final ChatScene scene) {
        if (Platform.isFxApplicationThread()) {
            _sceneStack.push(scene);
            _instance.stage.setScene(_sceneStack.peek());
            setName();
        } else {
            Platform.runLater(() -> {
                pushScene(scene);
            });
        }
    }

    /**
     * Sets the current scene to the scene from the top of the stack
     */
    public static void popScene() {
        if (Platform.isFxApplicationThread()) {
            if (!_sceneStack.empty()) {
                _sceneStack.pop().shutdown();
            }
            if (_sceneStack.empty()) {
                _sceneStack.push(new MainScene());
            }
            _instance.stage.setScene(_sceneStack.peek());
            setName();
        } else {
            Platform.runLater(() -> {
                popScene();
            });
        }
    }

    @Override
    public void start(Stage primaryStage) {
        _instance = this;
        this.stage = primaryStage;
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
        Parameters params = new ParametersImpl(args);
        Map<String, String> named = params.getNamed();
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--help")) {
                System.err.println("--connect=<hostname>[:<port>] - automatically connect to a server on startup\n" + 
                                   "--password=<password>         - set the password for automatically connecting\n" +
                                   "--test=<count>                - open <count> connections to the server\n" +
                                   "--help                        - display this help message");
                System.exit(0);
            }
        }
        String username = named.get("username");
        if (username == null) username = "user_$random";
        String[] usernameTokens = username.split("\\$random", -1);
        if (usernameTokens.length > 1) {
            Random rnd = new Random();
            StringBuilder usernameBuilder = new StringBuilder(usernameTokens[0]);
            for (int i = 1; i < usernameTokens.length; i++) {
                usernameBuilder.append(Integer.toHexString(rnd.nextInt())).append(usernameTokens[i]);
            }
            Chat.setUsername(usernameBuilder.toString());
        } else {
            Chat.setUsername(username);
        }
        String password = named.get("password");
        if (password == null) password = "";
        String host = named.get("connect");
        if (host != null) {
            String[] serverTokens = host.split(":");
            try {
                int port = Server.DEFAULT_PORT;
                if (serverTokens.length == 2) {
                    port = Integer.parseInt(serverTokens[1]);
                }
                if (serverTokens.length <= 2) {
                    String test = named.get("test");
                    if (test != null) {
                        StressTest.Test(InetAddress.getByName(serverTokens[0]), port, password, Integer.parseInt(test));
                    } else {
                        new ChatCLI(password, InetAddress.getByName(serverTokens[0]), port).run();
                    }
                } else {
                    System.err.println("--connect: Expected host in form <hostname>[:<port>]");
                }
            } catch (IOException e) {
                System.err.println("--connect: Failed to connect to server!");
                System.err.println(e);
            }
            System.exit(0);
        }
        launch(args);
    }
}