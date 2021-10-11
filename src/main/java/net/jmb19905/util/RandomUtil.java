package net.jmb19905.util;

import java.util.Random;

public class RandomUtil {

    public int randomIntInRange(int min, int max){
        Random random = new Random();
        return randomIntInRange(random, min, max);
    }

    public int randomIntInRange(Random random, int min, int max){
        return random.nextInt(max - min) + min;
    }
}
