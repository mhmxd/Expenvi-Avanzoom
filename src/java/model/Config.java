package model;

public class Config {
    // Fling
    public double flingVelocityGain;
    public double flingVelocityFriction;
    public double flingMinVelocity;
    public double flingGain;

    // Scroll
    public double scrollWheelGain;

    // Zoom
    public double zoomGain;
    public double zoomWheelNotchGain;

    // Pan
    public double panGain;
    public double panFriction;

    @Override
    public String toString() {
        return "flingVelocityGain=" + flingVelocityGain +
                " | flingVelocityFriction=" + flingVelocityFriction +
                " | flingMinVelocity=" + flingMinVelocity +
                " | flingGain=" + flingGain +
                " | zoomGain=" + zoomGain +
                " | zoomWheelNotchGain=" + zoomWheelNotchGain +
                " | panGain=" + panGain +
                " | panFriction=" + panFriction;
    }
}
