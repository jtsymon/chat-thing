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


import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
/**
 *
 * @author jts
 */
public class ServerSource {
    
    private final StringProperty address;
    public void setAddress(String value) { addressProperty().set(value); }
    public String getAddress() { return addressProperty().get(); }
    public StringProperty addressProperty() {
        return address;
    }

    private final IntegerProperty port;
    public void setPort(Integer value) { portProperty().set(value); }
    public Integer getPort() { return portProperty().get(); }
    public IntegerProperty portProperty() { 
        return port;
    }
    
    private final BooleanProperty enabled;
    public void setEnabled(Boolean value) { enabledProperty().set(value); }
    public Boolean getEnabled() { return enabledProperty().get(); }
    public BooleanProperty enabledProperty() { 
        return enabled;
    }
    
    public final boolean isLocal;
    
    public final ObservableValue<ServerSource> observable = new SimpleObjectProperty<>(this);
    
    public ServerSource(String address, int port) {
        this.address = new SimpleStringProperty(address);
        this.port = new SimpleIntegerProperty(port);
        this.enabled = new SimpleBooleanProperty(false);
        this.isLocal = false;
    }
    
    private ServerSource() {
        this.address = new SimpleStringProperty("LAN");
        this.port = new SimpleIntegerProperty(Server.DEFAULT_PORT);
        this.enabled = new SimpleBooleanProperty(true);
        this.isLocal = true;
    }
    
    public static ServerSource getLAN() {
        return new ServerSource();
    }
}
