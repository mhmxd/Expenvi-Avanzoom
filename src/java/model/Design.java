package model;

import java.util.Arrays;

public class Design {
    // Scroll
    public int numScrollBlocks;
    public int[] scrollDistances;
    public int[] scrollTolerances;
    public double scrollIndicWidth; // mm

    // Pan
    public int numPanReps;

    // Zoom
    public int numZoomBlocks;
    public int numZoomReps;
    public int[] zoomDistances; // in *notches*
    public int zoomTolerance; // in *notches*

    // PanZoom
    public int numPanZoomBlocks;
    public int panZoomEndThreshold; // (mm) the amount to keep inside the view at the end of panning

    @Override
    public String toString() {
        return "Design{" +
                "numScrollBlocks=" + numScrollBlocks +
                ", scrollDistances=" + Arrays.toString(scrollDistances) +
                ", scrollTolerances=" + Arrays.toString(scrollTolerances) +
                ", scrollIndicWidth=" + scrollIndicWidth +
                ", numPanReps=" + numPanReps +
                ", numZoomBlocks=" + numZoomBlocks +
                ", numZoomReps=" + numZoomReps +
                ", zoomDistances=" + Arrays.toString(zoomDistances) +
                ", zoomTolerance=" + zoomTolerance +
                ", numPanZoomBlocks=" + numPanZoomBlocks +
                ", panZoomEndThreshold=" + panZoomEndThreshold +
                '}';
    }
}
