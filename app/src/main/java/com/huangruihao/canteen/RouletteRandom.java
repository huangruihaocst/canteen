package com.huangruihao.canteen;

import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

/**
 * class RouletteRandom
 * Created by alexwang(ice_b0und@hotmail.com) on Thursday, 2016-05-19 19:21.
 *
 * Generate a mRandom selector by an array of associated doubles and objects.
 *
 */
public class RouletteRandom<T> {
    /**
     * Generate a new mRandom selector by objects and probabilities.
     * Note: probabilities may not sum to 1.0, and the size of the two parameters must be the same
     * @param objects: the objects to choose
     * @param probabilities: the "probability" of objects
     * @throws RuntimeException
     */
    public RouletteRandom(T[] objects, double[] probabilities) throws RuntimeException {
        if (objects == null || probabilities == null)
            throw new RuntimeException("RouletteRandom constructor: parameters must not be null");
        if (objects.length != probabilities.length)
            throw new RuntimeException("RouletteRandom constructor: objects and probabilities must be the same size!");

        this.objects = objects;
        this.probabilities = new double[probabilities.length];

        double total = 0;
        for (double v : probabilities) {
            if (v < 0)
                throw new RuntimeException("RouletteRandom constructor: probability cannot be negative");
            total += v;
            // if we got an overflow, throw an exception
            if (Double.isNaN(total))
                throw new RuntimeException("RouletteRandom constructor: probabilities sum overflows");
        }
        for (int i = 0; i < probabilities.length; ++i) {
            this.probabilities[i] = probabilities[i] / total;
        }
    }

    public static RouletteRandom fromAssociatedArray(Object[] assArray) {
        if (assArray.length % 2 != 0)
            throw new RuntimeException("RouletteRandom::fromAssociatedArray(): assArray.length must be times of two!");
        Object[] objects = new Object[assArray.length / 2];
        double[] ps = new double[assArray.length / 2];
        for (int i = 0; i < assArray.length / 2; ++i) {
            objects[i] = assArray[2 * i];
            ps[i] = Double.parseDouble(assArray[2 * i + 1].toString());
        }
        return new RouletteRandom<>(objects, ps);
    }

    /**
     * Randomly choose an object by probabilities.
     * The probability of an object being chosen is in proportion its 'probabilities' array value.
     * @return the chosen object.
     */
    public T choose() {
        final Random r = new Random();
        // rand is between [0.0d, 1.0d]
        double rand = r.nextDouble();
        if (rand == 1.0d)
            rand = 0;

        double sum = 0;
        double last = 0;
        for (int i = 0; i < probabilities.length; ++i) {
            sum += probabilities[i];
            if (last <= rand && rand < sum)
                return objects[i];
            last = sum;
        }
        // float point error takes us here.
        return objects[objects.length - 1];
    }

    /**
     * Test function, randomly choose total times.
     * @param total: test times
     * @return result string
     */
    public static String testMySelf(int total) {
        String[] objects = { "10", "20", "30", "40" };
        double[] ps = { 1, 2, 3, 4 };
        RouletteRandom rr = new RouletteRandom<>(objects, ps);
        HashMap<String, Integer> hm = new HashMap<>();
        for (int i = 0; i < total; ++i) {
            String str = (String) rr.choose();
            if (hm.containsKey(str)) {
                hm.put(str, hm.get(str) + 1);
            }
            else {
                hm.put(str, 1);
            }
        }
        String result = "";
        for (String key : hm.keySet()) {
            result += key + ": " + String.valueOf(hm.get(key) / (double)total) + "\n";
        }
        return result;
    }

    private T[] objects;
    private double[] probabilities;
}
