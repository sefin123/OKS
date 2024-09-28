package com.example.lab1.port;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

import static com.example.lab1.UI.ErrorWindow.createErrorWindow;

public class SerialPortManager {

    private SerialPort portTransmit;
    private SerialPort portReceiving;

    TextArea dataReceivedTextArea = new TextArea();

    public void initSerialPorts(String portTransmitName, String portReceivingName, Integer speedTx, Integer speedRx) {

        portTransmit = initializePort(portTransmitName, speedTx);
        portReceiving = initializePort(portReceivingName, speedRx);
        if (portTransmit == null || portReceiving == null) {
            closePorts();
        }
    }

    private SerialPort initializePort(String portName, Integer speed) {
        SerialPort port = SerialPort.getCommPort(portName);

        if (port.openPort()) {
            port.setComPortParameters(speed, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
            port.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() {
                    return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                }

                @Override
                public void serialEvent(SerialPortEvent event) {
                    if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                        return;
                    }

                    byte[] newData = new byte[port.bytesAvailable()];
                    int numRead = port.readBytes(newData, newData.length);
                    String receivedData = new String(newData, 0, numRead);
                    dataReceivedTextArea.clear();

                    Platform.runLater(() -> dataReceivedTextArea.appendText(receivedData));
                }
            });
            return port;

        } else {
            createErrorWindow(portName + " cant open");
            return null;
        }
    }

    public void SendAction(String data) {
        for (char c : data.toCharArray()) {
            String singleChar = String.valueOf(c);
            portTransmit.writeBytes(singleChar.getBytes(), 1);
        }
    }

    public void closePorts() {
        if (portTransmit.openPort()) {
            portTransmit.closePort();
        }
        if (portReceiving.openPort()) {
            portReceiving.closePort();
        }
    }

    public SerialPort getPortTransmit() {
        return portTransmit;
    }

    public SerialPort getPortReceiving() {
        return portReceiving;
    }

    public TextArea getDataReceivedTextArea() {
        return dataReceivedTextArea;
    }
}