package tool;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public class Resources {

    public static class SVG {
        private static final TaggedLogger conLog = Logger.tag(SVG.class.getSimpleName());

        public static URI ZOOM_IN_URI = URI.create("");
        public static URI ZOOM_OUT_URI = URI.create("");

        public static URI PAN_LVL1_URI = URI.create("");
        public static URI PAN_LVL2_URI = URI.create("");
        public static URI PAN_LVL3_URI = URI.create("");

        public static URI PLAN_URI = URI.create("");
        public static URI THRESH_URI = URI.create("");
        public static URI THRESH_200_URI = URI.create("");

        static {
            try {
                // Load the SVG resources
                PLAN_URI = Objects.requireNonNull(SVG.class.getResource("/plan-v4.svg")).toURI();

                THRESH_URI = Objects.requireNonNull(SVG.class.getResource("/threshold.svg")).toURI();
                THRESH_200_URI = Objects.requireNonNull(SVG.class.getResource("/threshold200.svg")).toURI();
                ZOOM_IN_URI = Objects.requireNonNull(SVG.class.getResource("/zoom-in.svg")).toURI();
                ZOOM_OUT_URI = Objects.requireNonNull(SVG.class.getResource("/zoom-out.svg")).toURI();

                PAN_LVL1_URI = Objects.requireNonNull(SVG.class.getResource("/curve1.svg")).toURI();
                PAN_LVL2_URI = Objects.requireNonNull(SVG.class.getResource("/curve2.svg")).toURI();
                PAN_LVL3_URI = Objects.requireNonNull(SVG.class.getResource("/curve3.svg")).toURI();

            } catch(URISyntaxException ignored) {
                conLog.error("Could not load the SVGs");
            }
        }

        public static void refresh() {
            try {
                PLAN_URI = Objects.requireNonNull(SVG.class.getResource("/plan-v3.svg")).toURI();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }

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
