package tool;

import model.MoPoint;

import java.awt.*;

public class MoRect extends Rectangle {

    public MoRect() {

    }

    public MoRect(int x, int y, int size) {
        super(x, y, size, size);
    }

    public MoRect(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    public MoRect(Point pos, Dimension dim) {
        this(pos.x, pos.y, dim.width, dim.height);
    }

    public void setSize(int size) {
        this.width = size;
        this.height = size;
    }

    public void scale(double prcnt) {
        this.width += (int) (prcnt * this.width);
        this.height += (int) (prcnt * this.height);
    }

    public void setOrigin(Point origin) {
        this.x += origin.x;
        this.y += origin.y;
    }

    public void setOriginWithRatio(Point origin, double ratio) {
        this.x += (int) (ratio * origin.x);
        this.y += (int) (ratio * origin.y);
    }
}
