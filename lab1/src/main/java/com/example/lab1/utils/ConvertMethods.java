package com.example.lab1.utils;

import static com.example.lab1.port.HammingCode.isPowerOfTwo;

public class ConvertMethods {

    public static String convertStringWithoutControlBits(String string) {
        StringBuilder tmpString = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            if (isPowerOfTwo(i + 1)) {
                tmpString.append(string.charAt(i));
            }
        }

        return tmpString.toString();
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

    public static String byteArrayToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%X", b));
        }
        return sb.toString();
    }

    public static byte[] StringToByteArray(String port) {
        byte[] portBytes = new byte[port.length()];
        for (int i = 0; i < port.length(); i++) {
            portBytes[i] = Byte.parseByte(String.valueOf(port.charAt(i)));
        }

        return portBytes;
    }

    public static String booleanArrayToString(boolean[] booleanArray) {
        StringBuilder sb = new StringBuilder();

        for (boolean b : booleanArray) {
            sb.append(b ? '1' : '0');
        }

        return sb.toString();
    }
}
