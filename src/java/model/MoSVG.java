package model;

import com.kitfox.svg.*;
import com.kitfox.svg.animation.AnimationElement;
import com.kitfox.svg.app.beans.SVGIcon;
import com.kitfox.svg.app.beans.SVGPanel;
import com.kitfox.svg.xml.StyleAttribute;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;
import tool.MoDimension;
import tool.MoRect;
import ui.ExpFrame;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static tool.Constants.*;

public class MoSVG extends SVGIcon {
    private final TaggedLogger conLog = Logger.tag(getClass().getSimpleName());

    private SVGDiagram svgDiagram;
//    private SVGUniverse svgUniverse;
    private URI svgURI;

    private MoRect destMinZoomSq = new MoRect();
    private double minZoomSqRatio;

    private MoRect destMaxZoomSq = new MoRect();
    private double maxZoomSqRatio;

    private Dimension initDim;

    public MoSVG() {
        // No need for super(), it's empty.
        setAntiAlias(true);
        setAutosize(SVGPanel.AUTOSIZE_STRETCH);
    }

    public void setup(URI svgURI) {
        this.svgURI = svgURI;
        setSvgURI(svgURI);
        svgDiagram = getSvgUniverse().getDiagram(svgURI);
//        int rotation = 0;
//        int startPosX = 0;
//        int startPosY = 0;

//        setSvgUniverse(SVGCache.getSVGUniverse());
//        setSvgURI(svgURI);

//        svgUniverse = SVGCache.getSVGUniverse();
//        try {
//            svgUniverse.loadSVG(svgURI.toURL());
//            svgDiagram = svgUniverse.getDiagram(svgURI);
//        } catch (MalformedURLException e) {
//            throw new RuntimeException(e);
//        }
//        svgDiagram = getSvgUniverse().getDiagram(svgURI);
//        final SVGUniverse uni = SVGCache.getSVGUniverse();
//        uni.clear();
//        try {
//            uni.loadSVG(svgURI.toURL());
//        } catch (MalformedURLException e) {
//            throw new RuntimeException(e);
//        }
//
//        svgDiagram = uni.getDiagram(svgURI);

//        SVGDiagram svgDiagram = SVGCache.getSVGUniverse().getDiagram(svgURI);
//        SVGRoot svgRoot = svgDiagram.getRoot();
//        StringBuilder builder = new StringBuilder();
//        builder.append("\"rotate(").append(rotation).append(" ")
//                .append(startPosX).append(" ")
//                .append(startPosY).append(" ").append(")\"");
//        try {
//            if (svgRoot.hasAttribute("transform", AnimationElement.AT_XML)) {
//                svgRoot.setAttribute("transform", AnimationElement.AT_XML, builder.toString());
//            } else {
//                svgRoot.addAttribute("transform", AnimationElement.AT_XML, builder.toString());
//            }
//
//            svgRoot.updateTime(0f);
//        } catch (SVGException ignored) {
//
//        }
    }

    public void setSize(Dimension dim) {
        setPreferredSize(dim);
        initDim = dim;
    }

    public void scale(double scale) {
        final int newWidth = (int) (getIconWidth() * (1 + scale));
        final int newHeight = (int) (getIconHeight() * (1 + scale));

        setPreferredSize(new Dimension(newWidth, newHeight));
    }

