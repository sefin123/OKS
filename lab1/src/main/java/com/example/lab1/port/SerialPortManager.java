package com.example.lab1.port;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

import static com.example.lab1.port.Collision.*;

import static com.example.lab1.port.HammingCode.*;

import static com.example.lab1.utils.ConvertMethods.*;

import java.util.*;

import static com.example.lab1.UI.ErrorWindow.createErrorWindow;

public class SerialPortManager {

    private SerialPort portTransmit;
    private SerialPort portReceiving;


    final Byte FLAG = 20;
    final byte[] DESTINATION_ADDRESS_BYTES = new byte[] {0, 0, 0, 0};
    final int PACKAGE_DATA_SIZE = 21;

    TextArea dataReceivedTextArea = new TextArea();

    private String infoDataTransfer = "";
    private String infoHammingEncode = "";
    private String infoHammingDecode = "";
    private String infoHammingError = "";
    private static int indexHammingError;

    private String transmitString = "";
    private String receivedString = "";
    private Integer numberOfCollision = 0;

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
                    StringBuilder resultString = new StringBuilder();

                    while (port.readBytes(newData, newData.length) > 0) {

                        String extractedData;
                        receivedString = new String(newData);

                        if (Objects.equals(transmitString, byteArrayToString(newData))) {
                            extractedData = extractDataFromPackets(new String(newData));
                        } else {
                            try {
                                delayThread(numberOfCollision);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            extractedData = extractDataFromPackets(transmitString);
                        }

                        String flipBitData = flipBitsWithProbability(extractedData);

                        int countDiffCharacters = countDifferentCharacters(extractedData, flipBitData);
                        if (countDiffCharacters == 2) {
                            infoHammingError = "2 different character";
                            infoHammingDecode = convertStringWithoutControlBits(extractedData);
                            indexHammingError = -1;
                            resultString.setLength(0);
                            resultString.append(binaryStringToAscii(convertStringWithoutControlBits(extractedData)));
                        } else {
                            infoHammingError = flipBitData;
                            String decodeData = decodeHamming(flipBitData, extractedData);
                            infoHammingDecode = decodeData;
                            resultString.setLength(0);
                            resultString.append(binaryStringToAscii(decodeData));

                        }

                        newData = new byte[port.bytesAvailable()];
                    }

                    dataReceivedTextArea.clear();

                    Platform.runLater(() -> dataReceivedTextArea.appendText(resultString.toString()));
                }
            });
            return port;

        } else {
            createErrorWindow(portName + " cant open");
            return null;
        }
    }

    public static int countDifferentCharacters(String str1, String str2) {
        int minLength = Math.min(str1.length(), str2.length());
        int count = 0;

        for (int i = 0; i < minLength; i++) {
            if (str1.charAt(i) != str2.charAt(i)) {
                count++;
            }
        }

        count += Math.abs(str1.length() - str2.length());

        return count;
    }

    public static String flipBitsWithProbability(String inputString) {
        StringBuilder flippedString = new StringBuilder();

        Random random = new Random();

        int index = 0;
        int indexOnceFlip = 0;
        int indexTwiceFlip = 0;
        for (char c : inputString.toCharArray()) {

            if (random.nextDouble() < 0.6) {

                int randomValue = random.nextInt(100);

                if (randomValue < 60 && indexOnceFlip != 1 && indexTwiceFlip != 2) {
                    indexOnceFlip++;
                    indexHammingError = index;
                    if (c == '0') {
                        flippedString.append('1');
                    } else if (c == '1') {
                        flippedString.append('0');
                    }
                } else if (randomValue < 85 && indexTwiceFlip != 2 && indexOnceFlip != 1) {
                    indexTwiceFlip++;
                    if (c == '0') {
                        flippedString.append('1');
                    } else if (c == '1') {
                        flippedString.append('0');
                    }
                } else {
                    flippedString.append(c);
                }
            } else {
                flippedString.append(c);
            }
            index++;
        }

        return flippedString.toString();
    }

    public static String extractDataFromPackets(String dataStream) {
        String marker = "200000000";
        StringBuilder extractedData = new StringBuilder();

        int markerIndex = dataStream.indexOf(marker);

        while (markerIndex != -1) {
            int skipIndex = markerIndex + marker.length() + 1;

            markerIndex = dataStream.indexOf(marker, skipIndex);
            if (markerIndex == -1) {
                markerIndex = dataStream.length();
            }

            extractedData.append(dataStream, skipIndex, markerIndex);

            if (markerIndex == dataStream.length())
                break;

        }

        return extractedData.toString();
    }

    public void SendAction(String data) throws InterruptedException {
        numberOfCollision = 0;

//        if (isBusyChannel()) {
//            Thread.sleep(1000);
//            System.out.println("Busy");
//        }

        String numOfPort = "0" + getPortReceiving().getSystemPortPath().replaceAll("\\D+", "");

        byte[] port = StringToByteArray(numOfPort);
        int numOfPackage = data.length() / (PACKAGE_DATA_SIZE + 1);

        StringBuilder dataToSend = new StringBuilder();
        for (int i = 0; i <= numOfPackage; i++) {

            dataToSend.setLength(0);
            StringBuilder packageData = new StringBuilder();
            for (int j = i * PACKAGE_DATA_SIZE; j < PACKAGE_DATA_SIZE * (i + 1); j++) {
                if (j < data.length() && data.charAt(j) != '\u0000') {
                    packageData.append(data.charAt(j));
                }
            }
            infoDataTransfer = booleanArrayToString(bytesToBits(packageData.toString().getBytes()));
            String dataToSendString = booleanArrayToString(encodeHamming(bytesToBits(packageData.toString().getBytes())));
            infoHammingEncode = dataToSendString;

            dataToSend.append(FLAG).append(byteArrayToString(DESTINATION_ADDRESS_BYTES)).append(byteArrayToString(port)).
                    append(dataToSendString);

            String collisionData = createCollision(dataToSend.toString());

            transmitString = dataToSend.toString();
            portTransmit.writeBytes(collisionData.getBytes(), collisionData.getBytes().length);

            if (countDifferentCharacters(receivedString, dataToSend.toString()) > 0) {
                i--;
                numberOfCollision++;
                System.out.println(numberOfCollision);
                if (numberOfCollision ==  16) {
                    createErrorWindow("Max number of collision!");
                    throw new RuntimeException("Max number of collision!");
                }
                delayThread(numberOfCollision);

            }
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

    public String getInfoHammingEncode() {
        return infoHammingEncode;
    }

    public String getInfoHammingDecode() {
        return infoHammingDecode;
    }

    public String getInfoHammingError() {
        return infoHammingError;
    }

    public int getIndexHammingError() {
        return indexHammingError;
    }

    public String getInfoDataTransfer() {
        return infoDataTransfer;
    }

    public Integer getNumberOfCollision() {
        return numberOfCollision;
    }
}