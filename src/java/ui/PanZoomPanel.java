package ui;

import logs.MoLogger;
import enums.TaskType;
import logs.TrialInstants;
import enums.TrialStatus;
import model.PanZoomBlock;
import model.PanZoomTrial;
import moose.Moose;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;
import tool.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static tool.Constants.*;

public class PanZoomPanel
        extends TaskPanel
        implements MouseMotionListener, MouseWheelListener, MouseListener{

    private final TaggedLogger conLog = Logger.tag(getClass().getSimpleName());

    // Constants
    public final double VIEWPPORT_SIZE_mm = 250;
    public final Dimension viewportDim = new Dimension(
            DISP.mmToPxW(VIEWPPORT_SIZE_mm),
            DISP.mmToPxH(VIEWPPORT_SIZE_mm));

    public static final double SCROLL_VP_SIZE_mm = 200;
//    public static final double WHEEL_STEP_SIZE = 0.25;
//    public static final float NOTCH_SCALE = 0.1f; // Based on Windows 10

//    public static final int ZOOM_N_ELEMENTS = 31; // # elements in rows = columns
//    public static final int ELEMENT_NOTCH_RATIO = 3;
    public static final double NOTCH_MM = 1;
    public static int N_ELEMENTS;
//    public static final int N_ELEMENTS = (ExpFrame.TOTAL_N_NOTCHES / ELEMENT_NOTCH_RATIO) * 2 + 1;
    public static final int ZOOM_OUT_ELEMENT_SIZE = 80; // Diameter of the elements (px)
    public static final int ZOOM_IN_ELEMENT_SIZE = 170; // W of the elements (px)
    public static final int ZOOM_IN_ELEMENT_RADIUS = 50; // Corner radius of the elements (px)
    public static final int GUTTER_RATIO = 3;

    public static final int MAX_ZOOM_LEVEL = 1700;

    public static final String ZOOM_OUT_SVG_FILE_NAME = "zoom_out.svg";
    public static final String ZOOM_IN_SVG_FILE_NAME = "zoom_in.svg";

    // Experiment
    private final TaskType taskType;
    private final Moose moose;
    private final boolean startOnLeft;
//    private final int vpSize; // Size of the viewport in px
//    private final int scrollVPSize; // Size of the viewport in px
    private final int lrMargin; // Left-right margin in px (mm comes from ExpFrame)

    // View
    PanZoomView panZoomView;
//    ZoomView zoomView;
//    VTScrollPane scrollPane;
//    private ZoomViewport zoomViewPort;
//    private final ArrayList<MoCoord> zoomElements = new ArrayList<>(); // Hold the grid coords + ids

    // Sound

    // -------------------------------------------------------------------------------------------
    /**
     * Constructor
     * @param dim Dimension – Desired dimension of the panel
     * @param ms Moose – Reference to the Moose
     * @param tsk TaskType – Type of the taskType
     */
    public PanZoomPanel(Dimension dim, Moose ms, TaskType tsk) {
        super(dim);

        setSize(dim);
        setLayout(null);

        startOnLeft = new Random().nextBoolean(); // Randomly choose whether to start traials on the left or right
//        vpSize = Utils.mm2px(VIEWPPORT_SIZE_mm);
//        scrollVPSize = Utils.mm2px(SCROLL_VP_SIZE_mm);
        lrMargin = DISP.mmToPxW(ExpFrame.LR_MARGIN_MM);

        taskType = tsk;
        moose = ms;

        createBlocks();

        // Generate the zooming SVG
        N_ELEMENTS = (ExpFrame.MAX_NOTCHES / ExpFrame.NOTCHES_IN_ELEMENT) * 2 + 1;
//        final int N_ELEMENTS = (ExpFrame.TOTAL_N_NOTCHES / ELEMENT_NOTCH_RATIO) + 1;
        if (taskType.equals(TaskType.ZOOM_IN)) {
            SVGPatterner.genCircleGrid(
                    ZOOM_IN_SVG_FILE_NAME,
                    N_ELEMENTS,
                    ZOOM_IN_ELEMENT_SIZE,
                    0,
                    COLORS.BLUE);
        } else {
            SVGPatterner.genCircleGrid(
                    ZOOM_OUT_SVG_FILE_NAME,
                    N_ELEMENTS,
                    ZOOM_OUT_ELEMENT_SIZE,
                    0,
                    COLORS.FLAX);
        }

        getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0),
                "Space");
        getActionMap().put("Space", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                conLog.info("SPACE Pressed");
                final boolean isSuccess = isTrialSuccess();
                conLog.debug("Result = {}", isSuccess);

                if (isSuccess) {
                    Sounder.playHit();
                    endTrial(TrialStatus.SUCCESS);
                } else {
                    Sounder.playMiss();
                    // TODO Shuffle trial in the block
                    endTrial(TrialStatus.FAIL);
                }

                remove(panZoomView);
//                Resources.SVG.refresh();

//                panZoomView.colorAction.actionPerformed(e);
//                zoomView.colorAction.actionPerformed(e);
            }
        });
    }

    @Override
    protected void loadConfig() {
        //            super.loadConfig();

        List<String> keyValues = new ArrayList<>();

        String key = String.join(".", STR.ZOOM, STR.WHEEL_NOTCH, STR.GAIN);
        final double zoomWheelNotchGain = config.getDouble(key);
        keyValues.add(key + " = " + String.format("%.2f", zoomWheelNotchGain));

        key = String.join(".", STR.PAN, STR.GAIN);
        final double panGain = config.getDouble(key);
        keyValues.add(key + " = " + String.format("%.2f", panGain));

        key = String.join(".", STR.ZOOM, STR.GAIN);
        final double zoomGain = config.getDouble(key);
        keyValues.add(key + " = " + String.format("%.2f", zoomGain));

        key = String.join(".", STR.FLING, STR.VELOCITY, STR.GAIN);
        final double flingVelGain = config.getDouble(key);
        keyValues.add(key + " = " + String.format("%.2f", flingVelGain));

        key = String.join(".", STR.FLING, STR.VELOCITY, STR.FRICTION);
        final double flingVelFriction = config.getDouble(key);
        keyValues.add(key + " = " + String.format("%.2f", flingVelFriction));

        // Set the config in the view
        panZoomView.setPanConfig(panGain);
        panZoomView.setZoomConfig(zoomWheelNotchGain, zoomGain);
        panZoomView.setFlingConfig(flingVelGain, flingVelFriction);

        // Show config in the label
        configLabel.setText(String.join(" | ", keyValues));

    }

    @Override
    protected void loadDesign() {
        String key = String.join(".", STR.PANZOOM, STR.PAN, STR.END, STR.THRESHOLD);
        final double panEndThreshold = design.getDouble(key);

        // Set the design property in the view
        panZoomView.setDesignProperties(panEndThreshold);
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) starTask();
    }

    /**
     * Cerate zoom blocks
     */
    @Override
    protected void createBlocks() {
        super.createBlocks();

        final String key = String.join(".", STR.PANZOOM, STR.NUM, STR.BLOCKS);
        for (int b = 0; b < design.getInt(key); b++) {
            blocks.add(new PanZoomBlock(b + 1));
        }
    }

    /**
     * Start a block
     */
    @Override
    protected void startBlock() {
        super.startBlock();

        activeTrial = activeBlock.getTrial(1); // Get the trial
        showActiveTrial();
    }

    /**
     * Show the active trial
     */
    @Override
    protected void showActiveTrial() {
        requestFocusInWindow();
        conLog.info("----------------------------------");
        final Component[] comps = getComponentsInLayer(JLayeredPane.PALETTE_LAYER);
        for (Component c : comps) {
            remove(c);
        }

        panZoomView = new PanZoomView((PanZoomTrial) activeTrial);
        panZoomView.setBounds(
                (getWidth() - viewportDim.width) / 2, (getHeight() - viewportDim.height)/2,
                viewportDim.width, viewportDim.height);


        SwingUtilities.invokeLater(() -> {
            add(panZoomView, JLayeredPane.PALETTE_LAYER);
            panZoomView.setVisible(true);

        });


        // Update prgogressLabel (trial/block)
        progressLabel.setText("Trial: " + activeTrial.trialNum + " – " + "Block: " + activeTrial.blockNum);
        progressLabel.setVisible(true);

        // Load config & design
        loadConfig();
        loadDesign();

        // Log
//        MoLogger.get().activateTrial(activeTrial);
//        MoLogger.get().logEvent(TrialInstants.TRIAL_OPEN);
    }

    @Override
    protected boolean isTrialSuccess() {
        // Check the circles inside the square
        return panZoomView.isSuccess();
    }

    // Actions -----------------------------------------------------------------------------------
    private final AbstractAction endTrialAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
