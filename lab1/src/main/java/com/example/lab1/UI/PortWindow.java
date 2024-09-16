package com.example.lab1.UI;

import com.example.lab1.port.SerialPortManager;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

import static com.example.lab1.UI.ConfigWindow.createConfigWindow;
import static com.example.lab1.UI.ErrorWindow.createErrorWindow;

public class PortWindow {

    public static void createPortWindow(SerialPortManager portManager, TextArea dataReceivedTextArea) {

        Stage stage = new Stage();
        stage.setTitle("Application for serial communication of port " +
                portManager.getPortTransfer().getSystemPortPath() +
                " to port " +
                portManager.getPortReceiving().getSystemPortPath());

        TextField dataToSendTextArea = new TextField();
        Label dataToSendTextAreaLabel = new Label("Select the COM port for data transfer:");

        Button closeButton = new Button("Close");

        Label statusInformationPortsLabel = new Label("Transfer port: " +
                portManager.getPortTransfer().getSystemPortPath() +
                " Receiving port: " +
                portManager.getPortReceiving().getSystemPortPath());

        Label statusInformationBytesLabel = new Label();

        dataToSendTextArea.setOnAction(actionEvent -> {
            String data = dataToSendTextArea.getText();
            if (Objects.equals(data, "")) {
                createErrorWindow("Fill in the field");
            }

            portManager.SendAction(data);

            statusInformationBytesLabel.setText(" Number of bytes: " +
                    dataToSendTextArea.getText().getBytes().length);
        });

        closeButton.setOnAction(actionEvent -> {
            stage.close();
            portManager.closePorts();
            createConfigWindow(portManager);
            dataReceivedTextArea.clear();
        });

        dataReceivedTextArea.setEditable(false);
        Label dataReceivedTextAreaLabel = new Label("Select the COM port for data transfer:");

        VBox root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(10));
        root.getChildren().addAll(dataToSendTextAreaLabel,
                dataToSendTextArea,
                closeButton,
                dataReceivedTextAreaLabel,
                dataReceivedTextArea,
                statusInformationPortsLabel,
                statusInformationBytesLabel);

        Scene scene = new Scene(root, 600, 400);

        stage.setScene(scene);
        stage.show();
    }
}
