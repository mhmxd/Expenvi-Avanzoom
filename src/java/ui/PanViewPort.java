package ui;

import com.google.common.base.Stopwatch;
import com.kitfox.svg.SVGCache;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGRoot;
import com.kitfox.svg.animation.AnimationElement;
import com.kitfox.svg.app.beans.SVGIcon;
import com.kitfox.svg.app.beans.SVGPanel;
import control.Logex;
import control.Server;
import enums.TrialEvent;
import enums.TrialStatus;
import model.PanTrial;
import moose.Memo;
import moose.Moose;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;
import tool.Constants;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.abs;
import static tool.Constants.BORDERS;
import static tool.Constants.*;
import static ui.PanPanel.focusAreaDim;

public class PanViewPort extends JPanel implements MouseListener, MouseMotionListener, PropertyChangeListener {
    private final TaggedLogger conLog = Logger.tag(getClass().getSimpleName());

    private final int BLINKER_DELAY = 50; // ms

    //-- Trial
    private final PanTrial trial;
    private final AbstractAction endTrialAction; // Received from higher levels
    private int nScansCurveInsideFocus, nScans;

    private ScheduledExecutorService panner = Executors.newSingleThreadScheduledExecutor();
//    private final double PAN_FRICTION = 0.1; // Values must be consistent (see below)
//    private final double PAN_GAIN = 0.01;

    // Config
    private double cnfgPanFriction;
    private double cnfgPanGain;
    // View
    private final SVGIcon icon;
    private int rotate;
    private Point dragPoint;
    private Integer xDiff;
    private Integer yDiff;
    private final PanFocusArea focusArea;
    private boolean hasFocus = false;
    private boolean isPanning;
    private boolean isTrialActive;
    private static final int startPosX = 100;
    private static final int startPosY = 500;
//    private static final int startBorderSize = 100;
//    private final int focusAreaSize;

    private BufferedImage image;
    private final Stopwatch insideFocusStopwatch;

    // Timers ---------------------------------------------------------------------------------
    private final Timer borderBlinker = new Timer(BLINKER_DELAY, new ActionListener() {
        private Border currentBorder;
        private int count = 0;
        private final Border border1 = new LineBorder(Color.YELLOW, BORDERS.THICKNESS_2);
        private final Border border2 = new LineBorder(Color.RED, BORDERS.THICKNESS_2);

        @Override
        public void actionPerformed(ActionEvent e) {
            if (count == 0) {
                currentBorder = getBorder();
            }

            if (count % 2 == 0) {
                setBorder(border1);
            } else {
                setBorder(border2);
            }

            count++;

            if (count > 5) {
                borderBlinker.stop();
                setBorder(currentBorder);
                count = 0;
            }
        }
    });

    //------------------------------------------------------------------
    private class PanTask implements Runnable {
        int velX, velY;

        public PanTask(double vX, double vY) {
            velX = (int) vX; // px/s -> px/(10)ms (10ms is the freq. of running the TaskType)
            velY = (int) vY; // px/s -> px/(10)ms
        }

