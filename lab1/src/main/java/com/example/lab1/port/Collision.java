package com.example.lab1.port;

import java.util.Random;

public class Collision {

    private static final Random random = new Random();

    public static String createCollision(String data) {

        int position = random.nextInt(data.length() * 8);
        int charIndex = position / 8;
        int bitIndex = position % 8;

        char[] newData = data.toCharArray();

        int magicNumber = random.nextInt(10) + 1;
        if (magicNumber >= 3) {
            return new String(newData);
        }

        char targetChar = newData[charIndex];

        targetChar ^= (char) (1 << bitIndex);

        newData[charIndex] = targetChar;

        return new String(newData);
    }

    public static boolean isBusyChannel() {
        Random random = new Random();
        return random.nextBoolean();
    }

    public static void delayThread(int numOfAttempt) throws InterruptedException {
        Thread.sleep(2 * (long)numOfAttempt * 100);
    }

}
