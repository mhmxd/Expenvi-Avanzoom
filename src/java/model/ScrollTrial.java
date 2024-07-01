package model;

import enums.Direction;

public class ScrollTrial extends Trial {
    private Direction direction;
    private int distance; // In lines
    private int indicSize; // In lines

    public ScrollTrial(Direction dir, int dist, int indSize) {
        super();

        direction = dir;
        distance = dist;
        indicSize = indSize;
    }
}
