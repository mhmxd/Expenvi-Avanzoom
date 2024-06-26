package ui;

import com.kitfox.svg.*;
import com.kitfox.svg.animation.AnimationElement;
import com.kitfox.svg.app.beans.SVGIcon;
import com.kitfox.svg.app.beans.SVGPanel;
import control.Server;
import enums.Task;
import listener.MooseListener;
import moose.Memo;
import moose.Moose;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;
import tool.MoDimension;
import tool.Resources;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static tool.Constants.*;

public class ZoomView extends JPanel
        implements MouseListener, MouseMotionListener, MouseWheelListener, MooseListener, PropertyChangeListener {
    private final TaggedLogger conLog = Logger.tag(getClass().getSimpleName());

    private String VK_SPACE = String.valueOf(KeyEvent.VK_SPACE);

    private Task task;

    // View
    private final SVGIcon zoomSVGIcon;
//    private final SVGIcon zoomOutSVGIcon;
    private final MoDimension zoomDim = new MoDimension(5000);
    private Point zoomPos; // Relative to this viewport! (set at paintComponent)

    private SVGIcon thresholdSVGIcon;
    private MoDimension thresholdDim = new MoDimension(200);
    private Point thresholdPos; // Being uninitialized is important!
    //    private FocusFrame thresholdSq;
    private BufferedImage image;

    // Consts
    private final int focusFrameSize = 50;
    private final int SYMBOL_SIZE = 50;

    // Panning
    private boolean isCursorInside = false;
    private boolean isGrabbed = false;
    private Point dragPoint;
    private final double PAN_FRICTION = 0.2;
    private Point padPos = new Point(0,0); // Changed only with padding
    //    private Integer xDiff;
//    private Integer yDiff;
    private ScheduledExecutorService panner = Executors.newSingleThreadScheduledExecutor();

    //– Zooming
    private final double GAIN = 0.05;
    private int detent;

    //– Moose receiving
    private Moose moose;

    public Action colorAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            conLog.info("Space pressed");
            // Color the hint sq
            colorThreshold("#FA4A4A");
        }
    };

    //------------------------------------------------------------------
    private class PanTask implements Runnable {
        int velX, velY;
        int dX, dY;

        public PanTask(double vX, double vY) {
            velX = (int) vX; // px/s -> px/(10)ms (10ms is the freq. of running the Task)
            velY = (int) vY; // px/s -> px/(10)ms
        }

        @Override
        public void run() {

            while (Math.abs(velX) > 0 || Math.abs(velY) > 0) {
                conLog.info("Run: vX, vY = {}, {}", velX, velY);
                pan(velX, velY);
                velX *= (1 - PAN_FRICTION);
                velY *= (1 - PAN_FRICTION);

                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    //------------------------------------------------------------------

    public ZoomView(Task task) {
        this.task = task;

        zoomSVGIcon = new SVGIcon();
        zoomSVGIcon.setAntiAlias(true);
        zoomSVGIcon.setAutosize(SVGPanel.AUTOSIZE_STRETCH);

//        zoomOutSVGIcon = new SVGIcon();
//        zoomOutSVGIcon.setAntiAlias(true);
//        zoomOutSVGIcon.setAutosize(SVGPanel.AUTOSIZE_STRETCH);

        thresholdSVGIcon = new SVGIcon();
        thresholdSVGIcon.setAntiAlias(true);
        thresholdSVGIcon.setAutosize(SVGPanel.AUTOSIZE_STRETCH);

        // Add the focus frame
        conLog.info("Adding FocusFrame");
//        focusFrame = new FocusFrame();
//        focusFrame.setFocusable(false);
//        add(focusFrame);

        // Init
        moose = new Moose();

        //– Listeners
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
//        moose.addMooseListener(this);
        Server.get().addPropertyChangeListener(this);

    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);

        URI zURI = null;
        if (task == Task.ZOOM_IN) {
            zURI = Resources.SVG.ZOOM_IN_URI;
        } else if (task == Task.ZOOM_OUT) {
            zURI = Resources.SVG.ZOOM_OUT_URI;
        }

        int rotation = 0;
        int startPosX = 0;
        int startPosY = 0;

        zoomSVGIcon.setSvgURI(zURI);
        SVGDiagram zoomSvgDiagram = SVGCache.getSVGUniverse().getDiagram(Resources.SVG.ZOOM_IN_URI);
        SVGRoot zoomSvgRoot = zoomSvgDiagram.getRoot();

        StringBuilder builder = new StringBuilder();
        builder.append("\"rotate(").append(rotation).append(" ")
                .append(startPosX).append(" ")
                .append(startPosY).append(" ").append(")\"");
        try {
            if (zoomSvgRoot.hasAttribute("transform", AnimationElement.AT_XML)) {
                zoomSvgRoot.setAttribute("transform", AnimationElement.AT_XML, builder.toString());
            } else {
                zoomSvgRoot.addAttribute("transform", AnimationElement.AT_XML, builder.toString());
            }

            zoomSvgRoot.updateTime(0f);
        } catch (SVGException ignored) {

        }
//
//        zoomOutSVGIcon.setSvgURI(Resources.SVG.ZOOM_OUT_URI);
//
//        SVGDiagram zoomOutSvgDiagram = SVGCache.getSVGUniverse().getDiagram(Resources.SVG.ZOOM_OUT_URI);
//        SVGRoot zoomOutSvgRoot = zoomOutSvgDiagram.getRoot();
//
//        builder = new StringBuilder();
//        builder.append("\"rotate(").append(rotation).append(" ")
//                .append(startPosX).append(" ")
//                .append(startPosY).append(" ").append(")\"");
//        try {
//            if (zoomOutSvgRoot.hasAttribute("transform", AnimationElement.AT_XML)) {
//                zoomOutSvgRoot.setAttribute("transform", AnimationElement.AT_XML, builder.toString());
//            } else {
//                zoomOutSvgRoot.addAttribute("transform", AnimationElement.AT_XML, builder.toString());
//            }
//
//            zoomOutSvgRoot.updateTime(0f);
//        } catch (SVGException ignored) {
//
//        }


        thresholdSVGIcon.setSvgURI(Resources.SVG.THRESH_200_URI);

        SVGDiagram thresholdSvgDiagram = SVGCache.getSVGUniverse().getDiagram(Resources.SVG.THRESH_200_URI);
        SVGRoot thresholdSvgRoot = thresholdSvgDiagram.getRoot();

        StringBuilder thresholdBuilder = new StringBuilder();
        thresholdBuilder.append("\"rotate(").append(rotation).append(" ")
                .append(startPosX).append(" ")
                .append(startPosY).append(" ").append(")\"");
        try {
            if (thresholdSvgRoot.hasAttribute("transform", AnimationElement.AT_XML)) {
                thresholdSvgRoot.setAttribute("transform", AnimationElement.AT_XML,
                        thresholdBuilder.toString());
            } else {
                thresholdSvgRoot.addAttribute("transform", AnimationElement.AT_XML,
                        thresholdBuilder.toString());
            }

            thresholdSvgRoot.updateTime(0f);
        } catch (SVGException ignored) {

        }

//        thresholdDim = new MoDimension(200);
        thresholdPos = new Point();
        thresholdPos.x = (this.getWidth() - thresholdDim.width) / 2;
        thresholdPos.y = (this.getHeight() - thresholdDim.height) / 2;

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (zoomPos == null) {
            zoomPos = new Point(-(zoomDim.width - getWidth())/2, -(zoomDim.height - getHeight())/2);

            //– Draw the threshold squares
//            focusFrame.setBounds(
//                    getWidth() / 2 - focusFrameSize / 2,
//                    getHeight() / 2 - focusFrameSize / 2,
//                    focusFrameSize,
//                    focusFrameSize);
//            focusFrame.setVisible(true);

            // Set the image to the whole panel
//            image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        }

        //– Draw the whole field
        zoomSVGIcon.setPreferredSize(zoomDim);
        zoomSVGIcon.paintIcon(this, g, zoomPos.x, zoomPos.y);

        //– Display the symbol at a certain zoom level
//        if (detent > 5) {
//            g.setColor(COLORS.RED);
//            g.fillRect(
//                    (getWidth() - SYMBOL_SIZE)/2,
//                    (getHeight() - SYMBOL_SIZE)/2,
//                    SYMBOL_SIZE, SYMBOL_SIZE
//            );
//        }

//        if (xDiff == null || yDiff == null) {
//            int offsetX;
//            int offsetY;
//            int offset = focusFrameSize / 2 + Constants.BORDERS.THICKNESS_2;
//
//            xDiff = -(5000 - getWidth())/2;
//            yDiff = -(5000 - getHeight())/2;
//
//            //– Draw the focus frame (rectangle in the middle)
//            focusFrame.setBounds(
//                    getWidth() / 2 - focusFrameSize / 2,
//                    getHeight() / 2 - focusFrameSize / 2,
//                    focusFrameSize,
//                    focusFrameSize);
//            focusFrame.setVisible(true);
//
//            image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
//        }

        //– Draw threshold
        thresholdSVGIcon.setPreferredSize(thresholdDim);
        thresholdSVGIcon.paintIcon(this, g, thresholdPos.x, thresholdPos.y);
    }

    private void colorThreshold(String color) {
        SVGDiagram thresholdSvgDiagram = SVGCache.getSVGUniverse().getDiagram(Resources.SVG.THRESH_URI);
        SVGRoot thresholdSvgRoot = thresholdSvgDiagram.getRoot();

        List<SVGElement> children = new ArrayList<>();
        thresholdSvgRoot.getChildren(children);

        try {
            children.get(0).setAttribute("stroke", 1, color);
            children.get(1).setAttribute("stroke", 1, color);
        } catch (SVGElementException e) {
            conLog.warn("Element not found!");
        }

        repaint();
    }

    private Point getCurPoint() {
        final Point p = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(p, this);

        return p;
    }

    /**
     * Position: -45 <= x, y <= -20
     * @param curPoint
     */
//    public void pan(Point curPoint) {
//        if (dragPoint == null) grab(curPoint);
//
//        int xDiff = curPoint.x - dragPoint.x;
//        int yDiff = curPoint.y - dragPoint.y;
//
//        dragPoint = curPoint;
//
//        zoomPos.translate(xDiff, yDiff);
//        padPos.translate(xDiff, yDiff);
//        conLog.info("Pos = {}", padPos);
////        xDiff += dX;
////        yDiff += dY;
//
////        if (image == null) return;
//
//        repaint();
//    }

    /**
     * Pan with the velocity
     * @param vX X veclocity
     * @param vY Y velocity
     */
    public void pan(double vX, double vY) {
        conLog.info("vX, vY = {}, {}", vX, vY);
//        double xVel = vX * 10;
//        double yVel = vY * 10;
//        final float VELOCITY_THRESHOLD_MULTIPLIER = 1000f / 16f;
//        final float THRESHOLD_MULTIPLIER = 0.75f;
//        float velocityThreshold = THRESHOLD_MULTIPLIER * VELOCITY_THRESHOLD_MULTIPLIER;
//
//        double time = Math.log(velocityThreshold / vX) * 1000d / PAN_FRICTION;
//        double flingDistance = xVel / PAN_FRICTION * (Math.exp(PAN_FRICTION * time / 1000d) - 1);

//        float t = 0f;
//        double multiplier;

        PanTask panTask = new PanTask(vX, vY);
        panner.scheduleAtFixedRate(panTask, 0, 10, TimeUnit.MILLISECONDS);


    }

    /**
     * Pan with displacement
     * @param dX – X distance
     * @param dY – Y distance
     */
    public void pan(int dX, int dY) {
        conLog.info("Pan dX, dY = {}, {}", dX, dY);
        if (Math.abs(dX) < 1000 && Math.abs(dY) < 1000) {
            zoomPos.translate(dX, dY);
            repaint();
        }
    }

    /**
     * Min dim = [width=16434,height=16434]
     * Max dim = [width=29204,height=29204]
     * @param dZ
     */
    public void zoom(int dZ) {
        final Point curPoint = MouseInfo.getPointerInfo().getLocation();
        final Point relPoint = new Point(curPoint);
        SwingUtilities.convertPointFromScreen(relPoint, this);

        double prcnt = dZ/100.0; // e.g. 10/100.0 -> 10%
        conLog.info("Prcnt = {}", prcnt);
//        MoDimension fieldNewDim = new MoDimension(zoomDim, (1 + prcnt));
//        double newFieldW = prcnt * zoomDim.width;
//        double newFieldH = prcnt * zoomDim.height;

//        zoomDim.scalePerc(1 + prcnt);

//        int dX = - (int) (((relPoint.x - zoomPos.x) * 1.0 / zoomDim.width) * prcnt * zoomDim.width);
//        int dY = - (int) (((relPoint.y - zoomPos.y) * 1.0 / zoomDim.height) * prcnt * zoomDim.height);

//        double dX = - (0.5 * prcnt * zoomDim.width);
//        double dY = - (0.5 * prcnt * zoomDim.width);

//        zoomPos.x = (int) (zoomPos.x + dX);
//        zoomPos.y = (int) (zoomPos.y + dY);

//        zoomPos.translate(dX, dY);
        zoomDim.scalePerc(prcnt);
        zoomPos = new Point(-(zoomDim.width - getWidth())/2, -(zoomDim.height - getHeight())/2);

        conLog.info("Dim = {}", zoomDim);
        conLog.info("Pos: {}", relPoint);

        repaint();
    }

//    public void grab(Point grabPoint) {
//        // Get the start point
//        if (dragPoint == null) dragPoint = grabPoint;
//
//        // Change the cursor and set flag
//        getParent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//        isGrabbed = true;
//    }
//
//    public void grab() {
//        final Point grabPoint = MouseInfo.getPointerInfo().getLocation();
//        SwingUtilities.convertPointFromScreen(grabPoint, this);
//        // Get the start point
//        dragPoint = grabPoint;
//        // Change the cursor and set flag
//        getParent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//        isGrabbed = true;
//    }

    private void release() {
        // Change back the cursor and flag
        getParent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        isGrabbed = false;
        dragPoint = null;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // TEST
//        conLog.info("Button = {}", e.getButton());
//        if (e.getButton() == MouseEvent.BUTTON3) { // R-Cl
//            PanTask panTask = new PanTask(0, -40);
//            panner.scheduleAtFixedRate(panTask, 0, 10, TimeUnit.MILLISECONDS);
//        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Wheel press: button=2,modifiers=⌥+Button2,extModifiers=Button2
//        if (e.getButton() == 2) grab(e.getLocationOnScreen());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        getParent().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        release();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        conLog.info("Entered");
        isCursorInside = true;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        conLog.info("Exited");
        isCursorInside = false;
        release();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Wheel button: e.getModifiersEx() => Button2
//        if (e.getButton() == 2) pan(e.getLocationOnScreen());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
//        if (isGrabbed) {
//            pan(getCurPoint());
//        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        final int dZ = e.getWheelRotation();
//        detent -= dZ; // Zoom-in -> must be +
//        conLog.info("wR = {}", e.getWheelRotation());
        zoom(-e.getWheelRotation());
//        svgSize = findSVGSize(-rot);
//        repaint();
    }

    //===========================================================================
    @Override
    public void mooseClicked(Memo mem) {

    }

    @Override
    public void mooseScrolled(Memo mem) {

    }

    @Override
    public void mooseWheelMoved(Memo mem) {

    }

    @Override
    public void moosePanned(Memo mem) {
        pan(mem.getV1Float(), mem.getV1Float());
    }

    @Override
    public void mooseGrabbed(Memo mem) {
//        grab(MouseInfo.getPointerInfo().getLocation());
    }

    @Override
    public void mooseReleased(Memo mem) {
        release();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Is it relate to Moose?
        if (STRINGS.equals(evt.getPropertyName(), STRINGS.MOOSE)) {
            if (evt.getNewValue() != null) {
                Memo memo = (Memo) evt.getNewValue();

                switch (memo.getAction()) {
//                    case STRINGS.GRAB -> {
//                        // Grab if happened inside
//                        if (isCursorInside) {
//                            grab();
//                        }
//                    }

                    case STRINGS.REL -> release();

//                    case STRINGS.PAN -> {
//                        if (STRINGS.equals(memo.getMode(), STRINGS.VEL)) {
//                            pan(memo.getV1Float() * GAIN, memo.getV2Float() * GAIN);
//                        }
//
//                        if (STRINGS.equals(memo.getMode(), STRINGS.STOP)) {
//                            panner.shutdownNow();
//                        }
//                    }

                    case STRINGS.ZOOM -> {
                        zoom(memo.getV1Int());
                    }
                }
            }
        }

    }
}

