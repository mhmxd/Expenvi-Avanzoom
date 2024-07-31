package ui;

import com.kitfox.svg.SVGCache;
import logs.MoLogger;
import control.Server;
import model.MoPlane;
import model.MoPoint;
import model.MoSVG;
import model.PanZoomTrial;
import moose.Memo;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;
import tool.MoRect;
import tool.Resources;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static tool.Constants.*;

public class PanZoomView extends JPanel
        implements MouseListener, MouseMotionListener, MouseWheelListener, PropertyChangeListener {
    private final TaggedLogger conLog = Logger.tag("CONSOLE");
    private final TaggedLogger trialLog = Logger.tag("TRIAL");
    private final TaggedLogger eventLog = Logger.tag("EVENT");
    private final TaggedLogger motionLog = Logger.tag("MOTION");

    private Instant firstPan = Instant.EPOCH, firstZoom = Instant.EPOCH;

    // Constants
//    private String VK_SPACE = String.valueOf(KeyEvent.VK_SPACE);
    private final Cursor DEF_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);
    private final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);
//    private final Cursor DEF_CURSOR = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
//    private final Cursor HAND_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

    // Trial
    private final PanZoomTrial trial;

    // Toggles
    private final boolean isLimitedToView = true;
    private final boolean isLimitingZoom = true;
    private boolean isFirstView = true;

    // View
    private MoSVG activeSVG;
    private final MoSVG plainSVG = new MoSVG();
    private MoSVG trialSVG;

    private MoPlane activePlane;
