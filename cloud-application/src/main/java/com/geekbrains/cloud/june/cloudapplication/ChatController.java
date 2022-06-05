package com.geekbrains.cloud.june.cloudapplication;

import com.geekbrains.cloud.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class ChatController implements Initializable {


    private String homeDir;

    @FXML
    public ListView<String> clientView;

    @FXML
    public ListView<String> serverView;

    private Network network;

    private void readLoop() {
        try {
            while (true) {
                CloudMessage message = network.read();
                if (message instanceof ListFiles listFiles) {
                    Platform.runLater(() -> {
                        serverView.getItems().clear();
                        serverView.getItems().addAll(listFiles.getFiles());
                    });
                } else if (message instanceof FileMessage fileMessage) {
                    Path current = Path.of(homeDir).resolve(fileMessage.getName());
                    Files.write(current, fileMessage.getData());
                    Platform.runLater(() -> {
                        clientView.getItems().clear();
                        clientView.getItems().addAll(getFiles(homeDir));
                    });
                } else if (message instanceof WarningMessage warningMessage) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.WARNING, warningMessage.getWarning(), ButtonType.OK);
                        alert.showAndWait();
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("Connection lost");
        }
    }

    // post init fx fields
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            homeDir = "client_files";
            clientView.getItems().clear();
            clientView.getItems().addAll(getFiles(homeDir));
            network = new Network(8189);
            Thread readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        clientView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    Path path =Paths.get(homeDir).resolve(clientView.getSelectionModel().getSelectedItem().toString());
                    if (path.toFile().isFile()) {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "It is not a Directory!", ButtonType.OK);
                        alert.showAndWait();
                    } else {
                        homeDir = path.toString();
                        clientView.getItems().clear();
                        clientView.getItems().addAll(getFiles(homeDir));
                    }
                }
            }
        });

        serverView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() ==2) {
                    String dir = serverView.getSelectionModel().getSelectedItem();
                    try {
                        network.write(new PathInRequest(dir));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private List<String> getFiles(String dir) {
        String[] list = new File(dir).list();
        assert list != null;
        return Arrays.asList(list);
    }

    public void upload(ActionEvent actionEvent) throws IOException {
        String file = clientView.getSelectionModel().getSelectedItem();
        network.write(new FileMessage(Path.of(homeDir).resolve(file)));
    }

    public void download(ActionEvent actionEvent) throws IOException {
        String file = serverView.getSelectionModel().getSelectedItem();
        network.write(new FileRequest(file));
    }

    public void upDirAction(ActionEvent actionEvent) throws IOException {
        if (clientView.isFocused()) {
            if (Paths.get(homeDir).getParent() == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Cannot go up.\n This is root directory!", ButtonType.OK);
                alert.showAndWait();
            } else {
            homeDir = Paths.get(homeDir).getParent().toString();
                clientView.getItems().clear();
                clientView.getItems().addAll(getFiles(homeDir));
            }
        }
        if (serverView.isFocused()) {
            String upDir = serverView.getSelectionModel().getSelectedItem();
            network.write(new PathUpRequest(upDir));
        }
    }
}