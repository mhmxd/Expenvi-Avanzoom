package logs;

import model.Trial;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;
import tool.Constants.STR;

import java.lang.reflect.Field;
import java.time.Instant;

public class TrialInstants {
    private final TaggedLogger conLog = Logger.tag(getClass().getSimpleName());

    public Instant trial_open = Instant.MIN;

    public Instant first_move = Instant.MIN;
    public Instant last_move = Instant.MIN;

    public Instant first_viewport_enter = Instant.MIN;
    public Instant last_viewport_enter = Instant.MIN;

    public Instant first_viewport_exit = Instant.MIN;
    public Instant last_viewport_exit = Instant.MIN;

    public Instant space_press = Instant.MIN;
    public Instant trial_close = Instant.MIN;

    //--- All the setters use Instant.now() for time
    public void trialOpen() {
        trial_open = Instant.now();
    }

    //-- Manage the first/last
    public void move() {
        if (first_move == Instant.MIN) first_move = Instant.now();
        last_move = Instant.now();
    }

    public void viewportEnter() {
        if (first_viewport_enter == Instant.MIN) first_viewport_enter = Instant.now();
        last_viewport_enter = Instant.now();
    }

    public void viewportExit() {
        if (first_viewport_exit == Instant.MIN) first_viewport_exit = Instant.now();
        last_viewport_exit = Instant.now();
    }

    public void spacePress() {
        space_press = Instant.now();
    }

    public void trialClose() {
        trial_close = Instant.now();
    }

    public String toLogValues() {
        StringBuilder result = new StringBuilder();

        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getType() == Instant.class) {
                try {
                    result.append(field.get(this)).append(STR.SP);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return result.deleteCharAt(result.length() - 1).toString();
    }


}