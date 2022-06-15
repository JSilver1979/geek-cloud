package com.geekbrains.cloud.june.cloudapplication;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.nio.file.Path;

public class RenameController {

    @FXML
    public TextField renameField;
    public Label renameInfo;
    public Button cancelRenameBttn;

    private ChatController controller;
    private Path oldPath;
    private ListView lv;

    public void setController(ChatController controller, ListView lv, Path oldFilePath) {
        this.controller = controller;
        this.oldPath = oldFilePath;
        this.lv = lv;
        renameInfo.setText("Rename file: " + oldPath.getFileName().toString());
    }

    public void renameAction(ActionEvent actionEvent) {
        String newName = renameField.getText().trim();
        controller.setNewFilename(oldPath, newName, lv);

        Stage stage = (Stage) cancelRenameBttn.getScene().getWindow();
        stage.close();
    }

    public void closeAction(ActionEvent actionEvent) {
        Stage stage = (Stage) cancelRenameBttn.getScene().getWindow();
        stage.close();
    }
}
