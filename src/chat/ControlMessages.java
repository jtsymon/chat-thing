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

/**
 *
 * @author jts
 */
public class ControlMessages {
    public static final byte CONTROL_MESSAGE_ID = 5;
    
    public static final byte USER_JOINED = 1;
    public static final byte USER_JOINED_SILENT = 2;
    public static final byte USER_LEFT = 3;
    public static final byte CHAT_NAME = 4;
    public static final byte USER_RENAME = 5;
    
    private static byte[] string_setting(String str, byte action) {
        byte[] name = str.getBytes(Chat.charset);
        byte[] buf = new byte[3 + name.length + 1];
        buf[0] = CONTROL_MESSAGE_ID;
        buf[1] = action;
        buf[2] = (byte)name.length;
        System.arraycopy(name, 0, buf, 3, name.length);
        buf[3 + name.length] = -1;
        return buf;
    }
    
    public static byte[] user_join(String username) {
        return string_setting(username, USER_JOINED);
    }
    
    public static byte[] user_join_silent(String username) {
        return string_setting(username, USER_JOINED_SILENT);
    }
    
    public static byte[] user_leave(String username) {
        return string_setting(username, USER_LEFT);
    }
    
    public static byte[] user_rename(String prevName, String newName) {
        return string_setting(prevName + "\0" + newName, USER_RENAME);
    }
    
    public static byte[] chat_name(String name) {
        return string_setting(name, CHAT_NAME);
    }
}
