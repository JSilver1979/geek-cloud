package com.geekbrains.cloud.june.cloudapplication;

import com.geekbrains.cloud.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
    private final Image DIR_IMAGE = new Image("folder.png");
    private final Image FILE_IMAGE = new Image("file.png");
    @FXML
    public Button connectBttn;
    @FXML
    public PasswordField pwdField;
    @FXML
    public TextField loginField;

    private String homeDir;

    @FXML
    public ListView<String> clientView;

    @FXML
    public ListView<String> serverView;

    private Network network;

    private Stage renameStage;
    private RenameController renameController;

    private void readLoop() {
        try {
            while (true) {
                CloudMessage message = network.read();
                if (message instanceof AuthApprove authApprove) {
                    connectBttn.setVisible(false);
                    loginField.setEditable(false);
                    pwdField.setEditable(false);
                }
                else if (message instanceof ListFiles listFiles) {
                    Platform.runLater(() -> {
                        serverView.getItems().clear();
                        serverView.getItems().addAll(listFiles.getFiles());
                    });
                } else if (message instanceof FileMessage fileMessage) {
                    Path current = Path.of(homeDir).resolve(fileMessage.getName());
                    Files.write(current, fileMessage.getData());
                    Platform.runLater(() -> {
                        clientView.getItems().clear();
                        clientView.getItems().addAll(getFilesList(homeDir));
                    });
                } else if (message instanceof WarningMessage warningMessage) {
                    Platform.runLater(() -> {
                        getWarning(warningMessage.getWarning());
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
        homeDir = "client_files";
        clientView.getItems().clear();
        clientView.getItems().addAll(getFilesList(homeDir));

        setImages(clientView);
        setImages(serverView);
        
        clientView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    String fileName = trueFileName(clientView.getSelectionModel().getSelectedItem());
                    Path path = Paths.get(homeDir).resolve(fileName);
                    if (path.toFile().isFile()) {
                        getWarning("It is not a Directory");
                    } else {
                        homeDir = path.toString();
                        clientView.getItems().clear();
                        clientView.getItems().addAll(getFilesList(homeDir));
                    }
                }
            }
        });

        serverView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() ==2) {
                    String dir = trueFileName(serverView.getSelectionModel().getSelectedItem());
                    try {
                        network.write(new PathInRequest(dir));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private List<String> getFilesList(String dir) {
        String[] list = new File(dir).list();
        // trying to show is file or dir
        assert list != null;
        for (int i = 0; i < list.length; i++) {
            if (Path.of(dir).resolve(list[i]).toFile().isFile()) {
                list[i] = "F: " + list[i];
            } else {
                list[i] = "D: " + list[i];
            }
        }
        return Arrays.asList(list);
    }

    private void setImages(ListView lv) {
        lv.setCellFactory(param -> new ListCell<String>() {
            private ImageView imageView = new ImageView();
            @Override
            public void updateItem(String name, boolean empty) {

                super.updateItem(name, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String[] marker = name.split(" ",2);
                    if (marker[0].equals("D:")) {
                        imageView.setImage(DIR_IMAGE);
                        imageView.setFitHeight(20.0);
                        imageView.setFitWidth(20.0);
                    } else {
                        imageView.setImage(FILE_IMAGE);
                        imageView.setFitHeight(20.0);
                        imageView.setFitWidth(20.0);
                    }
                    setText(marker[1]);
                    setGraphic(imageView);
                }
            }
        });
    }

    private void getWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.showAndWait();
    }
    private String trueFileName (String str) {
        String[] splitter = str.split(": ", 2);
        return splitter[1];
    }

    public void upload(ActionEvent actionEvent) throws IOException {
        String file = trueFileName(clientView.getSelectionModel().getSelectedItem());
        network.write(new FileMessage(Path.of(homeDir).resolve(file)));
    }

    public void download(ActionEvent actionEvent) throws IOException {
        String file = trueFileName(serverView.getSelectionModel().getSelectedItem());
        network.write(new FileRequest(file));
    }

    public void upDirAction(ActionEvent actionEvent) throws IOException {
        if (clientView.isFocused()) {
            if (Paths.get(homeDir).getParent() == null) {
                getWarning("Cannot go up.\n This is root directory!");
            } else {
            homeDir = Paths.get(homeDir).getParent().toString();
                clientView.getItems().clear();
                clientView.getItems().addAll(getFilesList(homeDir));
            }
        }
        if (serverView.isFocused()) {
            String upDir = serverView.getSelectionModel().getSelectedItem();
            network.write(new PathUpRequest(upDir));
        }
    }

    public void deleteAction(ActionEvent actionEvent) throws IOException {
        if (clientView.isFocused()) {
            try {
                if (clientView.getSelectionModel().getSelectedItem() == null){
                    getWarning("Please, choose file to delete!");
                } else {
                    Files.deleteIfExists(Paths.get(homeDir)
                            .resolve(trueFileName(clientView.getSelectionModel().getSelectedItem())));
                }
            } catch (IOException e) {
                getWarning("Cannot delete file: " + trueFileName(clientView.getSelectionModel().getSelectedItem()));
            }

            clientView.getItems().clear();
                clientView.getItems().addAll(getFilesList(homeDir));
        }
        if (serverView.isFocused()) {
            if (serverView.getSelectionModel().getSelectedItem() == null) {
                getWarning("Please, choose file to delete!");
            } else {
                String fileToDelete = trueFileName(serverView.getSelectionModel().getSelectedItem());
                network.write(new DeleteRequest(fileToDelete));
            }
        }
    }

    public void renameAction(ActionEvent actionEvent) {
        try {

            if (clientView.isFocused()) {
                if (clientView.getSelectionModel().getSelectedItem() == null) {
                    getWarning("Please, choose file to rename!");
                } else {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/geekbrains/cloud/june/cloudapplication/rename.fxml"));
                    Parent root = fxmlLoader.load();

                    renameStage = new Stage();
                    renameStage.setTitle("Rename file");
                    renameStage.setScene(new Scene(root));
                    renameStage.initModality(Modality.APPLICATION_MODAL);
                    renameStage.initStyle(StageStyle.UTILITY);
                    renameStage.show();
                    renameController = fxmlLoader.getController();

                    renameController.setController(this, clientView, Paths.get(homeDir)
                            .resolve(trueFileName(clientView.getSelectionModel().getSelectedItem())));
                }
            }
            if (serverView.isFocused()) {
                if (serverView.getSelectionModel().getSelectedItem() == null) {
                    getWarning("Please, choose file to rename!");
                } else {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/geekbrains/cloud/june/cloudapplication/rename.fxml"));
                    Parent root = fxmlLoader.load();

                    renameStage = new Stage();
                    renameStage.setTitle("Rename file");
                    renameStage.setScene(new Scene(root));
                    renameStage.initModality(Modality.APPLICATION_MODAL);
                    renameStage.initStyle(StageStyle.UTILITY);
                    renameStage.show();
                    renameController = fxmlLoader.getController();

                    renameController.setController(this, serverView, Paths.get(trueFileName(serverView.getSelectionModel().getSelectedItem())));
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setNewFilename(Path oldPath, String newName, ListView lv) {
        if (clientView.equals(lv)) {
            File fileToRename = oldPath.toFile();
            fileToRename.renameTo(oldPath.getParent().resolve(newName).toFile());

            clientView.getItems().clear();
            clientView.getItems().addAll(getFilesList(homeDir));
        }

        if(serverView.equals(lv)) {
            String oldFilename = oldPath.toString();
            try {
                network.write(new RenameRequest(oldFilename, newName));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    }

    public void connectAction(ActionEvent actionEvent) {
        try {
            network = new Network(8189);
            Thread readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start();
//            connectBttn.setVisible(false);
            network.write(new AuthRequest(loginField.getText(),pwdField.getText()));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }
}