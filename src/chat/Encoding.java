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
}
