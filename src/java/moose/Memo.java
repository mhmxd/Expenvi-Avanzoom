package moose;

import tool.Constants.*;

import java.util.Objects;

@SuppressWarnings("unused")
public class Memo {
    private String action;
    private String mode;
    private String value1;
    private String value2;
    private String debug;

    /**
     * Basic constructor
     */
    public Memo() {
        this.action = "";
        this.mode = "";
        this.value1 = "";
        this.value2 = "";
        this.debug = "";
    }

    /**
     * More general constructor
     * @param act Action
     * @param md Mode
     * @param values list of values (currently up to two is supported)
     */
    public Memo(String act, String md, Object... values) {
        action = act;
        mode = md;
        if (values.length == 1) value1 = String.valueOf(values[0]);
        if (values.length == 2) value2 = String.valueOf(values[1]);
    }

    /**
     * Return action
     *
     * @return String Action
     */
    public String getAction() {
        return action;
    }

    public boolean isAction(String checkStr) {
        return Objects.equals(action, checkStr);
    }

    /**
     * Return mode
     *
     * @return String Mode
     */
    public String getMode() {
        return mode;
    }

    public boolean isMode(String checkStr) {
        return Objects.equals(mode, checkStr);
    }

    /**
     * Get the first value
     *
     * @return String first value
     */
    public String getValue1() {
        return value1;
    }

    public float getV1Float() {
        return Float.parseFloat(value1);
    }

    public float getV2Float() {
        return Float.parseFloat(value2);
    }

    /**
     * Get the second value
     *
     * @return String second value
     */
    public String getValue2() {
        return value2;
    }

    /**
     * Convert and return the first value
     *
     * @return Int first value
     */
    public int getV1Int() {
        try {
            return (int) Double.parseDouble(value1);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Convert and return the second value
     *
     * @return Int second Value
     */
    public int getV2Int() {
        try {
            return (int) Double.parseDouble(value2);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Get debug
     *
     * @return String debug
     */
    public String getDebug() {
        return debug;
    }

    /**
     * Get the Memo from String
     *
     * @param msg String
     * @return Memo
     */
    public static Memo valueOf(String msg) {
        Memo result = new Memo();
        if (msg != null) {
            String[] parts = msg.split(STR.SP);

            if (parts.length >= 4 && parts.length <= 5) {
                result.action = parts[0];
                result.mode = parts[1];
                result.value1 = parts[2];
                result.value2 = parts[3];

                if (parts.length == 5) {
                    result.debug = parts[4];
                }
            }
        }

        return result;
    }

    /**
     * Get the String equivalent
     *
     * @return String
     */
    @Override
    public String toString() {
        return action + STR.SP + mode + STR.SP + value1 + STR.SP + value2;
    }
}