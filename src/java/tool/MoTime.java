package tool;

import java.time.Duration;
import java.time.Instant;

public class MoTime {

    /**
     * Millis until this moment
     * @param startInstant Instant
     * @return -1: startInstant EPOCH or null; -2: Duration.toMillis() exception (long overflow)
     *
     */
    public static long tillNowMillis(Instant startInstant) {
        if (startInstant == null) return -1;
        if (startInstant.compareTo(Instant.EPOCH) == 0) return -1; // Default for startInstant is EPOCH

        try {
            return Duration.between(startInstant, Instant.now()).toMillis();
        } catch (ArithmeticException ex) {
            return -2;
        }
    }

    /**
     * Get milliseconds of an Instant
     * @param instant Instant
     * @return Milliseconds. 0 if cannot be converted to ms.
     */
    public static long getMillis(Instant instant) {
        try {
            return instant.toEpochMilli();
        } catch (ArithmeticException ex) {
            return 0;
        }
    }
}
