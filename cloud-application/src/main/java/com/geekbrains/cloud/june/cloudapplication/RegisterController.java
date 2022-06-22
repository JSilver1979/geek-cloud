package com.geekbrains.cloud.june.cloudapplication;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {
    @FXML
    public TextField loginField;

    @FXML
    public PasswordField pwdField;

    private ChatController controller;

    public void setController(ChatController controller) {
        this.controller = controller;
    }

    public void registration(ActionEvent actionEvent) {
        controller.tryToReg(loginField.getText().trim(), pwdField.getText().trim());
    }
}