    public void setTrialParts(int roomNum, int minCircleRad) {
        // Positions are relative to the *plane*
        destMinZoomSq = getZoomArea(roomNum, "max");
        minZoomSqRatio = destMinZoomSq.width / (double) initDim.width;
//
        destMaxZoomSq = getZoomArea(roomNum, "min");
        maxZoomSqRatio = destMaxZoomSq.width / (double) initDim.width;
        conLog.trace("Max ratio = {}; Min Ratio: {}", maxZoomSqRatio, minZoomSqRatio);

        // Add circles
//        conLog.trace("Room {} Min Area = {}", trial.roomNum, roomMinArea);
        List<MoCircle> cList = generateCircles(
                destMaxZoomSq.width,
                destMaxZoomSq.x,
                destMaxZoomSq.y, minCircleRad);
        for (MoCircle c : cList) {
            addCircle(c.radius, new Point(c.cx, c.cy), ExpFrame.HIGHLIGHT_COLOR);
        }

        // Paint room walls :)
        paintRoom(roomNum, ExpFrame.HIGHLIGHT_COLOR);
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

    public void paint(Graphics g, Point pos) {
        paint(null, g, pos);
//        try {
//            svgDiagram.render((Graphics2D) g);
////            svgDiagram.updateTime(0f);
//        } catch (SVGException e) {
//            throw new RuntimeException(e);
//        }
    }

    public MoRect getDestMaxZoomSq() {
        destMaxZoomSq.setSize((int) (getIconWidth() * maxZoomSqRatio));
        final double scale = getIconWidth() / (double) initDim.width;
        destMaxZoomSq.x = (int) (scale * destMaxZoomSq.x); // Here from (0,0)
        destMaxZoomSq.y = (int) (scale * destMaxZoomSq.y); // Here from (0,0)
        return destMaxZoomSq;
    }

    public MoRect getDestMinZoomSq() {
        destMinZoomSq.setSize((int) (getIconWidth() * minZoomSqRatio));
        conLog.info("Min Zoom: {}", destMinZoomSq);
        final double scale = getIconWidth() / (double) initDim.width;
        destMinZoomSq.x = (int) (scale * destMinZoomSq.x); // Here from (0,0)
        destMinZoomSq.y = (int) (scale * destMinZoomSq.y); // Here from (0,0)

        return destMinZoomSq;
    }

    public void addText(String txt, Point pos, Color color) {
        final String xStr = String.valueOf(pos.x);
        final String yStr = String.valueOf(pos.y);
        final String colorHex = COLORS.getHex(color);

        Group group = svgDiagram.getRoot();
        Text text = new Text();
        text.appendText(txt);
        try {
            text.addAttribute("x", AnimationElement.AT_XML, xStr);
            text.addAttribute("y", AnimationElement.AT_XML, yStr);
            text.addAttribute("textLength", AnimationElement.AT_XML, String.valueOf(10));
            text.addAttribute("font-size", AnimationElement.AT_XML, String.valueOf(100));
            text.addAttribute("fill", AnimationElement.AT_XML, colorHex);

            group.loaderAddChild(null, text);

            //Update animation state for group and it's descendants so that it reflects new animation values.
            // We could also call diagram.update(0.0) or SVGCache.getSVGUniverse().update().  Note that calling
            // circle.update(0.0) won't display anything since even though it will update the circle's state,
            // it won't update the parent group's state.
            group.updateTime(0f);

        } catch (SVGException e) {
            e.printStackTrace();
        }
    }

    public void removeText() {
        Group group = svgDiagram.getRoot();
        System.out.println("Text: " + group.getChild(group.getNumChildren()-1).getTagName());
        try {
            if (group.getChild(group.getNumChildren()-1).getTagName() == "text") {
//                Text text = (Text) group.getChild(group.getNumChildren()-1);
                group.removeChild(group.getChild(group.getNumChildren()-1));
            }

            //Update animation state for group and it's descendants so that it reflects new animation values.
            // We could also call diagram.update(0.0) or SVGCache.getSVGUniverse().update().  Note that calling
            // circle.update(0.0) won't display anything since even though it will update the circle's state,
            // it won't update the parent group's state.
            group.updateTime(0.0);

        } catch (SVGException e) {
            e.printStackTrace();
        }
    }

    public void addCircle(int r, Point pos, Color color) {
        final String xStr = String.valueOf(pos.x);
        final String yStr = String.valueOf(pos.y);
        final String rStr = String.valueOf(r);
        final String colorHex = COLORS.getHex(color);
        conLog.trace("x, y = {}, {}", xStr, yStr);
        Group group = svgDiagram.getRoot();
        Circle circle = new Circle();
        try {
            circle.addAttribute("cx", AnimationElement.AT_XML, xStr);
            circle.addAttribute("cy", AnimationElement.AT_XML, yStr);
            circle.addAttribute("r", AnimationElement.AT_XML, rStr);
            circle.addAttribute("fill", AnimationElement.AT_XML, colorHex);

            group.loaderAddChild(null, circle);
            circle.updateTime(0f);
            //Update animation state for group and it's descendants so that it reflects new animation values.
            // We could also call diagram.update(0.0) or SVGCache.getSVGUniverse().update().  Note that calling
            // circle.update(0.0) won't display anything since even though it will update the circle's state,
            // it won't update the parent group's state.
            group.updateTime(0f);

            for (int i = 0; i < group.getNumChildren(); i++) {
                conLog.trace("Child = {}",
                        elementToString(group.getChild(i)));
            }


        } catch (SVGException e) {
            e.printStackTrace();
        }
    }

    public void paintRoom(int roomNum, Color color) {
        conLog.trace("Room Num = {}", roomNum);
        Group root = svgDiagram.getRoot();
        SVGElement element = root.getChild("r" + roomNum);
        conLog.trace ("n = {}, Element: {}", root.getNumChildren(), element);
        try {
            if (element != null) {
                element.setAttribute("fill", AnimationElement.AT_XML, COLORS.getHex(color));
                element.updateTime(0f);
            }
        } catch (SVGException e) {
            throw new RuntimeException(e);
        }

    }

    public void paintCirclesInRoom(int roomNum, Color color) {
        Group root = svgDiagram.getRoot();
        for (int i = 1; i <= 4; i++) {
            final String circleIndStr = "c" + roomNum + i;
            SVGElement element = root.getChild(circleIndStr);
            try {
                if (element != null) {
                    element.setAttribute(
                            "fill",
                            AnimationElement.AT_XML,
                            COLORS.getHex(color));
                    element.updateTime(0f);
                }
            } catch (SVGException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public MoRect getZoomArea(int roomNum, String minOrMax) {
        final String minStr = "r" + roomNum + minOrMax;
        conLog.trace("Str = {}", minStr);
        Group root = svgDiagram.getRoot();
        SVGElement element = root.getChild(minStr);
        conLog.trace("Element = {}", element);
        MoRect result = new MoRect();
        if (element != null) {
            StyleAttribute xAttrib = new StyleAttribute();
            StyleAttribute yAttrib = new StyleAttribute();
            StyleAttribute widthAttrib = new StyleAttribute();
            xAttrib.setName("x");
            yAttrib.setName("y");
            widthAttrib.setName("width");
            try {
                element.getStyle(xAttrib);
                result.x = xAttrib.getIntValue();
                element.getStyle(yAttrib);
                result.y = yAttrib.getIntValue();
                element.getStyle(widthAttrib);
                result.setSize(widthAttrib.getIntValue());
            } catch (SVGException e) {
                throw new RuntimeException(e);
            }
        }

        return result;
    }

    public String getArrtibValue(SVGElement element, String attribName) {
        StyleAttribute attrib = new StyleAttribute();
        attrib.setName(attribName);
        try {
            element.getStyle(attrib);
            return attrib.getStringValue();
        } catch (SVGException e) {
            throw new RuntimeException(e);
        }
    }

    public String elementToString(SVGElement element) {
        if (element == null) return "null";
        StringBuilder sb = new StringBuilder(element.getTagName()).append(": ");
        for (String s : element.getPresentationAttributes()) {
            sb.append(s).append(" = ").append(getArrtibValue(element, s)).append("; ");
        }

        return sb.toString();
    }

    public void whiten() {
        Group root = svgDiagram.getRoot();
        for (int i = 0; i < root.getNumChildren(); i++) {
            SVGElement element = root.getChild(i);
            conLog.trace("Element: {}", element);
            try {
                if (element != null && element.getId() != "border") {
                    if (element.hasAttribute("fill", 1)) {
                        element.setAttribute("fill", AnimationElement.AT_XML, "white");
                        element.updateTime(0f);
                    }
                }
            } catch (SVGException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void paint(Component comp, Graphics gg, Point pos) {
        paintIcon(comp, gg, pos.x, pos.y);
    }
}
