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

import java.net.InetAddress;
import java.nio.ByteBuffer;

/**
 *
 * @author jts
 */
public class Encoding {

    public static ByteBuffer byteEncode(String msg) {
        byte[] str = msg.getBytes(Chat.charset);
        byte[] buf = new byte[str.length + 1];
        System.arraycopy(str, 0, buf, 0, str.length);
        buf[str.length] = -1;
        return ByteBuffer.wrap(buf);
    }

    public static byte[] encodeInfoBuffer(String name, int port) {
        byte[] nameBuffer = name.getBytes(Chat.charset);
        byte[] infoBuffer = new byte[2 + 1 + nameBuffer.length];
        infoBuffer[0] = (byte) ((port >> 8) & 0xFF);
        infoBuffer[1] = (byte) ((port) & 0xFF);
        infoBuffer[2] = (byte) nameBuffer.length;
        System.arraycopy(nameBuffer, 0, infoBuffer, 3, nameBuffer.length);
        return infoBuffer;
    }

    public static ServerInfo decodeInfoBuffer(InetAddress source, byte[] infoBuffer) {
        int port = ((infoBuffer[0] & 0xFF) << 8) | (infoBuffer[1] & 0xFF);
        byte[] nameBuffer = new byte[infoBuffer[2]];
        System.arraycopy(infoBuffer, 3, nameBuffer, 0, nameBuffer.length);
        ServerInfo server = new ServerInfo(new String(nameBuffer, Chat.charset), source, port);
        return server;
    }
}
