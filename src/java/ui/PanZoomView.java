package ui;

import control.Server;
import listener.MooseListener;
import model.MoPlane;
import model.MoPoint;
import model.PanZoomTrial;
import moose.Memo;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;
import tool.MoRect;
import tool.Resources;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static tool.Constants.*;

public class PanZoomView extends JPanel
        implements MouseListener, MouseMotionListener, MouseWheelListener, PropertyChangeListener {
    private final TaggedLogger conLog = Logger.tag(getClass().getSimpleName());

    // Keys
    private String VK_SPACE = String.valueOf(KeyEvent.VK_SPACE);

    // Properties
//    private MoDimension dim;
//    private MoPoint pos;

    // Rooms and locations
//    private HashMap<Integer, List<Point>> roomCircleLocations = new HashMap<>();

//    private MoDimension planesDim;

    // Trial
    private final PanZoomTrial trial;

    // Toggles
    private final boolean isLimitingPan = false;
    private final boolean isLimitingZoom = false;

    // View
    private MoPlane activePlane;
    private MoPlane trialPlane;
    private MoPlane plainPlane;

    private MoPoint planePos;

//    private final MoSVG trialPlane;
//    private final MoSVG plainPlane;

//    private final MoDimension planeDim;
//    private Point planePos; // Relative to this viewport! (set at paintComponent)

//    private MoRect maxZoomSq, minZoomSq; // Dim of the two squares around circles

    private double zoomLvl = 100; // Keep track of the zoom level

//    private SVGIcon thresholdSVGIcon;
//    private MoDimension thresholdDim = new MoDimension(200);
//    private Point thresholdPos; // Being uninitialized is important!
//    private FocusFrame thresholdSq;
    private BufferedImage image;

    // Panning
    private boolean isCursorInside = false;
    private boolean isGrabbed = false;
    private Point dragPoint;

//    private final Point padPos = new Point(0,0); // Changed only with padding
//    private Integer xDiff;
//    private Integer yDiff;
    private final ScheduledExecutorService panner = Executors.newSingleThreadScheduledExecutor();

    // Config
    private double cnfgZoomWheelNotchGain;
    private double cnfgPanGain;
    private double cnfgPanFriction;
    private double cnfgZoomGain;
    private double cnfgFlingGain;

    //------------------------------------------------------------------
    private class PanTask implements Runnable {
        int velX, velY;
        int dX, dY;

        public PanTask(double vX, double vY) {
            velX = (int) vX; // px/s -> px/(10)ms (10ms is the freq. of running the TaskType)
            velY = (int) vY; // px/s -> px/(10)ms
        }

        @Override
        public void run() {

            while (Math.abs(velX) > 0 || Math.abs(velY) > 0) {
                conLog.info("Run: vX, vY = {}, {}", velX, velY);
                panDisplace(velX, velY);
                velX *= (1 - cnfgPanFriction);
                velY *= (1 - cnfgPanFriction);

                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    //------------------------------------------------------------------

    public PanZoomView(PanZoomTrial trial) {
//        planesDim = new MoDimension(vpSize); // Initial dim
        this.trial = trial;

        // Add the focus frame
//        focusFrame = new FocusFrame();
//        focusFrame.setFocusable(false);
//        add(focusFrame);

        // Init
//        trialPlane = new MoSVG();
//        plainPlane = new MoSVG();
//        moose = new Moose();

        //– Listeners
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
//        moose.addMooseListener(this);
        Server.get().addPropertyChangeListener(this);


        // Set circle locations temporarily
//        roomCircleLocations.put(7, new ArrayList<>());
//        roomCircleLocations.get(7).add(new Point(12200, 6500));
//        roomCircleLocations.get(7).add(new Point(13500, 6500));
//        roomCircleLocations.get(7).add(new Point(12800, 7000));
//        roomCircleLocations.get(7).add(new Point(13500, 6000));

    }

    public void setConfig(double zoomWheelNotchGain, double panGain, double panFriction, double zoomGain) {
        cnfgZoomWheelNotchGain = zoomWheelNotchGain;
        cnfgPanGain = panGain;
        cnfgPanFriction = panFriction;
        cnfgZoomGain = zoomGain;

        conLog.info("New config set!");
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        setBorder(BORDERS.BLACK_BORDER);

        // Set position
        planePos = new MoPoint();
//        planePos = new MoPoint(
//                -(planesDim.width - getWidth())/2,
//                -(planesDim.height - getHeight())/2);

        // Create planes (w/ this as initial dimension)
        plainPlane = new MoPlane(Resources.SVG_PLAN_URI);
        plainPlane.setBounds(planePos, getSize());

        trialPlane = new MoPlane(Resources.SVG_PLAN_URI);
        trialPlane.setBounds(planePos, getSize());
        trialPlane.setTrialParts(1, 30);

//        plainPlane.setup(Resources.SVG.PLAN_URI);
//        trialPlane.setup(Resources.SVG.PLAN_URI);

//        if (planePos == null) {
//            planePos = new Point(-(planeDim.width - getWidth())/2, -(planeDim.height - getHeight())/2);
//        }

//        maxZoomSq = plainPlane.getZoomArea(trial.roomNum, "min");
//        maxZoomSq.setOrigin(planePos);
//        minZoomSq = plainPlane.getZoomArea(trial.roomNum, "max");
//        minZoomSq.setOrigin(planePos);

//        addTrialComponents();

        setZoomLevel(trial.initZoomLvl);
    }

    public void addTrialComponents() {
//        // Add circles
//        Rectangle roomMinArea = trialPlane.getZoomArea(trial.roomNum, "min");
//        conLog.trace("Room {} Min Area = {}", trial.roomNum, roomMinArea);
//        List<Circle> cList = generateCircles(roomMinArea.width, roomMinArea.x, roomMinArea.y, 30);
//        for (Circle c : cList) {
//            trialPlane.addCircle(c.radius, new Point(c.cx, c.cy), ExpFrame.HIGHLIGHT_COLOR);
//        }
//
//        // Paint room walls :)
//        trialPlane.paintRoom(trial.roomNum, ExpFrame.HIGHLIGHT_COLOR);
    }

    private Point getPos() {
        return new Point(getX(), getY());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        //– Draw the whole field
//        plainPlane.setSize(planeDim);
//        trialPlane.setSize(planeDim);

//        plane.setup(Resources.SVG.PLAN_URI);
        if (zoomLvl > ExpFrame.ZOOM_OUT_INFO_THRESHOLD) {
            activePlane = trialPlane;
//            trialPlane.paint(this, g, planePos);
//            trialPlane.paintRoom(trial.roomNum, ExpFrame.HIGHLIGHT_COLOR);
//            trialPlane.paintCirclesInRoom(trial.roomNum, ExpFrame.HIGHLIGHT_COLOR);
        } else {
            activePlane = plainPlane;
//            plainPlane.paint(this, g, planePos);
        }

        activePlane.paint(this, g, planePos);
        // Add text when zoom level gets in range
//        plane.removeText();
//        if (zoomLvl >= 800) {
//            MoPoint textPos = MoPoint.copyTranslated(roomCircleLocations.get(7).get(0), -25, 25);
//            plane.addText("5", new Point(200, 200), COLORS.BLACK);
//        }

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
//        thresholdSVGIcon.setPreferredSize(thresholdDim);
//        thresholdSVGIcon.paintIcon(this, g, thresholdPos.x, thresholdPos.y);
    }

//    private void colorThreshold(String color) {
//        SVGDiagram thresholdSvgDiagram = SVGCache.getSVGUniverse().getDiagram(Resources.SVG.THRESH_URI);
//        SVGRoot thresholdSvgRoot = thresholdSvgDiagram.getRoot();
//
//        List<SVGElement> children = new ArrayList<>();
//        thresholdSvgRoot.getChildren(children);
//
//        try {
//            children.get(0).setAttribute("stroke", 1, color);
//            children.get(1).setAttribute("stroke", 1, color);
//        } catch (SVGElementException e) {
//            conLog.warn("Element not found!");
//        }
//
//        repaint();
//    }

    private Point getCurPoint() {
        final Point p = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(p, this);

        return p;
    }

    /**
     * Position: -45 <= x, y <= -20
     * @param curPoint
     */
    public void panDisplace(Point curPoint) {
        if (dragPoint == null) grab(curPoint);

        int xDiff = curPoint.x - dragPoint.x;
        int yDiff = curPoint.y - dragPoint.y;

//        if (isLimitingPan) {
//            // Plane shouldn't go outside more than its size
//            final MoPoint posToCheck = MoPoint.copyTranslated(planePos, xDiff, yDiff);
//            if (posToCheck.isXInClosed(-(planeDim.width - getWidth()), 0)) {
//                dragPoint = curPoint;
//
//                planePos.translate(xDiff, yDiff);
////                padPos.translate(xDiff, yDiff);
//
//                conLog.info("Dim = {}", planeDim);
//                conLog.info("ZoomLvl = {}", zoomLvl);
//                conLog.info("Plane Pos = {}", planePos);
//
//                repaint();
//            }
//        }

        dragPoint = curPoint;

        planePos.translate(xDiff, yDiff);
//        minZoomSq.translate(xDiff, yDiff);
//        maxZoomSq.translate(xDiff, yDiff);
//                padPos.translate(xDiff, yDiff);

//        conLog.trace("Dim = {}", planeDim);
//        conLog.trace("ZoomLvl = {}", zoomLvl);
//        conLog.trace("Plane Pos = {}", planePos);

        repaint();


    }

    /**
     * Pan with the velocity
     * @param vX X veclocity
     * @param vY Y velocity
     */
    public void panFling(double vX, double vY) {
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
    public void panDisplace(int dX, int dY) {
        conLog.info("Pan dX, dY = {}, {}", dX, dY);
//        plainPlane.translate((int) (dX * PAN_GAIN), (int) (dY * PAN_GAIN));
//        trialPlane.translate((int) (dX * PAN_GAIN), (int) (dY * PAN_GAIN));
        planePos.translate((int) (dX * cnfgPanGain), (int) (dY * cnfgPanGain));
//        maxZoomSq.translate((int) (dX * PAN_GAIN), (int) (dY * PAN_GAIN));
//        minZoomSq.translate((int) (dX * PAN_GAIN), (int) (dY * PAN_GAIN));
        repaint();
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

        double scale = -dZ/100.0;
        focalZoomPercent(relPoint, scale);
        conLog.trace("Prcnt = {}", scale);
//        MoDimension fieldNewDim = new MoDimension(fieldDim, (1 + scale));
//        double newFieldW = scale * fieldDim.width;
//        double newFieldH = scale * fieldDim.height;

//        fieldDim.scalePerc(1 + scale);

//        final int plW = plainPlane.getWidth();
//        final int plH = plainPlane.getHeight();
//        final int dX = - (int) (((relPoint.x - planePos.x) * 1.0 / plW) * scale * plW);
//        final int dY = - (int) (((relPoint.y - planePos.y) * 1.0 / plH) * scale * plH);

//        int dX = - (int) (((relPoint.x - planePos.x) * 1.0 / planeDim.width) * scale * planeDim.width);
//        int dY = - (int) (((relPoint.y - planePos.y) * 1.0 / planeDim.height) * scale * planeDim.height);

        /// Only zoom in/out to certain extents
//        if (isLimitingZoom) {
//            if ((1 + scale) * zoomLvl >= ZOOM_OUT_MAX && (1 + scale) * zoomLvl <= ZOOM_IN_MAX) {
//                planePos.translate(dX, dY);
//                planeDim.scalePerc(scale);
//
//                conLog.info("Dim = {}", planeDim);
//                conLog.info("ZoomLvl = {}", zoomLvl);
//                conLog.info("Plane Pos = {}", planePos);
//
//                zoomLvl += scale * zoomLvl;
//                repaint();
//            }
//        } else {
//            planePos.translate(dX, dY);
//            planeDim.scalePerc(scale);
//
//            maxZoomSq.translate(dX, dY);
//            maxZoomSq.scalePerc(scale);
//
//            minZoomSq.translate(dX, dY);
//            minZoomSq.scalePerc(scale);
//
//            conLog.trace("Dim = {}", planeDim);
//            conLog.trace("ZoomLvl = {}", zoomLvl);
//            conLog.trace("Plane Pos = {}", planePos);
//
//            zoomLvl += scale * zoomLvl;
//            repaint();
//        }

//        planePos.translate(dX, dY);
//        plainPlane.scalePercent(scale);
//        trialPlane.scalePercent(scale);
////        planeDim.scalePerc(scale);
//
////        maxZoomSq.translate(dX, dY);
////        maxZoomSq.scalePerc(scale);
////
////        minZoomSq.translate(dX, dY);
////        minZoomSq.scalePerc(scale);
//
////        conLog.trace("Dim = {}", planeDim);
////        conLog.trace("ZoomLvl = {}", zoomLvl);
////        conLog.trace("Plane Pos = {}", planePos);
//
//        zoomLvl += scale * zoomLvl;
//        repaint();

    }

    /**
     * Min dim = [width=16434,height=16434]
     * Max dim = [width=29204,height=29204]
     * @param dZ
     */
    public void zoom(double dZ) {
        final Point curPoint = MouseInfo.getPointerInfo().getLocation();
        final Point relPoint = new Point(curPoint);
        SwingUtilities.convertPointFromScreen(relPoint, this);

        double prcnt = -dZ/100.0;
        focalZoomPercent(relPoint, prcnt);
        conLog.info("Prcnt = {}", prcnt);
//        MoDimension fieldNewDim = new MoDimension(fieldDim, (1 + prcnt));
//        double newFieldW = prcnt * fieldDim.width;
//        double newFieldH = prcnt * fieldDim.height;

//        fieldDim.scalePerc(1 + prcnt);
//        int dX = - (int) (((relPoint.x - planePos.x) * 1.0 / plDim.width) * prcnt * plDim.width);
//        int dY = - (int) (((relPoint.y - planePos.y) * 1.0 / plDim.height) * prcnt * plDim.height);

        /// Only zoom in/out to certain extents
//        if (isLimitingZoom) {
//            if ((1 + prcnt) * zoomLvl >= ZOOM_OUT_MAX && (1 + prcnt) * zoomLvl <= ZOOM_IN_MAX) {
//                planePos.translate(dX, dY);
//                planeDim.scalePerc(prcnt);
//
//                conLog.info("Dim = {}", planeDim);
//                conLog.info("ZoomLvl = {}", zoomLvl);
//                conLog.info("Plane Pos = {}", planePos);
//
//                zoomLvl += prcnt * zoomLvl;
//                repaint();
//            }
//        } else {
//            planePos.translate(dX, dY);
//            planeDim.scalePerc(prcnt);
//
//            maxZoomSq.translate(dX, dY);
//            maxZoomSq.scalePerc(prcnt);
//
//            minZoomSq.translate(dX, dY);
//            minZoomSq.scalePerc(prcnt);
//
//            conLog.trace("Dim = {}", planeDim);
//            conLog.trace("ZoomLvl = {}", zoomLvl);
//            conLog.trace("Plane Pos = {}", planePos);
//
//            zoomLvl += prcnt * zoomLvl;
//            repaint();
//        }

//        planePos.translate(dX, dY);
//        plainPlane.scalePercent(prcnt);
//        trialPlane.scalePercent(prcnt);
////        planeDim.scalePerc(prcnt);
////
////        maxZoomSq.translate(dX, dY);
////        maxZoomSq.scalePerc(prcnt);
////
////        minZoomSq.translate(dX, dY);
////        minZoomSq.scalePerc(prcnt);
//
////        conLog.trace("Dim = {}", planeDim);
////        conLog.trace("ZoomLvl = {}", zoomLvl);
////        conLog.trace("Plane Pos = {}", planePos);
//
//        zoomLvl += prcnt * zoomLvl;
//        repaint();

    }

    public void setZoomLevel(double newZoomLvl) {
        final Point curPoint = new Point(getX() + getWidth() / 2, getY() + getHeight() / 2); // center
        final Point relPoint = new Point(curPoint);
        SwingUtilities.convertPointFromScreen(relPoint, this);

        double zScale = (newZoomLvl - zoomLvl) / zoomLvl;
        focalZoomPercent(relPoint, zScale);
//        conLog.info("Prcnt = {}", prcnt);
//        MoDimension fieldNewDim = new MoDimension(fieldDim, (1 + prcnt));
//        double newFieldW = prcnt * fieldDim.width;
//        double newFieldH = prcnt * fieldDim.height;

//        fieldDim.scalePerc(1 + prcnt);
//        final MoDimension plDim = plainPlane.getDim();
//        int dX = - (int) (((relPoint.x - planePos.x) * 1.0 / plDim.width) * prcnt * plDim.width);
//        int dY = - (int) (((relPoint.y - planePos.y) * 1.0 / plDim.height) * prcnt * plDim.height);
//
//        planePos.translate(dX, dY);
//        plainPlane.scalePercent(prcnt);
//        trialPlane.scalePercent(prcnt);
////        planeDim.scalePerc(prcnt);
//
////        maxZoomSq.translate(dX, dY);
////        maxZoomSq.scalePerc(prcnt);
////
////        minZoomSq.translate(dX, dY);
////        minZoomSq.scalePerc(prcnt);
//
//        zoomLvl += prcnt * zoomLvl;
//
////        conLog.info("Dim = {}", planeDim);
////        conLog.info("ZoomLvl = {}", zoomLvl);
////        conLog.info("Plane Pos = {}", planePos);
//
//        repaint();

    }

    public void focalZoomPercent(Point relPoint, double scale) {
        final int plW = plainPlane.getWidth();
        final int plH = plainPlane.getHeight();
        final int dX = - (int) (((relPoint.x - planePos.x) * 1.0 / plW) * scale * plW);
        final int dY = - (int) (((relPoint.y - planePos.y) * 1.0 / plH) * scale * plH);

        planePos.translate(dX, dY);
        plainPlane.scale(scale);
        trialPlane.scale(scale);

        zoomLvl += scale * zoomLvl;
        repaint();
    }

    public void grab(Point grabPoint) {
        // Get the start point
        if (dragPoint == null) dragPoint = grabPoint;

        // Change the cursor and set flag
        getParent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        isGrabbed = true;
    }

    public void grab() {
        final Point grabPoint = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(grabPoint, this);
        // Get the start point
        dragPoint = grabPoint;
        // Change the cursor and set flag
        getParent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        isGrabbed = true;
    }

    private void release() {
        // Change back the cursor and flag
        getParent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        isGrabbed = false;
        dragPoint = null;
    }

    /**
     * Check if the trial is a hit (destination matches the view)
     * @return True if Hit, false otherwise
     */
    public boolean isSuccess() {
        // Get the zoom dest sqs coords relative to this view
        MoRect destMaxZoomSq = trialPlane.getDestMaxZoomSq();
        MoRect destMinZoomSq = trialPlane.getDestMinZoomSq();
        destMaxZoomSq.setOrigin(planePos);
        destMinZoomSq.setOrigin(planePos);

        // Create the rectangle for the view
        MoRect viewRect = new MoRect(0, 0, getWidth(), getHeight());

        // Check if the circles are fully inside the view (i.e., the min zoom sq is inside the view)
        final boolean areCirclesVisible = viewRect.contains(destMaxZoomSq);

//        conLog.info("PlanePos = {}", planePos);
//        conLog.info("Inside? {}", viewRect.contains(destMaxZoomSq));
//        conLog.info("planeDim = {}; minRect: {}; maxRect: {}", planeDim, minZoomSq, maxZoomSq);

        // Check if no walls are shown (i.e., view is inside the max zoom sq)
        conLog.info("Walls not showing: {}", destMinZoomSq.contains(viewRect));
        conLog.info("View: {}; Dest Zoom Sq: {}", viewRect, destMinZoomSq);

        return viewRect.contains(destMaxZoomSq);
    }

    //----------------------------------------------------------------------------------------------------
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
        if (e.getButton() == 2) grab(e.getLocationOnScreen());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        getParent().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        release();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        conLog.trace("Entered");
        isCursorInside = true;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        conLog.trace("Exited");
        isCursorInside = false;
        release();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Wheel button: e.getModifiersEx() => Button2
        if (e.getButton() == 2) {
            conLog.trace("Dragging...");
            getParent().setCursor(new Cursor(Cursor.HAND_CURSOR));
            panDisplace(e.getLocationOnScreen());
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (isGrabbed) {
            panDisplace(getCurPoint());
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
//        final int dZ = e.getWheelRotation();
//        detent -= dZ; // Zoom-in -> must be +
        conLog.info("Rotation = {}, Prec. Rot = {}",
                e.getWheelRotation(), e.getPreciseWheelRotation());
        final double dZ = e.getPreciseWheelRotation() * cnfgZoomWheelNotchGain;
        zoom(dZ);
//        svgSize = findSVGSize(-rot);
//        repaint();
    }

    //===========================================================================
//    @Override
//    public void mooseClicked(Memo mem) {
//
//    }
//
//    @Override
//    public void mooseScrolled(Memo mem) {
//
//    }
//
//    @Override
//    public void mooseWheelMoved(Memo mem) {
//
//    }
//
//    @Override
//    public void moosePanned(Memo mem) {
////        panDisplace(mem.getV1Float(), mem.getV1Float());
//    }

//    @Override
//    public void mooseGrabbed(Memo mem) {
//        grab(MouseInfo.getPointerInfo().getLocation());
//    }
//
//    @Override
//    public void mooseReleased(Memo mem) {
//        release();
//    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Is it relate to Moose?
        if (STR.equals(evt.getPropertyName(), STR.MOOSE)) {
            if (evt.getNewValue() != null) {
                Memo memo = (Memo) evt.getNewValue();

                switch (memo.getAction()) {
                    case STR.GRAB -> {
                        // Grab if happened inside
                        if (isCursorInside) {
                            grab();
                        }
                    }

                    case STR.REL -> release();

                    case STR.PAN -> {

                        // Pan displace
                        if (STR.equals(memo.getMode(), STR.DISPLACE)) {
                            panDisplace((int) (memo.getV1Float()), (int) (memo.getV2Float()));
                        }

                        // Zoom
                        if (STR.equals(memo.getMode(), STR.STOP)) {
                            panner.shutdownNow();
                        }
                    }

                    case STR.FLING -> {
                        conLog.info("Flinging...");
                        panFling(memo.getV1Float() * cnfgFlingGain, memo.getV2Float() * cnfgFlingGain);
                    }

                    case STR.ZOOM -> {
                        zoom(memo.getV1Int() * cnfgZoomGain);
                    }
                }
            }
        }

    }
}
