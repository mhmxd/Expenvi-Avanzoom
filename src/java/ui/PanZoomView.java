package ui;

import control.Server;
import model.MoPlane;
import model.MoPoint;
import model.PanZoomTrial;
import moose.Memo;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;
import tool.MoDimension;
import tool.MoRect;
import tool.Resources;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static tool.Constants.*;

public class PanZoomView extends JPanel
        implements MouseListener, MouseMotionListener, MouseWheelListener, PropertyChangeListener {
    private final TaggedLogger conLog = Logger.tag(getClass().getSimpleName());

    // Constants
//    private String VK_SPACE = String.valueOf(KeyEvent.VK_SPACE);
    private final Cursor DEF_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);
    private final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);
//    private final Cursor DEF_CURSOR = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
//    private final Cursor HAND_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

    // Trial
    private final PanZoomTrial trial;

    // Toggles
    private final boolean isLimitingPan = true;
//    private final boolean isLimitingZoom = true;

    // View
    private MoPlane activePlane;
    private MoPlane trialPlane;
    private MoPlane plainPlane;

    private final MoPoint planePos = new MoPoint();
    private MoPoint initPlanePose = new MoPoint();
    private boolean isTrialRunning;

    private double zoomLvl = 100; // Keep track of the zoom level

    // Panning
    private boolean isCursorInside = false;
    private boolean isGrabbed = false;
    private Point dragPoint;

    private final ScheduledExecutorService panner = Executors.newSingleThreadScheduledExecutor();

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

        setBorder(BORDERS.BLACK_BORDER);

        // Create planes (w/ this as initial dimension)
        plainPlane = new MoPlane(Resources.PLAIN_PLAN_URI);
        plainPlane.setBounds(initPlanePose, getSize());

        trialPlane = new MoPlane(Resources.TRIAL_PLAN_URI);
        final MoDimension testDim = new MoDimension(getSize(), -400);
        trialPlane.setBounds(initPlanePose, getSize());
//        conLog.info("Room: {}", trial.roomNum);
        trialPlane.setTrialParts(trial.roomNum, 30);

        setZoomLevel(trial.initZoomLvl);

        // Check if the cursor is inside
        if (getBounds().contains(getCurPoint())) {
            isCursorInside = true;
        }
    }


    @Override
    protected void paintComponent(Graphics g) {
        conLog.trace("Trial Running: {}, Plane position: {}", isTrialRunning, planePos);
        super.paintComponent(g);
        conLog.info("Zoom Lvl: {}", zoomLvl);
        if (zoomLvl > ExpFrame.ZOOM_OUT_INFO_THRESHOLD) {
            activePlane = trialPlane;
        } else {
            activePlane = plainPlane;
        }

        activePlane.paint(this, g, planePos);
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
    public void panDisplace(Point curPoint) {
        if (dragPoint == null) grab(curPoint);

        int xDiff = curPoint.x - dragPoint.x;
        int yDiff = curPoint.y - dragPoint.y;

        if (isLimitingPan) {
            // Plane shouldn't go outside more than its size
            final MoPoint posToCheck = MoPoint.copyTranslated(planePos, xDiff, yDiff);
            if (posToCheck.isXInClosed(-(activePlane.getWidth() - getWidth()), 0)) {
                dragPoint = curPoint;

                planePos.translate(xDiff, yDiff);
//                padPos.translate(xDiff, yDiff);

//                conLog.info("Dim = {}", planeDim);
                conLog.info("ZoomLvl = {}", zoomLvl);
                conLog.info("Plane Pos = {}", planePos);

                repaint();
            }
        }

        dragPoint = curPoint;

        planePos.translate(xDiff, yDiff);

        repaint();
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
    }

    /**
     * Pan with displacement
     * @param dX – X distance
     * @param dY – Y distance
     */
    public void panDisplace(int dX, int dY) {
        conLog.info("Pan dX, dY = {}, {}", dX, dY);
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
    }

    public void setZoomLevel(double newZoomLvl) {
        final Point centerPoint = new Point(getWidth() / 2, getHeight() / 2); // center
        double zScale = (newZoomLvl - zoomLvl) / zoomLvl;
        focalZoomPercent(centerPoint, zScale);
    }

    public void focalZoomPercent(Point relPoint, double scale) {
        conLog.info("Focal point: {}", relPoint);
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
//        getParent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        isGrabbed = true;
    }

    public void grab() {
        final Point grabPoint = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(grabPoint, this);
        // Get the start point
        dragPoint = grabPoint;
        // Change the cursor and set flag
//        getParent().setCursor();
        isGrabbed = true;
    }

    private void release() {
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
        MoRect destMaxZoomSq = trialPlane.getDestMaxZoomSq();
        MoRect destMinZoomSq = trialPlane.getDestMinZoomSq();
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

        return areWallsInvisible && areCirclesVisible;
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
        if (e.getButton() == 2 || e.getButton() == 3) {
            grab(e.getLocationOnScreen());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
//        getParent().setCursor(DEF_CURSOR);
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
        if (e.getButton() == 2 || e.getButton() == 3) {
            setCursor(HAND_CURSOR);
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