//    private MoPlane trialPlane;
//    private MoPlane plainPlane;
    private MoRect abstractPlane; // Used for calculating the scales, etc.

    private final MoPoint planePos = new MoPoint();
    private MoPoint initPlanePose = new MoPoint();
    private boolean isTrialRunning;

    private double zoomLvl = 100; // Keep track of the zoom level

    // Panning
    private boolean isCursorInside = false;
    private boolean isGrabbed = false;
    private Point dragPoint;

    private final ScheduledExecutorService panner = Executors.newSingleThreadScheduledExecutor();

    // Design
    private int dsgnPanEndThredholdW; // px
    private int dsgnPanEndThredholdH; // px

    // Config
    private double cnfgZoomWheelNotchGain;
    private double cnfgPanGain;
    private double cnfgZoomGain;
    private double cnfgFlingVelGain;
    private double cnfgFlingVelFriction;

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
                velX *= (1 - cnfgFlingVelFriction);
                velY *= (1 - cnfgFlingVelFriction);

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
        this.trial = trial;
        isTrialRunning = false;

        //– Listeners
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
//        moose.addMooseListener(this);
        Server.get().addPropertyChangeListener(this);

        // Clear any previous svg cache
        SVGCache.getSVGUniverse().clear();

        // LOG
        trialLog.info("P{} – Trial: {}; {}; {}",
                ExpFrame.pID,
                trial.id, trial.blockNum, trial.trialNum);
        eventLog.info("P{} – Trial: {}; {}; {}",
                ExpFrame.pID,
                trial.id, trial.blockNum, trial.trialNum);
    }

    public void setDesignProperties(double panEndThreshold) {
        dsgnPanEndThredholdW = DISP.mmToPxW(panEndThreshold);
        dsgnPanEndThredholdH = DISP.mmToPxH(panEndThreshold);
    }

    public void setPanConfig(double panGain) {
        cnfgPanGain = panGain;
        conLog.info("Pan config set!");
    }

    public void setZoomConfig(double zoomWheelNotchGain, double zoomGain) {
        cnfgZoomWheelNotchGain = zoomWheelNotchGain;
        cnfgZoomGain = zoomGain;
        conLog.info("Zoom config set!");
    }

    public void setFlingConfig(double flingVelGain, double flingVelFriction) {
        cnfgFlingVelGain = flingVelGain;
        cnfgFlingVelFriction = flingVelFriction;
        conLog.info("Fling config set!");
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        trialLog.info("Open");
        eventLog.info("Open");
        firstPan = Instant.MIN;
        firstZoom = Instant.MIN;

        setBorder(BORDERS.BLACK_BORDER);
        conLog.trace("Size: {}", getSize());
        // Create planes (w/ this as initial dimension)
        activePlane = new MoPlane();
        activePlane.setBounds(initPlanePose, getSize());

        // Set up SVGs
        plainSVG.setup(Resources.PLAIN_PLAN_URI);
        plainSVG.setSize(getSize());

        trialSVG = new MoSVG();
        trialSVG.setup(Resources.TRIAL_PLAN_URI);
        trialSVG.setSize(getSize());
        trialSVG.setTrialParts(trial.roomNum, 30);

        abstractPlane = new MoRect(initPlanePose, getSize());

        repaint();

        // Initial setting
        activeSVG = trialSVG;
//        activePlane = trialPlane;
        conLog.info("Init ZL = {}", trial.initZoomLvl);
//        zoomLvl = trial.initZoomLvl;
        setZoomLevel(trial.initZoomLvl);

        // Check if the cursor is inside
        final Point curPoint = getCurPoint();
        conLog.info("curPoint = {}, inside? {}, {}", curPoint, contains(curPoint),
                getBounds().contains(curPoint));
        if (contains(curPoint)) {
            isCursorInside = true;
        }


    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        conLog.trace("Plane position: {}, Zoom Lvl = {}", planePos, zoomLvl);

        if (zoomLvl > ExpFrame.ZOOM_OUT_INFO_THRESHOLD) {
//            plainSVG.paint(g, planePos);
            activeSVG = trialSVG;
//            trialPlane.paint(this, g, planePos);
        } else {
//            trialSVG.paint(g, planePos);
            activeSVG = plainSVG;
//            activePlane = plainPlane;
//            plainPlane.paint(this, g, planePos);
        }

        activeSVG.paint(g, planePos);

//        if (isFirstView) {
////            zoomLvl = trial.initZoomLvl;
//            setZoomLevel(trial.initZoomLvl);
//            isFirstView = false;
//        }
    }

    private Point getCurPoint() {
        final Point p = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(p, this);

        return p;
    }

    /**
     * @param curPoint
     */
    public void panDisplace(Point curPoint) {
        eventLog.info("Pan Displace – Cursor Point = {}", curPoint);
        if (firstPan == Instant.MIN) firstPan = Instant.now();

        if (dragPoint == null) grab(curPoint);

        int dX = curPoint.x - dragPoint.x;
        int dY = curPoint.y - dragPoint.y;

        // Plane shouldn't go outside more than its size
        if (isLimitedToView) {
            if (checkTransform(dX, dY, 0)) {
                planePos.translate(dX, dY);
                dragPoint = curPoint;
                repaint();
            }

        }

    }

    /**
     * Pan with the velocity
     * @param vX X veclocity
     * @param vY Y velocity
     */
    public void panFling(double vX, double vY) {
        conLog.info("vX, vY = {}, {}", vX, vY);

        PanTask panTask = new PanTask(vX, vY);
        panner.scheduleAtFixedRate(panTask, 0, 10, TimeUnit.MILLISECONDS);

        // LOG
        eventLog.info("Pan Fling – vX, vY = {}, {}", vX, vY);
        if (firstPan == Instant.MIN) firstPan = Instant.now();
    }

    /**
     * Pan with displacement
     * @param dX – X distance
     * @param dY – Y distance
     */
    public void panDisplace(int dX, int dY) {
        conLog.info("Pan dX, dY = {}, {}", dX, dY);
        eventLog.info("Pan Displace – dX, dY = {}, {}", dX, dY);
        if (firstPan == Instant.MIN) firstPan = Instant.now();

        planePos.translate((int) (dX * cnfgPanGain), (int) (dY * cnfgPanGain));
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
        eventLog.info("Zoom – prcnt = {}", prcnt);
    }

    public void setZoomLevel(double newZoomLvl) {
        final Point centerPoint = new Point(getWidth() / 2, getHeight() / 2); // center
        double zScale = (newZoomLvl - zoomLvl) / zoomLvl;
        conLog.info("newZoomLvl - zoomLvl = {}", newZoomLvl - zoomLvl);
        conLog.info("zScale = {}", zScale);
        focalZoomPercent(centerPoint, zScale);

        // LOG
        eventLog.info("Zoom – level = {}", newZoomLvl);
    }

    public void focalZoomPercent(Point relPoint, double scale) {
        conLog.info("Focal point: {}, Scale = {}", relPoint, scale);
        eventLog.info("Zoom – Scale = {}", scale);
        if (firstZoom == Instant.MIN) firstZoom = Instant.now();

        final int plW = activeSVG.getIconWidth();
        final int plH = activeSVG.getIconHeight();
        final int dX = - (int) (((relPoint.x - planePos.x) * 1.0 / plW) * scale * plW);
        final int dY = - (int) (((relPoint.y - planePos.y) * 1.0 / plH) * scale * plH);
        conLog.info("dX, dY = {}, {}", dX, dY);

        if (isLimitedToView) {
            final boolean canTransform = checkTransform(dX, dY, scale);

            if (canTransform) {
                conLog.info("Zooming...");
                planePos.translate(dX, dY);

                plainSVG.scale(scale);
                trialSVG.scale(scale);

                zoomLvl += scale * zoomLvl;

                repaint();
            }
        }

    }

    private boolean checkTransform(int dX, int dY, double scale) {
        final MoPoint posToCheck = MoPoint.copyTranslated(planePos, dX, dY);
        abstractPlane.scale(scale);
        conLog.trace("posToCheck = {}", posToCheck);

        int leftThreshold = - abstractPlane.width + dsgnPanEndThredholdW;
        int rightThreshold = getWidth() - dsgnPanEndThredholdW;
        int topThreshold = - abstractPlane.height + dsgnPanEndThredholdH;
        int bottomThreshold = getHeight() - dsgnPanEndThredholdH;
        conLog.trace("Thresholds: {}, {}, {}, {}",
                leftThreshold, rightThreshold, topThreshold, bottomThreshold);

        return posToCheck.isXYInClosed(
                leftThreshold, rightThreshold,
                topThreshold, bottomThreshold);
    }

    public void grab(Point grabPoint) {
        eventLog.info("Mouse – Grab {}", grabPoint);
        // Get the start point
        if (dragPoint == null) dragPoint = grabPoint;

        // Change the cursor and set flag
//        getParent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        isGrabbed = true;
    }

    public void grab() {
        eventLog.info("Mouse – Grab");
        final Point grabPoint = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(grabPoint, this);
        // Get the start point
        dragPoint = grabPoint;
        // Change the cursor and set flag
//        getParent().setCursor();
        isGrabbed = true;
    }

    private void release() {
        eventLog.info("Mouse – Release");
        // Change back the cursor and flag
//        getParent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        isGrabbed = false;
        dragPoint = null;
    }

    /**
     * Check if the trial is a hit (destination matches the view)
     * @return True if Hit, false otherwise
     */
    public boolean isSuccess() {
        // Get the zoom dest sqs coords relative to this view
        MoRect destMaxZoomSq = trialSVG.getDestMaxZoomSq();
        MoRect destMinZoomSq = trialSVG.getDestMinZoomSq();
        destMaxZoomSq.setOrigin(planePos);
        destMinZoomSq.setOrigin(planePos);

        // Create the rectangle for the view
        MoRect viewRect = new MoRect(0, 0, getWidth(), getHeight());

        // Check if no walls are shown (i.e., view is inside the max zoom sq)
        final boolean areWallsInvisible = destMinZoomSq.contains(viewRect);
        final boolean areCirclesVisible = viewRect.contains(destMaxZoomSq);
        conLog.info("Walls not showing: {}", areWallsInvisible);
        conLog.info("Circles showing: {}" , areCirclesVisible);
        conLog.info("View: {}; Dest Min ZSq: {}; Dest Max ZSq: {}",
                viewRect, destMinZoomSq, destMaxZoomSq);
        trialLog.info("Close – Walls & Circles – {}, {}", areWallsInvisible, areCirclesVisible);

        Long panTrialTime = Duration.between(firstPan, Instant.now()).toMillis();
        Long zoomTrialTime = Duration.between(firstZoom, Instant.now()).toMillis();
        trialLog.info("Trial Time – From Pan = {}, Zoom = {}", panTrialTime, zoomTrialTime);
        trialLog.info("-----------------------------------------------------");
        eventLog.info("Close – Walls & Circles – {}, {}", areWallsInvisible, areCirclesVisible);
        eventLog.info("-----------------------------------------------------");
        return areWallsInvisible && areCirclesVisible;
    }

    //----------------------------------------------------------------------------------------------------
    @Override
    public void mouseClicked(MouseEvent e) {
        motionLog.info("MouseClicked – {}", e.paramString());
        // TEST
//        conLog.info("Button = {}", e.getButton());
//        if (e.getButton() == MouseEvent.BUTTON3) { // R-Cl
//            PanTask panTask = new PanTask(0, -40);
//            panner.scheduleAtFixedRate(panTask, 0, 10, TimeUnit.MILLISECONDS);
//        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        motionLog.info("MousePressed – {}", e.paramString());
        MoLogger.get().logMouseEvent(e);
        // Wheel press: button=2,modifiers=⌥+Button2,extModifiers=Button2
        if (e.getButton() == 2 || e.getButton() == 3) {
            grab(e.getLocationOnScreen());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        motionLog.info("MouseReleased – {}", e.paramString());
//        getParent().setCursor(DEF_CURSOR);
        release();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        motionLog.info("MouseEntered – {}", e.paramString());
        conLog.trace("Entered");
        isCursorInside = true;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        motionLog.info("MouseExited – {}", e.paramString());
        conLog.trace("Exited");
        isCursorInside = false;
        release();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        motionLog.info("MouseDragged – {}", e.paramString());
        // Wheel button: e.getModifiersEx() => Button2
        if (e.getButton() == 2 || e.getButton() == 3) {
            setCursor(HAND_CURSOR);
            panDisplace(e.getLocationOnScreen());
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        motionLog.info("MouseMoved – {}", e.paramString());
        if (isGrabbed) {
            panDisplace(getCurPoint());
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        motionLog.info("MouseWheelEvent – {}", e.paramString());
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
                            if (isCursorInside) {
                                panDisplace(
                                        (int) (memo.getV1Float()),
                                        (int) (memo.getV2Float()));
                            }
                        }

                        // Stop
                        if (STR.equals(memo.getMode(), STR.STOP)) {
                            if (isCursorInside) {
                                panner.shutdownNow();
                            }
                        }
                    }

                    case STR.FLING -> {
                        conLog.info("Flinging...");
                        if (isCursorInside) {
                            panFling(
                                    memo.getV1Float() * cnfgFlingVelGain,
                                    memo.getV2Float() * cnfgFlingVelGain);
                        }
                    }

                    case STR.ZOOM -> {
                        if (isCursorInside) {
                            zoom(memo.getV1Int() * cnfgZoomGain);
                        }
                    }
                }
            }
        }

    }
}
