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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author jts
 */
public class ServerBrowserScene extends Scene {

    private static ObservableList<ServerSource> initSources() {
        List<ServerSource> wrap = new LinkedList<>();
        wrap.add(ServerSource.getLAN());
        return FXCollections.observableList(wrap);
    }

    private ObservableList<ServerInfo> servers = FXCollections.observableList(new LinkedList<ServerInfo>());
    private ObservableList<ServerSource> sources = initSources();
    private ServerFinder finder;

    public ServerBrowserScene() {
        super(new VBox(), 600, 400);

        TableView<ServerInfo> serverList = new TableView<>(this.servers);
        serverList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<ServerInfo, String> serverNameCol = new TableColumn<>("Server");
        serverNameCol.setCellValueFactory(new PropertyValueFactory("name"));
        TableColumn<ServerInfo, String> serverAddressCol = new TableColumn<>("Address");
        serverAddressCol.setCellValueFactory(new PropertyValueFactory("address"));
        TableColumn<ServerInfo, Integer> serverPortCol = new TableColumn<>("Port");
        serverPortCol.setCellValueFactory(new PropertyValueFactory("port"));
        serverPortCol.setMaxWidth(60);
        serverPortCol.setMinWidth(60);
        serverList.getColumns().addAll(serverNameCol, serverAddressCol, serverPortCol);

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

        ServerSource test = new ServerSource("jtsymon.tk", Server.DEFAULT_PORT);
        this.sources.add(test);
        test.portProperty().set(10202);

        HBox controls = new HBox(8);
        controls.setAlignment(Pos.CENTER_RIGHT);
        Button refresh = new Button("Refresh");
        refresh.setOnAction((ActionEvent e) -> {
            if (this.finder != null) {
                this.finder.shutdown();
            }
            this.servers.clear();
            this.finder = new ServerFinder(this.servers, this.sources);
        });
        refresh.fire();
        Button back = new Button("Back");
        back.setOnAction((ActionEvent e) -> {
            this.finder.shutdown();
            Chat.setScene(new MainScene());
        });
        controls.getChildren().addAll(refresh, back);
        VBox.setVgrow(serverList, Priority.ALWAYS);
        ((VBox) this.getRoot()).getChildren().addAll(serverList, sourceList, controls);
    }
}
