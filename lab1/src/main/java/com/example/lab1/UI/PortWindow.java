package com.example.lab1.UI;

import com.example.lab1.port.SerialPortManager;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.util.Objects;

import static com.example.lab1.UI.ConfigWindow.createConfigWindow;
import static com.example.lab1.UI.ErrorWindow.createErrorWindow;
import static com.example.lab1.port.SerialPortManager.isPowerOfTwo;

public class PortWindow {

    public static void createPortWindow(SerialPortManager portManager, TextArea dataReceivedTextArea) {

        Stage stage = new Stage();
        stage.setTitle("Application for serial communication of port " +
                portManager.getPortTransmit().getSystemPortPath() +
                " to port " +
                portManager.getPortReceiving().getSystemPortPath());

        TextField dataToSendTextArea = new TextField();
        Label dataToSendTextAreaLabel = new Label("Data to send:");

        Button closeButton = new Button("Close");

        Label statusInformationPortsLabel = new Label("Transmit port: " +
                portManager.getPortTransmit().getSystemPortPath() +
                " Receiving port: " +
                portManager.getPortReceiving().getSystemPortPath());

        TextFlow infoHammingEncodeTextFlow = new TextFlow();
        TextFlow infoHammingErrorTextFlow = new TextFlow();
        TextFlow infoHammingDecodeTextFlow = new TextFlow();
        TextFlow infoDataTransferTextFlow = new TextFlow();


        dataToSendTextArea.setOnAction(actionEvent -> {
            String data = dataToSendTextArea.getText();
            if (Objects.equals(data, "")) {
                createErrorWindow("Fill in the field");
            }

            portManager.SendAction(data);

            infoDataTransferTextFlow.getChildren().clear();
            infoHammingDecodeTextFlow.getChildren().clear();
            infoDataTransferTextFlow.getChildren().add(new Text(portManager.getInfoDataTransfer()));
            infoHammingDecodeTextFlow.getChildren().add(new Text(portManager.getInfoHammingDecode()));
            changeColorCodeHamming(infoHammingEncodeTextFlow, portManager.getInfoHammingEncode());
            changeColorErrorHamming(portManager, infoHammingErrorTextFlow, portManager.getInfoHammingError());

        });

        closeButton.setOnAction(actionEvent -> {
            stage.close();
            portManager.closePorts();
            createConfigWindow(portManager);
            dataReceivedTextArea.clear();
        });

        dataReceivedTextArea.setEditable(false);
        Label dataReceivedTextAreaLabel = new Label("Received text:");

        VBox root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(10));
        root.getChildren().addAll(dataToSendTextAreaLabel,
                dataToSendTextArea,
                closeButton,
                dataReceivedTextAreaLabel,
                dataReceivedTextArea,
                statusInformationPortsLabel,
                infoDataTransferTextFlow,
                infoHammingEncodeTextFlow,
                infoHammingErrorTextFlow,
                infoHammingDecodeTextFlow);

        Scene scene = new Scene(root, 700, 450);

        stage.setScene(scene);
        stage.show();
    }

    private static void changeColorCodeHamming(TextFlow textFlow, String string) {
        textFlow.getChildren().clear();
        for (int i = 0; i < string.length(); i++) {
            Text textNode = new Text(String.valueOf(string.charAt(i)));
            if (!isPowerOfTwo(i + 1)) {
                textNode.setFill(Color.GREEN);
            }
            textFlow.getChildren().add(textNode);
        }
    }

    private static void changeColorErrorHamming(SerialPortManager portManager, TextFlow textFlow, String string) {
        textFlow.getChildren().clear();
        for (int i = 0; i < string.length(); i++) {
            Text textNode = new Text(String.valueOf(string.charAt(i)));
            if (portManager.getIndexHammingError() == i && portManager.getIndexHammingError() != -1) {
                textNode.setFill(Color.RED);
            }
            textFlow.getChildren().add(textNode);
        }
    }
}