package com.magusgeek.brutaltester.util;

import java.util.Random;

public class SeedGenerator {

    private static Random random = new Random();
    private static int seed = 0;
    private static int usedCount = 0;
    public static boolean repeteableTests = false;
    public static void initialSeed(long newSeed){
        random = new Random(newSeed);
        repeteableTests = true;
    }
    public static synchronized int nextSeed() {
        return random.nextInt(Integer.MAX_VALUE);
    }
    public static synchronized int[] getSeed(int playerCount) {
        usedCount %= playerCount;
        if (usedCount == 0) {
            seed = random.nextInt(Integer.MAX_VALUE);
        }
        int[] result = new int[]{seed, usedCount};
        usedCount++;
        return result;
    }
}
