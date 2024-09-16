package com.example.lab1;

import com.example.lab1.port.SerialPortManager;
import javafx.application.Application;
import javafx.stage.Stage;

import static com.example.lab1.UI.ConfigWindow.createConfigWindow;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        SerialPortManager portManager = new SerialPortManager();

        createConfigWindow(portManager);
    }

    public static void main(String[] args) {
        launch();
    }
}