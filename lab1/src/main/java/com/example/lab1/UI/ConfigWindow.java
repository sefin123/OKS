package com.example.lab1.UI;

import com.example.lab1.port.SerialPortManager;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.example.lab1.UI.PortWindow.createPortWindow;
import static com.example.lab1.UI.ErrorWindow.createErrorWindow;

public class ConfigWindow {

    public static void createConfigWindow(SerialPortManager portManager) {

        Stage stage = new Stage();
        stage.setTitle("Config Serial Communication App");
        try {
            List<String> devFiles = Files.list(Paths.get("/dev"))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(s -> s.matches("ttys00[0-9]"))
                    .sorted()
                    .toList();

            Label portTransmitLabel = new Label("Select the COM port for transmit data:");
            ComboBox<String> portTransmitComboBox = new ComboBox<>();
            portTransmitComboBox.getItems().addAll(devFiles);
            portTransmitComboBox.setValue(devFiles.get(1));

            Label portReceivingLabel = new Label("Select the COM port for receiving data:");
            ComboBox<String> portReceivingComboBox = new ComboBox<>();
            portReceivingComboBox.getItems().addAll(devFiles);
            portReceivingComboBox.setValue(devFiles.get(2));

            Label SpeedTxLabel = new Label("Speed Tx");
            ComboBox<Integer> speedTxComboBox = new ComboBox<>();
            speedTxComboBox.getItems().addAll(9600, 19200, 38400, 57600, 115200);
            speedTxComboBox.setValue(9600);

            Label SpeedRxLabel = new Label("Speed Rx");
            ComboBox<Integer> speedRxComboBox = new ComboBox<>();
            speedRxComboBox.getItems().addAll(9600, 19200, 38400, 57600, 115200);
            speedRxComboBox.setValue(9600);

            Button saveButton = new Button("Save");
            saveButton.setOnAction(actionEvent -> {
                if (speedTxComboBox.getValue() == null) {
                    createErrorWindow("speedTx is empty");
                }
                if (speedRxComboBox.getValue() == null) {
                    createErrorWindow("speedRx is empty");
                }
                portManager.initSerialPorts(portTransmitComboBox.getValue(),
                        portReceivingComboBox.getValue(),
                        speedTxComboBox.getValue(),
                        speedRxComboBox.getValue());
                createPortWindow(portManager, portManager.getDataReceivedTextArea());
                stage.close();
            });

            Button closeButton = new Button("Close");
            closeButton.setOnAction(actionEvent -> {
                stage.close();
                portManager.closePorts();
            });

            VBox root = new VBox();
            root.getChildren().addAll(
                    portTransmitLabel, portTransmitComboBox,
                    portReceivingLabel, portReceivingComboBox,
                    SpeedTxLabel, speedTxComboBox,
                    SpeedRxLabel, speedRxComboBox,
                    saveButton, closeButton
            );
            root.setSpacing(10);
            root.setPadding(new Insets(10));

            Scene scene = new Scene(root, 400, 350);

            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}