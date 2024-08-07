package model;

import enums.Direction;

public class ScrollTrial extends Trial {
    public Direction direction;
    public int distance; // In lines
    public int indicSize; // In lines

    public ScrollTrial(Direction dir, int dist, int indSize) {
        super();

        direction = dir;
        distance = dist;
        indicSize = indSize;
    }
}
