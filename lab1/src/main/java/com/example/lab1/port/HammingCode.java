package com.example.lab1.port;

import java.util.ArrayList;
import java.util.List;

public class HammingCode {

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
        tmp[count] = !tmp[count];

        StringBuilder tmpString = new StringBuilder();
        for (int i = 0; i < dataLength; i++) {
            if (isPowerOfTwo(i + 1)) {
                tmpString.append(tmp[i] ? "1" : "0");
            }
        }

        return tmpString.toString();
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
}
