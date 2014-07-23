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
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/**
 *
 * @author jts
 *
 * TODO: Add some way to add more remote server sources
 */
public class ServerBrowserScene extends ChatScene {
    
    {
        name = "Server Browser";
    }

    private class ClickableCellFactory<T> implements Callback<TableColumn<ServerInfo, T>, TableCell<ServerInfo, T>> {

        @Override
        public TableCell call(TableColumn p) {
            TableCell<ServerInfo, T> cell = new TableCell<ServerInfo, T>() {
                @Override
                public void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : getString());
                    setGraphic(null);
                }

                private String getString() {
                    return getItem() == null ? "" : getItem().toString();
                }
            };

            cell.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
                if (event.getClickCount() > 1) {
                    TableCell<ServerInfo, T> clicked = (TableCell) event.getSource();
                    ServerInfo server = (ServerInfo) clicked.getTableRow().getItem();
                    if (server != null) {
                        try {
                            server.connect(ServerBrowserScene.this.password.getValue());
                        } catch (IOException ioe) {
                            System.err.println("Failed to connect to server: " + ioe.getLocalizedMessage());
                        }
                    }
                }
            });
            return cell;
        }
    }

    private static ObservableList<ServerSource> initSources() {
        List<ServerSource> wrap = new LinkedList<>();
        wrap.add(ServerSource.getLAN());
        return FXCollections.observableList(wrap);
    }

    private ObservableList<ServerInfo> servers = FXCollections.observableList(new LinkedList<ServerInfo>());
    private ObservableList<ServerSource> sources = initSources();
    private ServerFinder finder;
    private StringProperty password = new SimpleStringProperty();

    public ServerBrowserScene() {
        super(new VBox(), 600, 400);

        TableView<ServerInfo> serverList = new TableView<>(this.servers);
        serverList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        serverList.setEditable(false);
        TableColumn<ServerInfo, String> serverNameCol = new TableColumn<>("Server");
        serverNameCol.setCellFactory(new ClickableCellFactory<>());
        serverNameCol.setCellValueFactory(new PropertyValueFactory("name"));
        TableColumn<ServerInfo, String> serverAddressCol = new TableColumn<>("Address");
        serverAddressCol.setCellFactory(new ClickableCellFactory<>());
        serverAddressCol.setCellValueFactory(new PropertyValueFactory("address"));
        TableColumn<ServerInfo, Integer> serverPortCol = new TableColumn<>("Port");
        serverPortCol.setCellFactory(new ClickableCellFactory<>());
        serverPortCol.setCellValueFactory(new PropertyValueFactory("port"));
        serverPortCol.setMaxWidth(60);
        serverPortCol.setMinWidth(60);
        serverList.getColumns().addAll(serverNameCol, serverAddressCol, serverPortCol);

        HBox serverControls = new HBox(8);
        serverControls.setAlignment(Pos.CENTER_LEFT);
        TextField passwordInput = new TextField("");
        passwordInput.setPromptText("Password");
        this.password.bind(passwordInput.textProperty());
        HBox.setHgrow(passwordInput, Priority.ALWAYS);
        Button connectButton = new Button("Connect");
        connectButton.setOnAction((ActionEvent e) -> {
            ServerInfo server = serverList.getSelectionModel().getSelectedItem();
            if (server != null) {
                try {
                    server.connect(this.password.getValue());
                } catch (IOException ioe) {
                    System.err.println("Failed to connect to server: " + ioe.getLocalizedMessage());
                }
            }
        });
        HBox.setHgrow(connectButton, Priority.NEVER);
        VBox.setVgrow(serverControls, Priority.NEVER);
        serverControls.getChildren().addAll(passwordInput, connectButton);

        TableView<ServerSource> sourceList = new TableView<>(this.sources);
        sourceList.setEditable(true);
        sourceList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<ServerSource, String> sourceAddressCol = new TableColumn<>("Address");
        sourceAddressCol.setCellValueFactory(new PropertyValueFactory("address"));
        TableColumn<ServerSource, Integer> sourcePortCol = new TableColumn<>("Port");
        sourcePortCol.setCellValueFactory(new PropertyValueFactory("port"));
        sourcePortCol.setMaxWidth(60);
        sourcePortCol.setMinWidth(60);
        TableColumn<ServerSource, Boolean> sourceEnabledCol = new TableColumn<>("");
        sourceEnabledCol.setCellValueFactory((CellDataFeatures<ServerSource, Boolean> p) -> p.getValue().enabledProperty());
        sourceEnabledCol.setCellFactory((TableColumn<ServerSource, Boolean> p) -> new CheckBoxTableCell<>());
        sourceEnabledCol.setEditable(true);
        sourceEnabledCol.setMaxWidth(30);
        sourceEnabledCol.setMinWidth(30);
        TableColumn<ServerSource, ServerSource> sourceRemoveCol = new TableColumn<>("");
        sourceRemoveCol.setSortable(false);
        sourceRemoveCol.setCellValueFactory((CellDataFeatures<ServerSource, ServerSource> p) -> p.getValue().observable);
        sourceRemoveCol.setCellFactory((TableColumn<ServerSource, ServerSource> p) -> new TableCell<ServerSource, ServerSource>() {
            final Button button = new Button();

            {
                button.setText("-");
                button.setPadding(new Insets(0, 8, 0, 8));
                button.setOnAction((ActionEvent e) -> {
                    ServerSource source = this.getItem();
                    if (!source.isLocal) {
                        System.out.println("Removed " + source.getAddress() + ":" + source.getPort());
                        ServerBrowserScene.this.sources.remove(source);
                    }
                });
            }

            @Override
            public void updateItem(final ServerSource source, boolean empty) {
                super.updateItem(source, empty);
                if (!empty && !this.getItem().isLocal) {
                    setGraphic(button);
                } else {
                    setGraphic(null);
                }
            }
        });
        sourceRemoveCol.setMaxWidth(30);
        sourceRemoveCol.setMinWidth(30);
        sourceList.getColumns().addAll(sourceAddressCol, sourcePortCol, sourceEnabledCol, sourceRemoveCol);

        ServerSource test = new ServerSource("jt-symon.rhcloud.com/endpoint/chat");
        this.sources.add(test);
        
        HBox controls = ChatScene.defaultControls();
        Button refresh = new Button("Refresh");
        refresh.setPrefWidth(100);
        refresh.setOnAction((ActionEvent e) -> {
            if (this.finder != null) {
                this.finder.shutdown();
            }
            this.servers.clear();
            this.finder = new ServerFinder(this.servers, this.sources);
        });
        refresh.fire();
        controls.getChildren().add(0, refresh);
        VBox.setVgrow(serverList, Priority.ALWAYS);
        ((VBox) this.getRoot()).getChildren().addAll(serverList, serverControls, sourceList, controls);
    }

    @Override
    public void shutdown() {
        this.finder.shutdown();
    }
}
