package model;

import java.awt.*;

public class MoPoint extends Point {

    public MoPoint(Point p) {
        this.x = p.x;
        this.y = p.y;
    }

    public MoPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static MoPoint copy(Point p) {
        return new MoPoint(p);
    }

    public static MoPoint copyTranslated(Point p, int dX, int dY) {
        final MoPoint exP = new MoPoint(p);
        exP.translate(dX, dY);
        return exP;
    }

    public boolean isXInClosed(int lb, int ub) {
        return x >= lb && x <= ub;
    }

    public boolean isYInClosed(int lb, int ub) {
        return y >= lb && y <= ub;
    }

    public boolean isXYInClosed(int xlb, int xub, int ylb, int yub) {
        return isXInClosed(xlb, xub) && isYInClosed(ylb, yub);
    }
}
