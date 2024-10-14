package com.example.lab1.UI;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class ErrorWindow {

    public static void createErrorWindow(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR, errorMessage, ButtonType.OK);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}