        @Override
        public void run() {

            while (hasFocus && (abs(velX) + abs(velY) > 0)) {
                pan(velX, velY);

                if (abs(velX) > 0){
                    int xDir = velX / abs(velX);
                    velX = (int) (abs(velX) - cnfgPanFriction * abs(velX)) * xDir;
                }

                if (abs(velY) > 0){
                    int yDir = velY / abs(velY);
                    velY = (int) (abs(velY) - cnfgPanFriction * abs(velY)) * yDir;
                }

                conLog.info("Run: vX, vY = {}, {}", velX, velY);

                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Constructor
     * @param pt PanTrial
     * @param endTrAction AbstractAction
     */
    public PanViewPort(Moose moose, PanTrial pt, AbstractAction endTrAction) {
        icon = new SVGIcon();
        icon.setAntiAlias(true);
        icon.setAutosize(SVGPanel.AUTOSIZE_NONE);

        trial = pt;
        endTrialAction = endTrAction;

        // Init
//        focusAreaSize = Utils.mm2px(PanPanel.FOCUS_SIZE_mm);
        insideFocusStopwatch = Stopwatch.createUnstarted();

        // Add the focus area
        focusArea = new PanFocusArea();
        focusArea.setLayout(null);
        focusArea.setOpaque(false);
        focusArea.setFocusable(false);
        add(focusArea);

        // Set listeners
        addMouseListener(this);
        addMouseMotionListener(this);
//        moose.addMooseListener(this);

        Server.get().addPropertyChangeListener(this);

        // Get config
//        PAN_GAIN = ExpFrame.config.getDouble(STRINGS.PAN_GAIN);
//        PAN_FRICTION = ExpFrame.config.getDouble(STRINGS.PAN_FRICTION);
    }

    public void setConfig(double panGain, double panFriction) {
        cnfgPanGain = panGain;
        cnfgPanFriction = panFriction;

        conLog.info("New config set!");
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        conLog.info("setVisible");
        if (aFlag) startTrial(trial.uri, trial.rotation);
    }

    /**
     * Start the trial
     * @param uri URI
     * @param rotation int
     */
    public void startTrial(URI uri, int rotation) {
        conLog.info("startTrial");
        icon.setSvgURI(uri);
        rotate = rotation;
        xDiff = null;
        yDiff = null;

        focusArea.setActive(false);
        isTrialActive = true;

        SVGDiagram diagram = SVGCache.getSVGUniverse().getDiagram(uri);
        SVGRoot root = diagram.getRoot();

        StringBuilder builder = new StringBuilder();
        builder.append("\"rotate(").append(rotate).append(" ").append(startPosX).append(" ").append(startPosY).append(")\"");
        try {
            if (root.hasAttribute("transform", AnimationElement.AT_XML)) {
                root.setAttribute("transform", AnimationElement.AT_XML, builder.toString());
            } else {
                root.addAttribute("transform", AnimationElement.AT_XML, builder.toString());
            }
            root.updateTime(0f);
        } catch (SVGException ignored) {
        }

        repaint();
    }


    /**
     * Check whether the trial is at the end (circle is inside focus area)
     * Also, manages the focus area's activatino/deactivation
     * @return True (Hit) or False
     */
    protected boolean isTrialFinished() {

        paintComponent(image.getGraphics());

        int[] focusAreaPixels = image.getRGB(
                focusArea.getX(),
                focusArea.getY(),
                focusArea.getWidth(),
                focusArea.getHeight(),
                null, 0, focusArea.getWidth());

        scanFocusAreaForCurve(focusAreaPixels);
        return scanFocusAreaForLineEnd(focusAreaPixels);
    }

    /**
     * Check whether a (RGB) pixel array include a color
     * @param pixelArray Array of TYPE_INT_ARGB
     * @param color COlor to check
     * @return True if there is one pixel with the input color
     */
    private boolean hasColor(int[] pixelArray, Color color) {
        for (int p : pixelArray) {
            Color pc = new Color(p);
            if (pc.equals(color)) return true;
        }

        return false;
    }

    /**
     * Scan the focus area for the curve
     * @param focusPixels Array of the RGBA pixels
     */
    private void scanFocusAreaForCurve(int[] focusPixels) {
        // Has the curve entered the focus area?
        boolean focusEntered = Logex.get().hasLogged(TrialEvent.getFirst(TrialEvent.FOCUS_ENTER));

        // Check if line is inside focus area
        if (focusEntered) nScans++; // Only count after entering the focus area
        for (int c : focusPixels) {
            Color clr = new Color(c);
            if (clr.equals(Color.BLACK)) {
                focusArea.setActive(true);
                if (focusEntered) nScansCurveInsideFocus++; // Only count after entering the focus area

                logInsideFocus(); // LOG
                return;
            }
        }

        focusArea.setActive(false);

        // No black was found
        logOutsideFocus();
    }

    /**
     * Scan the focus area for the end-circle color.
     * The moment the color of the end circle is found, return.
     * @param focusPixels Array of RGBA pixels
     */
    private boolean scanFocusAreaForLineEnd(int[] focusPixels) {
        conLog.trace("Num of focus pixels = {}", focusPixels.length);
        for (int c : focusPixels) {
            Color clr = new Color(c);
            if (clr.equals(COLORS.YELLOW)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check the accruacy (called AFTER finishing the trial)
     * @return True (> 90% of the curved traversed inside the focus area)
     */
    private boolean wasTrialAccurate() {
//        return ((double) nScansCurveInsideFocus / nScans) >= 0.9;
        return true;
    }

    /**
     * Translate the content inside by dX and dY
     * @param dX Delta-X
     * @param dY Delta-Y
     */
    public void translate(int dX, int dY) {
        Logex.get().logEvent(TrialEvent.PAN); // LOG

        this.xDiff += dX;
        this.yDiff += dY;

        if (image == null) return;

        repaint();

        if (isTrialFinished()) {
            Duration totalTrialDuration = Duration.between(
                    Logex.get().getTrialInstant(TrialEvent.getFirst(TrialEvent.FOCUS_ENTER)),
                    Instant.now());
            conLog.debug("Duration = {}", totalTrialDuration);
            // < 90% of the curve traversed inside the focus area => error
            isTrialActive = false;
            hasFocus = false;
            ActionEvent endTrialEvent = (wasTrialAccurate())
                    ? new ActionEvent(this, TrialStatus.HIT, "")
                    : new ActionEvent(this, TrialStatus.ERROR, TrialStatus.TEN_PERCENT_OUT);
            endTrialAction.actionPerformed(endTrialEvent);
        }


        // Check for the end of the trial
//        SwingUtilities.invokeLater(() -> {
//
//        });
    }

    /**
     * Pan with the velocity
     * @param vX X veclocity
     * @param vY Y velocity
     */
    public void pan(double vX, double vY) {
        conLog.info("vX, vY = {}, {}", vX, vY);

        PanTask panTask = new PanTask(vX, vY);
        panner.scheduleAtFixedRate(panTask, 0, 10, TimeUnit.MILLISECONDS);
    }

    public void pan(int dX, int dY) {
        conLog.trace("Pan dX, dY = {}, {}", dX, dY);
        if (abs(dX) < 1000 && abs(dY) < 1000) {
            translate(dX, dY);
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (xDiff == null || yDiff == null) {
            int offsetX;
            int offsetY;
            int offset = focusAreaDim.width / 2 + BORDERS.THICKNESS_2;
            if (rotate >= 0 && rotate < 90) {
                offsetX = offset;
                offsetY = -offset;
            } else if (rotate >= 90 && rotate < 180) {
                offsetX = offset;
                offsetY = offset;
            } else if (rotate >= 180 && rotate < 270) {
                offsetX = -offset;
                offsetY = offset;
            } else {
                offsetX = -offset;
                offsetY = -offset;
            }

            xDiff = getWidth() / 2 - startPosX + offsetX;
            yDiff = getHeight() / 2 - startPosY + offsetY;

            image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        }

        focusArea.setBounds(
                getWidth() / 2 - focusAreaDim.width / 2,
                getHeight() / 2 - focusAreaDim.height / 2,
                focusAreaDim.width,
                focusAreaDim.height);
        focusArea.setVisible(true);

        icon.paintIcon(this, g, xDiff, yDiff);
    }

    // ------------------------------------------------------------------------
    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (isTrialActive && hasFocus) { // Pressed inside
            isPanning = true;

            // Get the start point
            dragPoint = e.getLocationOnScreen();

            // Change the cursor
            getParent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Change back the cursor and the border
        getParent().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        if (!hasFocus) {
            setBorder(BORDERS.BLACK_BORDER);
        }

        isPanning = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        hasFocus = true;
        setBorder(BORDERS.FOCUSED_BORDER);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        hasFocus = false;
        if (!isPanning) setBorder(BORDERS.BLACK_BORDER);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (isTrialActive && isPanning) {
            Point curPoint = e.getLocationOnScreen();
            int xDiff = curPoint.x - dragPoint.x;
            int yDiff = curPoint.y - dragPoint.y;

            dragPoint = curPoint;

            translate(xDiff, yDiff);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

//    @Override
//    public void mooseClicked(Memo mem) {
//        borderBlinker.start();
//    }
//
//    @Override
//    public void mooseScrolled(Memo mem) {
//        if (isTrialActive && hasFocus) {
//            conLog.debug("Translate: {}, {}", mem.getV1Int(), mem.getV2Int());
//            translate(
//                    (int) (mem.getV1Int() * ExpFrame.PAN_MOOSE_GAIN),
//                    (int) (mem.getV2Int() * ExpFrame.PAN_MOOSE_GAIN));
//        }
//    }
//
//    @Override
//    public void mooseWheelMoved(Memo mem) {
//
//    }

//    @Override
//    public void mooseZoomStart(Memo mem) {
//
//    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Is it relate to Moose?
        if (Constants.STRINGS.equals(evt.getPropertyName(), Constants.STRINGS.MOOSE)) {
            if (evt.getNewValue() != null) {
                Memo memo = (Memo) evt.getNewValue();

                switch (memo.getAction()) {
//                    case Constants.STRINGS.GRAB -> {
//                        // Grab if happened inside
//                        if (isCursorInside) {
//                            grab();
//                        }
//                    }

//                    case Constants.STRINGS.REL -> release();

                    case Constants.STRINGS.PAN -> {
                        if (Constants.STRINGS.equals(memo.getMode(), Constants.STRINGS.VEL)) {
                            if (hasFocus) {
                                pan(memo.getV1Float() * cnfgPanGain, memo.getV2Float() * cnfgPanGain);
                            }
                        }

                        if (Constants.STRINGS.equals(memo.getMode(), Constants.STRINGS.STOP)) {
                            panner.shutdownNow();
                        }
                    }

//                    case Constants.STRINGS.ZOOM -> {
//                        zoom(memo.getV1Int());
//                    }
                }
            }
        }

    }

    // Logs ------------------------------------------------------------------------
    private void logInsideFocus() {
        // If hasn't entered before or has exited before
        if (!Logex.get().hasLoggedKey(TrialEvent.FOCUS_ENTER) ||
                Logex.get().hasLoggedKey(TrialEvent.FOCUS_EXIT)) {
            Logex.get().logEvent(TrialEvent.FOCUS_ENTER);

            // Start the stopwatch (if not already started)
            if (!insideFocusStopwatch.isRunning()) insideFocusStopwatch.start();
        }

    }

    private void logOutsideFocus() {
        // If hasn't exited before or has entered before
        if (Logex.get().hasLoggedKey(TrialEvent.FOCUS_ENTER)) {
            Logex.get().logEvent(TrialEvent.FOCUS_EXIT);

            // Start the stopwatch (if not already started)
            if (insideFocusStopwatch.isRunning()) insideFocusStopwatch.stop();
        }
    }

}
