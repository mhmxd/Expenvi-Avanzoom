package tool;

import java.awt.*;

public class MoDimension extends Dimension {

    public MoDimension(int size) {
        this.width = size;
        this.height = size;
    }

    public MoDimension(int w, int h) {
        super(w, h);
    }

    /**
     * Constructor with scale
     * @param base Base dim
     * @param dW delta-W
     * @param dH delta-H
     */
    public MoDimension(Dimension base, int dW, int dH) {
        this.width = base.width + dW;
        this.height = base.height + dH;
    }

    /**
     * Constructor with scale
     * @param base Base dim
     * @param d Change in size
     */
    public MoDimension(Dimension base, int d) {
        this.width = base.width + d;
        this.height = base.height + d;
    }

    public MoDimension(Dimension base, double d) {
        this.width = (int) (base.width * d);
        this.height = (int) (base.height * d);
    }

    public MoDimension scale(double ratio) {
        width *= ratio;
        height *= ratio;

        return this;
    }

    public void scalePerc(double prcnt) {
        this.width += (int) (prcnt * this.width);
        this.height += (int) (prcnt * this.height);
    }
}
