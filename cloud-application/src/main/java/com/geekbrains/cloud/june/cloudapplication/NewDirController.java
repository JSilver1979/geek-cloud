package com.geekbrains.cloud.june.cloudapplication;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NewDirController {
    @FXML
    public TextField createField;
    @FXML
    public Button cancelBttn;

    private MainController controller;
    private ListView lv;

    public void setController(MainController controller, ListView lv) {
        this.controller = controller;
        this.lv = lv;
    }



    @FXML
    public void closeAction(ActionEvent actionEvent) {
        Stage stage = (Stage) cancelBttn.getScene().getWindow();
        stage.close();
    }
    @FXML
    public void createDirAction(ActionEvent actionEvent) {
        controller.setNewDir(createField.getText(),lv);
        Stage stage = (Stage) cancelBttn.getScene().getWindow();
        stage.close();
    }
}
