package model;

import enums.Direction;

public class ScrollTrial extends Trial {
    public Direction direction;
    public int distance; // In lines
    public int tolerance; // In lines

    public ScrollTrial(Direction dir, int dist, int indSize) {
        super();

        direction = dir;
        distance = dist;
        tolerance = indSize;
    }
}
