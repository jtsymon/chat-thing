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
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author jts
 */


public class ServerInfo {

    private StringProperty name;
    public void setName(String value) { nameProperty().set(value); }
    public String getName() { return nameProperty().get(); }
    public StringProperty nameProperty() { 
        return name;
    }

    private StringProperty hostname;
    public void setAddress(String value) { addressProperty().set(value); }
    public String getAddress() { return addressProperty().get(); }
    public StringProperty addressProperty() {
        return hostname;
    }

    private IntegerProperty port;
    public void setPort(Integer value) { portProperty().set(value); }
    public Integer getPort() { return portProperty().get(); }
    public IntegerProperty portProperty() { 
        return port;
    }

    private InetAddress addr;
    public ServerInfo(InetAddress address, int port) {
        this.name = new SimpleStringProperty("...");
        this.addr = address;
        this.hostname = new SimpleStringProperty(address.getHostName());
        this.port = new SimpleIntegerProperty(port);
    }
}
