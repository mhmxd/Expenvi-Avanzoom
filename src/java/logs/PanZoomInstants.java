package logs;

import java.time.Instant;

public class PanZoomInstants extends TrialInstants{

    public Instant first_pan;
    public Instant last_pan;

    public Instant first_zoom;
    public Instant last_zoom;

    public PanZoomInstants() {
        first_pan = Instant.MIN;
        last_pan = Instant.MIN;
        first_zoom = Instant.MIN;
        last_zoom = Instant.MIN;
    }

    public void pan() {
        if (first_pan == Instant.MIN) first_pan = Instant.now();
        last_pan = Instant.now();
    }

    public void zoom() {
        if (first_zoom == Instant.MIN) first_zoom = Instant.now();
        last_zoom = Instant.now();
    }
}
