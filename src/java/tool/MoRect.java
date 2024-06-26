package tool;

import java.awt.*;

public class MoRect extends Rectangle {

    public void setSize(int size) {
        this.width = size;
        this.height = size;
    }

    public void scalePerc(double prcnt) {
        this.width += (int) (prcnt * this.width);
        this.height += (int) (prcnt * this.height);
    }

    public void setOrigin(Point origin) {
        this.x += origin.x;
        this.y += origin.y;
    }
}
