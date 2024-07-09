package tool;

import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class Constants {

    //-- Borders
    public static class BORDERS {
        public static final double THICKNESS_2_mm = 2;
        public static final double THICKNESS_1_mm = 1;
        public static final int THICKNESS_2 = DISP.mmToPxW(THICKNESS_2_mm);
        public static final int THICKNESS_1 = DISP.mmToPxW(THICKNESS_1_mm);

        public static final LineBorder BLACK_BORDER = new LineBorder(
                Color.BLACK, THICKNESS_1);
        public static final LineBorder FOCUSED_BORDER = new LineBorder(
                COLORS.GREEN, THICKNESS_2);
        public static final LineBorder FOCUS_LOST_BORDER = new LineBorder(
                COLORS.RED, THICKNESS_2);
    }

    //-- Colors
    public static class COLORS {
        public static final Color BLACK = Color.BLACK;
        public static final Color GREEN = Color.decode("#4CAF50");
        public final static Color DARK_GREEN = Color.decode("#1B5E20");
        public static final Color RED = Color.decode("#FF0000");
        public static final Color FLAX = Color.decode("#EDD98E");
        public static final Color YELLOW = Color.decode("#E3CE17");
        public final static Color BLUE = Color.decode("#64C0FF");
        public final static Color PLATINIUM = Color.decode("#E7E7E7");
        public final static Color LIGHT_GRAY = Color.decode("#DDDDDD");
        public final static Color VERY_LIGHT_GRAY = Color.decode("#FAFAFA");
        public final static Color GRAY_76 = Color.decode("#C2C2C2");

        public static String getHex(Color c) {
            return "#" + Integer.toHexString(c.getRGB()).substring(2);
        }
    }

    //-- Display properties
    public static class DISP {
//        public final static int PPI = 109; // Apple Display
        public final static int PPI = 90; // BenQ
//        public final static int PPI = 127; // MacBook Pro

        private final static double hPPIScale = 1.01; // Height DPI is slightly larger

        public final static double INCH_MM = 25.4;
        public final static double LR_MARGIN_mm = 20; // (mm) Left-right margin

        public static int mmToPxW(double mm) {
            return (int) ((mm / INCH_MM) * PPI);
        }

        public static int mmToPxH(double mm) {
            return (int) ((mm / INCH_MM) * PPI * hPPIScale);
        }
    }

    public static class FONTS {
        public static Font SF_LIGHT = new Font(Font.DIALOG,  Font.PLAIN, 5);

        // Italic attribute
        public static Map<? extends AttributedCharacterIterator.Attribute, ?>
                ATTRIB_ITALIC = Collections.singletonMap(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
    }


    //-- Strings
    public static class STR {
        public static final String MOOSE = "moose";
        public final static String CONNECTION = "connection";
        public final static String END = "end";

        public static final String MEMO_SP = ";";

        public static final String CLICK = "click";
        public static final String SCROLL = "scroll";
        public static final String DISPLACE = "displace";

        public static final String KEEP_ALIVE = "keep_alive";

        public final static String DEMO_TITLE = "Welcome to the scrolling experiment!";
        public final static String DEMO_NEXT = "First, let's have a demo >";

        public final static String SHORT_BREAK_TEXT =
                "<html>Time for a quick break! To continue, press <B>ENTER</B>.</html>";

        public static final String DLG_BREAK_TITLE  = "Time for a break!";
        public static final String DLG_BREAK_TEXT   =
                "<html>When ready, press <B>BLUE + RED</B> keys to start the next block</html>";

        public final static String EXP_START_MESSAGE =
                "To begin the experiment, press SPACE.";
        public final static String END_EXPERIMENT_MESSAGE =
                "All finished! Thank you for participating in this experiment!";

        public static final String PANZOOM = "panzoom";

        public final static String PID = "pid";
        public final static String TASK = "task";
        public final static String TECHNIQUE = "technique";
        public static final String FLING = "fling";

        public static final String ZOOM = "zoom";
        public static final String PAN = "pan";
        public static final String GRAB = "grab";
        public static final String REL = "rel";
        public static final String STOP = "stop";
        public static final String VEL = "vel";

        public static final String WHEEL_NOTCH = "wheel.notch";

        public static final String GAIN = "gain";
        public static final String FRICTION = "friction";

        public static final String VELOCITY = "velocity";
        public static final String MIN = "min";

        public static final String NUM = "num";
        public static final String BLOCKS = "blocks";

        public static final String DISTANCES = "distances";
        public static final String DIRECTIONS = "directions";
        public static final String INDICATOR = "indicator";
        public static final String TOLERANCES = "tolerances";

        public static final String WIDTH = "width";


//        public static String chain(String... args) {
//            StringBuilder sb = new StringBuilder();
//            for (int i = 0; i < args.length - 1; i++) {
//                sb.append(args[i]).append(".");
//            }
//            sb.append(args[args.length - 1]);
//
//            return sb.toString();
//        }

        /**
         * Null-safe String comparison
         */
        public static boolean equals(String s1, String s2) {
            return Objects.equals(s1, s2);
        }

    }
}
