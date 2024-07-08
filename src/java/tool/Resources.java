package tool;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public class Resources {
    private static final TaggedLogger conLog = Logger.tag(Resources.class.getSimpleName());

    public static URI ZOOM_IN_URI = URI.create("");
    public static URI ZOOM_OUT_URI = URI.create("");

    public static URI PAN_LVL1_URI = URI.create("");
    public static URI PAN_LVL2_URI = URI.create("");
    public static URI PAN_LVL3_URI = URI.create("");

    public static URI SVG_PLAN_URI = URI.create("");
    public static URI THRESH_URI = URI.create("");
    public static URI THRESH_200_URI = URI.create("");

    public static URI WAV_HIT_URI = URI.create("");
    public static URI WAV_MISS_URI = URI.create("");
    public static URI WAV_ERROR_URI = URI.create("");
    public static URI WAV_END_URI = URI.create("");


    static {
        try {
            // Load the SVG resources
            SVG_PLAN_URI = Objects.requireNonNull(Resources.class.getResource("/plan4.svg")).toURI();

            THRESH_URI = Objects.requireNonNull(Resources.class.getResource("/threshold.svg")).toURI();
            THRESH_200_URI = Objects.requireNonNull(Resources.class.getResource("/threshold200.svg")).toURI();
            ZOOM_IN_URI = Objects.requireNonNull(Resources.class.getResource("/zoom-in.svg")).toURI();
            ZOOM_OUT_URI = Objects.requireNonNull(Resources.class.getResource("/zoom-out.svg")).toURI();

            PAN_LVL1_URI = Objects.requireNonNull(Resources.class.getResource("/curve1.svg")).toURI();
            PAN_LVL2_URI = Objects.requireNonNull(Resources.class.getResource("/curve2.svg")).toURI();
            PAN_LVL3_URI = Objects.requireNonNull(Resources.class.getResource("/curve3.svg")).toURI();

            WAV_HIT_URI = Objects.requireNonNull(Resources.class.getResource("/hit.wav")).toURI();
            WAV_MISS_URI = Objects.requireNonNull(Resources.class.getResource("/miss.wav")).toURI();
            WAV_ERROR_URI = Objects.requireNonNull(Resources.class.getResource("/error.wav")).toURI();
            WAV_END_URI = Objects.requireNonNull(Resources.class.getResource("/end.wav")).toURI();

        } catch(URISyntaxException ignored) {
            conLog.error("Could not load the SVGs");
        }
    }

//    public static class SVG {
//        
//
//        public static void refresh() {
//            try {
//                PLAN_URI = Objects.requireNonNull(SVG.class.getResource("/plan-v3.svg")).toURI();
//            } catch (URISyntaxException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }

    public static class TEXT {

        public static URI URI_LOREM = URI.create("");

        static {
            try {
                URI_LOREM = Objects.requireNonNull(Resources.class.getResource("/lorem.txt")).toURI();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
