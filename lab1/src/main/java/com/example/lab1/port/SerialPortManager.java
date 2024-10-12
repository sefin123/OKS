package com.example.lab1.port;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

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

                        String extractedData = extractDataFromPackets(new String(newData));
                        String flipBitData = flipBitsWithProbability(extractedData, 0.6);
                        System.out.println(extractedData);
                        System.out.println(flipBitData);
                        int countDiffCharacters = countDifferentCharacters(extractedData, flipBitData);
                        if (countDiffCharacters == 2) {
                            infoHammingError = "2 different character";
                            infoHammingDecode = convertStringWithoutControlBits(extractedData);
                            indexHammingError = -1;
                            resultString.append(binaryStringToAscii(convertStringWithoutControlBits(extractedData)));
                        } else {
                            infoHammingError = flipBitData;
                            String decodeData = decodeHamming(flipBitData, extractedData);
                            infoHammingDecode = decodeData;
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

    public static String convertStringWithoutControlBits(String string) {
        StringBuilder tmpString = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            if (isPowerOfTwo(i + 1)) {
                tmpString.append(string.charAt(i));
            }
        }

        return tmpString.toString();
    }

    public static String decodeHamming(String hammingData, String string) {
        int dataLength = hammingData.length();
        int parityBitCount = calculateParityBitCount(dataLength) - 1;
        boolean[] booleanArray = new boolean[dataLength];

        for (int i = 0; i < dataLength; i++) {
            booleanArray[i] = hammingData.charAt(i) == '1';
        }

        boolean[] tmp = new boolean[dataLength];
        for (int i = 1; i <= dataLength; i++) {
            if (isPowerOfTwo(i)) {
                tmp[i - 1] = booleanArray[i - 1];
            }
        }

        for (int i = 0; i < parityBitCount; i++) {
            int parityBitPosition = (int) Math.pow(2, i);
            tmp[parityBitPosition - 1] = calculateParityBit(tmp, parityBitPosition);
        }

        StringBuilder decodedString = new StringBuilder();
        for (int i = 0; i < dataLength; i++) {
            decodedString.append(tmp[i] ? "1" : "0");
        }
        List<Integer> list;
        list = findNonMatchingIndexes(decodedString.toString(), string);

        int count = 0;
        for (Integer integer : list) {
            count += integer;
        }

        if (count == 0) {
            StringBuilder tmpString = new StringBuilder();
            for (int i = 0; i < dataLength; i++) {
                if (isPowerOfTwo(i + 1)) {
                    tmpString.append(tmp[i] ? "1" : "0");
                }
            }

            return tmpString.toString();
        }

        tmp[count] = !tmp[count];

        StringBuilder tmpString = new StringBuilder();
        for (int i = 0; i < dataLength; i++) {
            if (isPowerOfTwo(i + 1)) {
                tmpString.append(tmp[i] ? "1" : "0");
            }
        }

        return tmpString.toString();
    }

    public static List<Integer> findNonMatchingIndexes(String str1, String str2) {
        List<Integer> nonMatchingIndexes = new ArrayList<>();

        int minLength = Math.min(str1.length(), str2.length());

        for (int i = 0; i < minLength; i++) {
            if (str1.charAt(i) != str2.charAt(i)) {
                if (isPowerOfTwo(i + 1)) {
                    nonMatchingIndexes.add(i);
                }
            }
        }

        for (int i = minLength; i < str1.length(); i++) {
            if (isPowerOfTwo(i + 1)) {
                nonMatchingIndexes.add(i);
            }
        }

        for (int i = minLength; i < str2.length(); i++) {
            if (isPowerOfTwo(i + 1)) {
                nonMatchingIndexes.add(i);
            }
        }

        return nonMatchingIndexes;
    }

    public static int countDifferentCharacters(String str1, String str2) {
        int minLength = Math.min(str1.length(), str2.length());
        int count = 0;

        for (int i = 0; i < minLength; i++) {
            if (str1.charAt(i) != str2.charAt(i)) {
                count++;
            }
        }

        // Добавляем разницу в длине строк, если их длины различаются
        count += Math.abs(str1.length() - str2.length());

        return count;
    }

    public static String flipBitsWithProbability(String inputString, double probability) {
        StringBuilder flippedString = new StringBuilder();

        Random random = new Random();

        int index = 0;
        int indexOnceFlip = 0;
        int indexTwiceFlip = 0;
        for (char c : inputString.toCharArray()) {

            if (random.nextDouble() < probability) {

                int randomValue = random.nextInt(100); // Генерируем случайное число от 0 до 99

                if (randomValue < 60 && indexOnceFlip != 1 && indexTwiceFlip != 2) { // 60% вероятность изменения одного символа
                    indexOnceFlip++;
                    indexHammingError = index;
                    if (c == '0') {
                        flippedString.append('1');
                    } else if (c == '1') {
                        flippedString.append('0');
                    }
                } else if (randomValue < 85 && indexTwiceFlip != 2 && indexOnceFlip != 1) { // 25% вероятность изменения двух символов
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

    public static String binaryStringToAscii(String binaryString) {
        StringBuilder asciiString = new StringBuilder();

        for (int i = 0; i < binaryString.length(); i += 8) {
            String binaryChar = binaryString.substring(i, Math.min(i + 8, binaryString.length()));
            int charCode = Integer.parseInt(binaryChar, 2);
            char asciiChar = (char) charCode;
            asciiString.append(asciiChar);
        }

        return asciiString.toString();
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
            sb.append(String.format("%X", b));
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

    public void SendAction(String data) {
        String numOfPort = "0" + getPortReceiving().getSystemPortPath().replaceAll("\\D+", "");

        byte[] port = convertStringToByteArray(numOfPort);
        int numOfPackage = data.length() / (PACKAGE_DATA_SIZE + 1);

        StringBuilder dataToSend = new StringBuilder();
        for (int i = 0; i <= numOfPackage; i++) {

            dataToSend.setLength(0);
            StringBuilder packageData = new StringBuilder();
            for (int j = i * PACKAGE_DATA_SIZE; j < PACKAGE_DATA_SIZE * (i + 1); j++) {
                if (j < data.length()) {
                    packageData.append(data.charAt(j));
                }
            }
            infoDataTransfer = booleanArrayToString(bytesToBits(packageData.toString().getBytes()));
            String dataToSendString = booleanArrayToString(encodeHamming(bytesToBits(packageData.toString().getBytes())));
            infoHammingEncode = dataToSendString;

            dataToSend.append(FLAG).append(ByteArrayToString(DESTINATION_ADDRESS_BYTES)).append(ByteArrayToString(port)).
                    append(dataToSendString);

            byte[] a = dataToSend.toString().getBytes();

            portTransmit.writeBytes(a, a.length);
        }
    }

    public static boolean[] encodeHamming(boolean[] data) {
        int dataLength = data.length;
        int parityBitCount = calculateParityBitCount(dataLength);
        boolean[] hammingData = new boolean[dataLength + parityBitCount];

        int dataIndex = 0;

        for (int i = 1; i <= dataLength + parityBitCount; i++) {
            if (isPowerOfTwo(i)) {
                hammingData[i - 1] = data[dataIndex];
                dataIndex++;
            }
        }

        for (int i = 0; i < parityBitCount; i++) {
            int parityBitPosition = (int) Math.pow(2, i);
            hammingData[parityBitPosition - 1] = calculateParityBit(hammingData, parityBitPosition);
        }

        return hammingData;
    }

    private static int calculateParityBitCount(int dataLength) {
        int r = 0;
        while (Math.pow(2, r) < dataLength + r + 1) {
            r++;
        }
        return r;
    }

    public static boolean isPowerOfTwo(int number) {
        return (number & (number - 1)) != 0 || number == 0;
    }

    private static boolean calculateParityBit(boolean[] data, int position) {
        int bitIndex = position - 1;
        int count = 0;

        for (int i = bitIndex; i < data.length; i += position * 2) {
            for (int j = i; j < Math.min(i + position, data.length); j++) {
                if (data[j]) {
                    count++;
                }
            }
        }

        return count % 2 == 1;
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
}