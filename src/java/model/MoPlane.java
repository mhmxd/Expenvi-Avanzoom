package model;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;
import tool.MoRect;

import java.awt.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MoPlane extends MoRect {
    private final TaggedLogger conLog = Logger.tag(getClass().getSimpleName());

//    private final MoSVG svg;
//    private URI uri;
//    private final MoDimension svgOriginalDim;

    private MoRect destMinZoomSq = new MoRect();
    private double minZoomSqRatio;

    private MoRect destMaxZoomSq = new MoRect();
    private double maxZoomSqRatio;

    private int destRoomNum;

    public MoPlane() {
//        uri = svgURI;
//        this.pos = pos;
//        this.dim = dim;

        // Set svg
//        svg = new MoSVG();
//        svg.setup(svgURI);
//        svgOriginalDim = new MoDimension(svg.getIconWidth(), svg.getIconHeight());
    }

    public void setBounds(Point pos, Dimension dim) {
        super.setBounds(pos.x, pos.y, dim.width, dim.height);
//        svg.setSize(dim);
    }

    public void setDestRoomNum(int destRoomNum) {
        this.destRoomNum = destRoomNum;
    }

    public void setTrialParts(int roomNum, int minCircleRad) {
        this.destRoomNum = roomNum;

        // Positions are relative to the *plane*
//        destMinZoomSq = svg.getZoomArea(roomNum, "max");
//        minZoomSqRatio = destMinZoomSq.width / (double) svgOriginalDim.width;
//
//        destMaxZoomSq = svg.getZoomArea(roomNum, "min");
//        maxZoomSqRatio = destMaxZoomSq.width / (double) svgOriginalDim.width;
        conLog.trace("Max ratio = {}; Min Ratio: {}", maxZoomSqRatio, minZoomSqRatio);

        // Add circles
//        conLog.trace("Room {} Min Area = {}", trial.roomNum, roomMinArea);
        List<MoCircle> cList = generateCircles(
                destMaxZoomSq.width,
                destMaxZoomSq.x,
                destMaxZoomSq.y, minCircleRad);
//        for (MoCircle c : cList) {
//            svg.addCircle(c.radius, new Point(c.cx, c.cy), ExpFrame.HIGHLIGHT_COLOR);
//        }

        // Paint room walls :)
//        svg.paintRoom(roomNum, ExpFrame.HIGHLIGHT_COLOR);
    }

    public static List<MoCircle> generateCircles(int W, int Xs, int Ys, int minR) {
        Random rand = new Random();
        List<MoCircle> circles = new ArrayList<>();
        int maxR = W / 6; // Since diameter = 2 * radius, maxR is W / 6

        for (int i = 0; i < 4; i++) {
            boolean validPosition = false;
            int cx = 0, cy = 0, r = 0;

            while (!validPosition) {
                r = rand.nextInt(maxR - minR + 1) + minR;

                switch (i) {
                    case 0: // Top side
                        cx = rand.nextInt(W - 2 * r) + Xs + r;
                        cy = Ys + r;
                        break;
                    case 1: // Bottom side
                        cx = rand.nextInt(W - 2 * r) + Xs + r;
                        cy = Ys + W - r;
                        break;
                    case 2: // Left side
                        cx = Xs + r;
                        cy = rand.nextInt(W - 2 * r) + Ys + r;
                        break;
                    case 3: // Right side
                        cx = Xs + W - r;
                        cy = rand.nextInt(W - 2 * r) + Ys + r;
                        break;
                }

                MoCircle newCircle = new MoCircle(cx, cy, r);
                validPosition = true;

                for (MoCircle circle : circles) {
                    if (isOverlapping(circle, newCircle)) {
                        validPosition = false;
                        break;
                    }
                }

                if (validPosition) {
                    circles.add(newCircle);
                }
            }
        }

        return circles;
    }

    private static boolean isOverlapping(MoCircle c1, MoCircle c2) {
        int distance = (int) Point.distance(c1.cx, c1.cy, c2.cx, c2.cy);
        return distance < (c1.radius + c2.radius);
    }

    public void scale(double scale) {
        final int newWidth = (int) (getWidth() * (1 + scale));
        final int newHeight = (int) (getHeight() * (1 + scale));

        setBounds(x, y, newWidth, newHeight);
//        svg.setSize(getSize());
    }

//    public void paint(Component comp, Graphics gg, Point pos) {
//        svg.paint(comp, gg, pos);
//    }

//    public MoRect getDestMaxZoomSq() {
//        destMaxZoomSq.setSize((int) (getWidth() * maxZoomSqRatio));
//        final double scale = getWidth() / (double) svgOriginalDim.width;
//        destMaxZoomSq.x = (int) (scale * destMaxZoomSq.x); // Here from (0,0)
//        destMaxZoomSq.y = (int) (scale * destMaxZoomSq.y); // Here from (0,0)
//        return destMaxZoomSq;
//    }
//
//    public MoRect getDestMinZoomSq() {
//        destMinZoomSq.setSize((int) (getWidth() * minZoomSqRatio));
//        conLog.info("Min Zoom: {}", destMinZoomSq);
//        final double scale = getWidth() / (double) svgOriginalDim.width;
//        destMinZoomSq.x = (int) (scale * destMinZoomSq.x); // Here from (0,0)
//        destMinZoomSq.y = (int) (scale * destMinZoomSq.y); // Here from (0,0)
//
//        return destMinZoomSq;
//    }

}
