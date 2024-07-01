package enums;

import tool.Utils;

public enum Direction {
    N(1), S(1), E(3), W(4), NE(5), NW(6), SE(7), SW(8);

    private final int n;

    Direction(int i) { n = i; }

    // Get a random diagonal direction
    public static Direction getRandDiagonal() {
        return Direction.values()[Utils.randInt(4, 9)];
    }

    // Randomlhy choose one direction between the two
    public static Direction rand(Direction d0, Direction d1) {
        if (Utils.randInt(0, 2) == 0) return d0;
        else return d1;
    }

    // Get the opposite direction (Horizontal)
    public static Direction oppHz(Direction dr) {
        return switch (dr) {
            case N -> N;
            case S -> S;
            case E -> W;
            case W -> E;
            case NE -> NW;
            case NW -> NE;
            case SE -> SW;
            case SW -> SE;
        };
    }
    // Get the opposite direction (Vertical)
    public static Direction oppVt(Direction dr) {
        return switch (dr) {
            case N -> S;
            case S -> N;
            case E -> E;
            case W -> W;
            case NE -> SE;
            case NW -> SW;
            case SE -> NE;
            case SW -> NW;
        };
    }
}
