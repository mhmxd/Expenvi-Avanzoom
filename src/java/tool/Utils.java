package tool;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

import static tool.Constants.DISP;

public class Utils {
    private static final TaggedLogger conLog = Logger.tag(Utils.class.getSimpleName());

    private static final long MS_IN_DAY = 24 * 60 * 60 * 1000; // Milliseconds in a day

    /**
     * Returns a random int between the min (inclusive) and the bound (exclusive)
     * @param min Minimum (inclusive)
     * @param bound Bound (exclusive)
     * @return Random int
     */
    public static int randInt(int min, int bound) {
        if (bound <= min) return -1;
        else return new Random().nextInt(min, bound);
    }

    /**
     * Returns a random odd int between the min (inclusive) and the bound (exclusive)
     * @param min Minimum (inclusive)
     * @param bound Bound (exclusive)
     * @return Random int
     */
    public static int randOddInt(int min, int bound) {
        if (bound <= min) return min;
        else {
            Random random = new Random();
            int randomInt;
            do {
                randomInt = random.nextInt(bound - min) + min;
            } while (randomInt % 2 == 0); // Keep generating until we get an odd number

            return randomInt;
        }
    }

    /**
     * Returns a random int, multiple of mult,  between the min (inclusive) and the bound (exclusive)
     * @param min Minimum (inclusive)
     * @param bound Bound (exclusive)
     * @return Random int
     */
    public static int randMulInt(int min, int bound, int mult) {
        conLog.trace("min; bound = {}; {}", min, bound);
        if (bound <= min) return -1;
        return mult * randInt(min / mult, bound / mult);
    }

    public static int getLastIndBelow(List<Integer> list, int threshold) {
        if (list == null || list.isEmpty()) {
            return -1;
        }

        int lastBelowThreshold = -1;
        for (int num : list) {
            if (num < threshold) {
                lastBelowThreshold = num; // Update the lastBelowThreshold if the current num is below the threshold
            }
        }

        return lastBelowThreshold;
    }

    public static int getFirstAboveThreshold(List<Integer> list, int threshold) {
        if (list == null || list.isEmpty()) {
            return -1;
        }

        int lastBelowThreshold = -1;
        for (int num : list) {
            if (num > threshold) return num;
        }

        return -1;
    }

    public static boolean isBetween(double value, int min, int max, String excl) {
        switch (excl) {
            case "00" -> {
                return (value > min) && (value < max);
            }

            case "01" -> {
                return (value > min) && (value <= max);
            }

            case "10" -> {
                return (value >= min) && (value < max);
            }

            case "11" -> {
                return (value >= min) && (value <= max);
            }
        }

        return false;
    }

    public static double modifyInRange(double value, double change, int min, int max) {
        if (isBetween(value + change, min, max, "11")) return value + change;
        else if (value + change < min) return min;
        else if (value + change > max) return max;
        return value;
    }
    /**
     * Get the time in millis (in each day)
     * @return Timestamp (long)
     */
    public static long nowMillisInDay() {
        return System.currentTimeMillis() % MS_IN_DAY;
    }


    public static String nowDate() {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        return format.format(Calendar.getInstance().getTime());
    }


}