//            MoLogger.get().logEvent(TrialInstants.SPACE_PRESS);
//            double enterToSpace = MoLogger.get().getDurationSec(
//                    MoLogger.last(TrialInstants.VIEWPORT_ENTER),
//                    TrialInstants.SPACE_PRESS);
//            double firstZoomToSpace = MoLogger.get().getDurationSec(
//                    MoLogger.first(TrialInstants.ZOOM),
//                    TrialInstants.SPACE_PRESS);
//            double enterToLastZoom = MoLogger.get().getDurationSec(
//                    MoLogger.last(TrialInstants.VIEWPORT_ENTER),
//                    MoLogger.last(TrialInstants.ZOOM));
//            double firstZoomToLastZoom = MoLogger.get().getFirstToLastDurSec(TrialInstants.ZOOM);
//
//            conLog.debug("En->lZ = {}, fZ->lZ = {}", enterToLastZoom, firstZoomToLastZoom);

            endTrial(TrialStatus.FAIL);
        }
    };

    // Mouse -------------------------------------------------------------------------------------
    @Override
    public void mouseClicked(MouseEvent e) {
        // TODO Show an error (ptc. shouldn't click)
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Show an error (ptc. shouldn't press)
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Press errro should cover this
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Log movement AFTER trial has been opened
//        if (MoLogger.get().hasLogged(TrialInstants.TRIAL_OPEN)) MoLogger.get().logEvent(TrialInstants.MOVE);
        final Point mp = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(mp, this);
        final Point p = e.getPoint();
//        conLog.info("Point: {}", p);
//        conLog.info("Bounds: {}", panZoomView.getBounds());
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        // TODO Show error if not over the ViewPort
        conLog.info("Wheeling...");
    }
}
