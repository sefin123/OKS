package com.example.lab1.port;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.util.ArrayList;
import java.util.Arrays;

import static com.example.lab1.UI.ErrorWindow.createErrorWindow;

public class SerialPortManager {

    private SerialPort portTransmit;
    private SerialPort portReceiving;


    final Byte FLAG = 20;
    final byte[] DESTINATION_ADDRESS_BYTES = new byte[] {0, 0, 0, 0};
    final int PACKAGE_DATA_SIZE = 21;
    final Byte FCS = 0;

    TextArea dataReceivedTextArea = new TextArea();

    private ArrayList<String> bitStuffing = new ArrayList<>();

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
                    ArrayList<String> dynamicStringArray = new ArrayList<>();
                    ArrayList<String> extractedArray = new ArrayList<>();
                    int index = 0;
                    while (port.readBytes(newData, newData.length) > 0) {
                        System.out.println("data length: " + newData.length);
                        System.out.println("data: " + Arrays.toString(newData));
                        String receivedData = new String(newData);
                        System.out.println("received: " + receivedData);
                        System.out.println("rl: " + receivedData.length());
                        String extracted = extractDataFromPackets(receivedData);
                        System.out.println("extract: " + extracted);
                        extractedArray.add(extracted);
                        String debitStaffing = debitStaffing(extracted);
                        System.out.println("debitStaffing: " + debitStaffing);
                        dynamicStringArray.add(binaryStringToAscii(debitStaffing));
                        for (String c : dynamicStringArray) {
                            System.out.println(c);
                        }
                        System.out.println("----------------");
                        index++;
                        newData = new byte[port.bytesAvailable()];
                    }
                    bitStuffing = extractedArray;
                    dataReceivedTextArea.clear();
                    Platform.runLater(() -> {
                        StringBuilder sb = new StringBuilder();
                        for (String str : dynamicStringArray) {
                            sb.append(str); // Добавляем каждую строку с новой строки
                        }
                        dataReceivedTextArea.appendText(sb.toString());
                    });
                }
            });
            return port;

        } else {
            createErrorWindow(portName + " cant open");
            return null;
        }
    }

    public static String binaryStringToAscii(String binaryString) {

        if (!binaryString.matches("[01]+")) {
            return "Некорректная строка бинарных данных";
        }
        StringBuilder asciiBuilder = new StringBuilder();

        // Дополнить строку нулями до кратного 8, если необходимо
        int remainder = binaryString.length() % 8;
        if (remainder != 0) {
            binaryString = "0".repeat(8 - remainder) + binaryString;
        }

        for (int i = 0; i < binaryString.length(); i += 8) {
            String binaryChar = binaryString.substring(i, i + 8);
            int asciiValue = Integer.parseInt(binaryChar, 2);
            char asciiChar = (char) asciiValue;
            asciiBuilder.append(asciiChar);
        }

        return asciiBuilder.toString();
    }

    public static String extractDataFromPackets(String dataStream) {
        String marker = "200000000";
        StringBuilder extractedData = new StringBuilder();

        int markerIndex = dataStream.indexOf(marker);

        while (markerIndex != -1) {
            System.out.println("mI: " + markerIndex);
            // Пропускаем 8 символов после маркера "20"
            int skipIndex = markerIndex + marker.length() + 1;
            System.out.println("sI: " + skipIndex);

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

    public static boolean[] bytesToBits(byte[] bytes) {
        boolean[] bits = new boolean[bytes.length * 8];
        int bitIndex = 0;

        for (byte b : bytes) {
            for (int i = 7; i >= 0; i--) {
                bits[bitIndex++] = (b & (1 << i)) != 0;
            }
        }

        return bits;
    }

    public static String ByteArrayToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%X", b)); // Преобразование каждого байта в двухзначное шестнадцатеричное число
        }
        return sb.toString();
    }

    public byte[] convertStringToByteArray(String port) {
        byte[] portBytes = new byte[port.length()];
        for (int i = 0; i < port.length(); i++) {
            portBytes[i] = Byte.parseByte(String.valueOf(port.charAt(i)));
        }

        return portBytes;
    }

    public String booleanArrayToString(boolean[] booleanArray) {
        StringBuilder sb = new StringBuilder();

        for (boolean b : booleanArray) {
            sb.append(b ? '1' : '0');
        }

        return sb.toString();
    }

    public void SendAction(String data) {//(1)
        String numOfPort = "0" + getPortReceiving().getSystemPortPath().replaceAll("\\D+", "");

        byte[] port = convertStringToByteArray(numOfPort);
        int numOfPackage = data.length() / (PACKAGE_DATA_SIZE + 1);

        byte q = 0;
        for (int i = 7; i >= 0; i--) {
            int bit = (FLAG >> i) & 1;
            q = (byte) (q | (bit << i));
        }
        StringBuilder dataToSend = new StringBuilder();
        for (int i = 0; i <= numOfPackage; i++) {
            dataToSend.setLength(0);
            StringBuilder packageData = new StringBuilder();
            for (int j = i * PACKAGE_DATA_SIZE; j < PACKAGE_DATA_SIZE * (i + 1); j++) {
                if (j < data.length()) {
                    packageData.append(data.charAt(j));
                }
            }

            boolean[] dataToSendBits = bytesToBits(packageData.toString().getBytes()); //(2)(3)

            String dataToSendString = booleanArrayToString(dataToSendBits);//(4)

            dataToSend.append(FLAG).append(ByteArrayToString(DESTINATION_ADDRESS_BYTES)).append(ByteArrayToString(port)).
                    append(bitStaffing(dataToSendString));//(5)
            System.out.println(i + "A: " + dataToSend);
            System.out.println("asdasd" + numOfPackage);

            portTransmit.writeBytes(dataToSend.toString().getBytes(), dataToSend.length());
        }
    }

    public static String bitStaffing(String dataToSend) {
        int count = 0;
        StringBuilder result = new StringBuilder();

        for (int k = 0; k < dataToSend.length(); k++) {
            char currentChar = dataToSend.charAt(k);
            if (currentChar == '1') {
                count++;
                result.append(currentChar);
                if (count == 5) {
                    result.append('a');
                    count = 0;
                }
            } else {
                count = 0;
                result.append(currentChar);
            }
        }

        return result.toString();
    }

    public static String debitStaffing(String stuffedData) {
        StringBuilder result = new StringBuilder();
        int countOnes = 0;

        boolean flag = false;
        for (char c : stuffedData.toCharArray()) {
            if (flag) {
                flag = false;
                continue;
            }
            if (c == '1') {
                countOnes++;
                if (countOnes == 5) {
                    countOnes = 0;
                    flag = true;
                }
            } else {
                countOnes = 0;
            }

            result.append(c);
        }

        return result.toString();
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

    public ArrayList<String> getBitStuffing() {
        return bitStuffing;
    }
}