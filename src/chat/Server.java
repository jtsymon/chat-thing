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
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jts
 */
public class Server extends Thread {

    public static final byte[] MAGIC_PACKET = {'#', 'y', 'o', 'l', 'o', 's', 'w', 'a', 'g'};
    public static final int MAX_NAME_LENGTH = 32;
    public static final int MAX_PASSWORD_LENGTH = 254;
    public static final int DEFAULT_PORT = 50505;
    public static final InetAddress BROADCAST_IP;

    static {
        InetAddress tmp = null;
        try {
            tmp = InetAddress.getByName("239.6.6.6");
        } catch (UnknownHostException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, "Failed to get broadcast IP");
            System.exit(1);
        }
        BROADCAST_IP = tmp;
    }

    /**
     * Listens for broadcast packets requesting information about servers
     */
    private class BroadcastListener extends Thread {

        private final MulticastSocket listener;
        private final DatagramSocket socket;

        public BroadcastListener() throws IOException {
            this.listener = new MulticastSocket(DEFAULT_PORT);
            this.listener.setSoTimeout(100);
            this.socket = new DatagramSocket();
        }

        @Override
        public void run() {
            try {
                this.listener.joinGroup(BROADCAST_IP);
                MULTICAST_LISTEN:
                while (Server.this._running) {
                    try {
                        DatagramPacket dp = new DatagramPacket(new byte[MAGIC_PACKET.length], MAGIC_PACKET.length);
                        this.listener.receive(dp);
                        byte[] packet = dp.getData();
                        if (dp.getLength() != MAGIC_PACKET.length) {
                            continue;
                        }
                        for (int i = 0; i < MAGIC_PACKET.length; i++) {
                            if (MAGIC_PACKET[i] != packet[i]) {
                                continue MULTICAST_LISTEN;
                            }
                        }
                        dp = new DatagramPacket(Server.this.infoBuffer, Server.this.infoBuffer.length, dp.getAddress(), dp.getPort());
                        this.socket.send(dp);
                    } catch (SocketTimeoutException e) {
                        // do nothing (timeouts are enabled so we can stop the server cleanly)
                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private class PublicPhoneHome extends Thread {
        
        private final List<ServerSource> sources;
        
        public PublicPhoneHome(List<ServerSource> sources) {
            this.sources = sources;
        }
        
        @Override
        public void run() {
            while (Server.this._running) {
                try {
                String info = "?name=" + URLEncoder.encode(Server.this.name, "UTF-8") + "&port=" +
                        URLEncoder.encode(Integer.toString(Server.this.port), "UTF-8");
                    for (ServerSource source : this.sources) {
                        try {
                            HttpURLConnection connection = (HttpURLConnection)new URL("http://" + source.getAddress() + info).openConnection();
                            connection.getResponseCode();
                        } catch (IOException ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, "Failed to URL encode server info");
                    break;
                }
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private static class User {

        public boolean identified = false;
        public String username;
        public final String address;
        public ByteBuffer buffer = ByteBuffer.allocate(1024);

        public User(Socket socket) {
            this.address = (new StringBuilder(socket.getInetAddress().toString())).append(":").append(socket.getPort()).toString();
        }
    }

    private String name;
    private byte[] infoBuffer;
    private byte[] passBuffer;
    public final int port;
    private final BroadcastListener multicastListener;
    private final PublicPhoneHome publicPhoneHome;
    private final ServerSocketChannel ssc;
    private final Selector selector;
    private String[] welcomeTokens = new String[]{"Welcome to ", "!"};
    private ByteBuffer welcomeBuf;
    private boolean _running = true;

    public Server(String name, String password, boolean isPublic) throws IOException {
        super();
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IOException("Server name too long!");
        }
        if (password.length() > MAX_PASSWORD_LENGTH) {
            throw new IOException("Server password too long!");
        }
        this.passBuffer = password.getBytes(Chat.charset);
        this.multicastListener = new BroadcastListener();
        this.ssc = ServerSocketChannel.open();
        try {
            // try default port
            this.ssc.socket().bind(new InetSocketAddress(DEFAULT_PORT));
        } catch (IOException e) {
            this.ssc.socket().bind(null);
        }
        this.port = this.ssc.socket().getLocalPort();
        this.updateName(name);
        this.generateWelcomeMessage();
        this.ssc.configureBlocking(false);
        this.selector = Selector.open();
        this.ssc.register(selector, SelectionKey.OP_ACCEPT);
        this.start();
        this.multicastListener.start();
        if (isPublic) {
            // TODO: implement some way of adding sources for the server to update
            // maybe make the server browser's list global?
            List<ServerSource> sources = new LinkedList<>();
            sources.add(new ServerSource("jt-symon.rhcloud.com/endpoint/chat"));
            this.publicPhoneHome = new PublicPhoneHome(sources);
            this.publicPhoneHome.start();
        } else {
            this.publicPhoneHome = null;
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
        User user = new User(sc.socket());
        sc.configureBlocking(false);
        sc.register(this.selector, SelectionKey.OP_READ, user);
        System.out.println("Accepted connection from: " + user.address);
    }

    private void killUser(SelectionKey key) throws IOException {
        ((SocketChannel) key.channel()).socket().close();
        key.channel().close();
        User user = (User) key.attachment();
        if (user.identified) {
            broadcastRaw(ControlMessages.user_leave(user.username));
        } else {
            System.out.println(user.address + " failed to identify themselves");
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel ch = (SocketChannel) key.channel();
        User user = (User) key.attachment();

        int pos = user.buffer.position();
        int read = 0;
        while ((read = ch.read(user.buffer)) > 0) {
            byte[] buf = user.buffer.array();

            for (int i = pos, end = pos + read; i < end; i++) {
                if (buf[i] == -1) {
                    buf[i] = 0;
                    // handle any remainder in the buffer after the terminator (next message)
                    if (++i < end) {
                        end -= i;
                        byte[] remainder = new byte[end];
                        user.buffer.position(i);
                        user.buffer.get(remainder);
                        user.buffer.position(i);
                        if (user.identified) {
                            handleMessage(user, key);
                        } else {
                            identifyUser(user, key);
                        }
                        user.buffer.clear();
                        user.buffer.put(remainder);
                        i = 0;
                    } else {
                        if (user.identified) {
                            handleMessage(user, key);
                        } else {
                            identifyUser(user, key);
                        }
                        break;
                    }
                }
            }
        }
        if (user.buffer.remaining() == 0) {
            tell(key, "SYSTEM: Your message was too long");
            user.buffer.clear();
        }
        if (read < 0) {
            killUser(key);
        }
    }

    private void generateWelcomeMessage() {
        StringBuilder welcome = new StringBuilder();
        for (int i = 0; i < this.welcomeTokens.length - 1; i++) {
            welcome.append(this.welcomeTokens[i]).append(this.name);
        }
        welcome.append(this.welcomeTokens[this.welcomeTokens.length - 1]);
        this.welcomeBuf = Encoding.byteEncode(welcome.toString());
        System.out.println("Welcome message = '" + welcome.toString() + "'");
    }

    private void updateName(String name) {
        this.name = name;
        this.infoBuffer = Encoding.encodeInfoBuffer(this.name, this.port);
        this.generateWelcomeMessage();
    }

    private void handleMessage(User user, SelectionKey key) throws IOException {
        user.buffer.flip();
        byte[] data = new byte[user.buffer.limit() - 1];
        user.buffer.get(data);
        if (data.length > 0) {
            // check for special codes
            if (data[0] == ControlMessages.CONTROL_MESSAGE_ID) {
                switch (data[1]) {
                    case 1: // user list
                        for (SelectionKey other : selector.keys()) {
                            if (!other.equals(key) && other.isValid() && other.channel() instanceof SocketChannel) {
                                User otherUser = (User) other.attachment();
                                if (otherUser.identified) {
                                    tellRaw(key, ControlMessages.user_join_silent(otherUser.username));
                                }
                            }
                        }
                        break;
                }
                // check for user commands
            } else if (data[0] == '/') {
                String[] command = new String(data, Chat.charset).split("\\s+", 2);
                switch (command[0].toLowerCase()) {
                    case "/welcome":
                        this.welcomeTokens = command[1].split("\\$name", -1);
                        this.generateWelcomeMessage();
                        break;
                    case "/name":
                        if (command[1].length() > MAX_NAME_LENGTH) {
                            tell(key, "Cannot set the name (too long)");
                        } else {
                            this.updateName(command[1]);
                            broadcastRaw(ControlMessages.chat_name(this.name));
                        }
                        break;
                    case "/nick":
                        if (command[1].length() > MAX_NAME_LENGTH) {
                            tell(key, "Cannot set the nick (too long)");
                        } else {
                            broadcastRaw(ControlMessages.user_rename(user.username, command[1]));
                            user.username = command[1];
                        }
                }
            } else {
                this.broadcast(user.username + ": " + new String(data, Chat.charset));
            }
        }
        user.buffer.clear();
    }

    private void identifyUser(User user, SelectionKey key) throws IOException {
        user.buffer.flip();
        byte nameLength = user.buffer.get();
        if (nameLength > MAX_NAME_LENGTH) {
            killUser(key);
            return;
        }
        byte passLength = user.buffer.get();
        if (this.passBuffer.length > 0 && passLength != this.passBuffer.length) {
            killUser(key);
            return;
        }
        if (user.buffer.remaining() < nameLength + passLength) {
            killUser(key);
            return;
        }
        byte[] username = new byte[nameLength];
        user.buffer.get(username);
        user.username = new String(username, Chat.charset);
        for (SelectionKey other : selector.keys()) {
            if (!other.equals(key) && other.isValid() && other.channel() instanceof SocketChannel) {
                User otherUser = (User) other.attachment();
                if (otherUser.identified
                        && otherUser.username != null
                        && otherUser.username.equalsIgnoreCase(user.username)) {
                    killUser(key);
                    return;
                }
            }
        }
        if (this.passBuffer.length > 0) {
            byte[] pass = new byte[passLength];
            user.buffer.get(pass);
            if (!Arrays.equals(pass, this.passBuffer)) {
                killUser(key);
                return;
            }
        }
        user.buffer.clear();
        broadcastRaw(ControlMessages.user_join(user.username));
        user.identified = true;
        tellRaw(key, ControlMessages.chat_name(this.name));
        ((SocketChannel) key.channel()).write(welcomeBuf);
        welcomeBuf.rewind();
    }

    private void broadcast(String msg) throws IOException {
        System.out.println(msg);
        ByteBuffer msgBuf = Encoding.byteEncode(msg);
        for (SelectionKey key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel && ((User) key.attachment()).identified) {
                SocketChannel sch = (SocketChannel) key.channel();
                try {
                    sch.write(msgBuf);
                    msgBuf.rewind();
                } catch (IOException ex) {
                    // socket is closed / broken
                    killUser(key);
                }
            }
        }
    }

    private void broadcastRaw(byte[] buf) throws IOException {
        ByteBuffer msgBuf = ByteBuffer.wrap(buf);
        for (SelectionKey key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                SocketChannel sch = (SocketChannel) key.channel();
                try {
                    sch.write(msgBuf);
                    msgBuf.rewind();
                } catch (IOException ex) {
                    // socket is closed / broken
                    killUser(key);
                }
            }
        }
    }

    private void tell(SelectionKey key, String msg) throws IOException {
        ByteBuffer msgBuf = Encoding.byteEncode(msg);
        SocketChannel sch = (SocketChannel) key.channel();
        try {
            sch.write(msgBuf);
        } catch (IOException ex) {
            // socket is closed / broken
            killUser(key);
        }
    }

    private void tellRaw(SelectionKey key, byte[] buf) throws IOException {
        SocketChannel sch = (SocketChannel) key.channel();
        try {
            sch.write(ByteBuffer.wrap(buf));
        } catch (IOException ex) {
            // socket is closed / broken
            killUser(key);
        }
    }

    @Override
    public void run() {
        System.out.println("Server starting on port " + this.port);
        Iterator<SelectionKey> iter;
        SelectionKey key;
        while (this.ssc.isOpen()) {
            try {
                this.selector.select();
                iter = this.selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    key = iter.next();
                    iter.remove();
                    try {
                        if (key.isAcceptable()) {
                            this.handleAccept(key);
                        }
                        if (key.isReadable()) {
                            this.handleRead(key);
                        }
                    } catch (CancelledKeyException | ClosedChannelException e) {
                        killUser(key);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void shutdown() {
        try {
            this._running = false;
            System.err.println("Waiting for server to exit...");
            this.selector.wakeup();
            this.ssc.socket().close();
            this.ssc.close();
            synchronized (this.selector) {
                for (SelectionKey key : this.selector.keys()) {
                    SelectableChannel channel = key.channel();
                    if (channel instanceof SocketChannel) {
                        ((SocketChannel)key.channel()).socket().close();
                    }
                }
                this.selector.close();
            }
            this.join();
            this.multicastListener.join();
            if (this.publicPhoneHome != null) {
                this.publicPhoneHome.interrupt();
                this.publicPhoneHome.join();
            }
            System.err.println("Server exited cleanly");
        } catch (IOException|InterruptedException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
