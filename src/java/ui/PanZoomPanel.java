package ui;

import control.Logex;
import enums.TaskType;
import enums.TrialEvent;
import enums.TrialStatus;
import model.PanZoomTrial;
import moose.Moose;
import org.apache.commons.configuration2.ex.ConfigurationException;
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
    private final ArrayList<MoCoord> zoomElements = new ArrayList<>(); // Hold the grid coords + ids

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


        // Add the elements to the list (done once)
//        for (int r = 0; r < N_ELEMENTS; r++) {
//            for (int c = 0; c < N_ELEMENTS; c++) {
//                zoomElements.add(new MoCoord(r, c, String.format("r%d_c%d", r, c)));
//            }
//        }

//        addMouseListener(this);
//        addMouseMotionListener(this);
//        addMouseWheelListener(this);
//        moose.addMooseListener(this);

        getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0),
                "Space");
        getActionMap().put("Space", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                conLog.info("SPACE Pressed");
                final boolean result = wasTrialSeccess();
                conLog.debug("Result = {}", result);
                remove(panZoomView);
//                Resources.SVG.refresh();
                endTrial(TrialStatus.HIT);
//                panZoomView.colorAction.actionPerformed(e);
//                zoomView.colorAction.actionPerformed(e);
            }
        });
    }

    @Override
    protected void loadConfig() {
        try {
            super.loadConfig();

            List<String> keyValues = new ArrayList<>();

            final String zoomNotchGainKey = String.join(".", STRINGS.WHEEL_NOTCH, STRINGS.GAIN);
            final double zoomWheelNotchGain = config.getDouble(zoomNotchGainKey);
            keyValues.add(zoomNotchGainKey + " = " + String.format("%.2f", zoomWheelNotchGain));

            final String panGainKey = String.join(".",STRINGS.PAN, STRINGS.GAIN);
            final double panGain = config.getDouble(panGainKey);
            keyValues.add(panGainKey + " = " + String.format("%.2f", panGain));

            final String panFrictionKey = String.join(".",STRINGS.PAN, STRINGS.FRICTION);
            final double panFriction = config.getDouble(panFrictionKey);
            keyValues.add(panFrictionKey + " = " + String.format("%.2f", panFriction));

            // Set in the scrollPane
            panZoomView.setConfig(zoomWheelNotchGain, panGain, panFriction);

            // Show config in the label
            configLabel.setText(String.join(" | ", keyValues));

        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);

        if (aFlag) {
            // Begin
            starTask();
        }
    }

    /**
     * Cerate zoom blocks
     */
    @Override
    protected void createBlocks() {
        super.createBlocks();

//        for (int i = 0; i < NUM_ZOOM_BLOCKS; i++) {
//            blocks.add(new Block(i + 1, taskType, 1));
//        }
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

        // Update prgogressLabel (trial/block)
//        progressLabel.setText("Trial: " + activeTrial.trialNum + " – " + "Block: " + activeTrial.blockNum);
//        progressLabel.setVisible(true);

        // Create the viewport for showing the trial
        switch (taskType) {
            case PAN_ZOOM -> {
                panZoomView = new PanZoomView((PanZoomTrial) activeTrial);
//                panZoomView.setBounds(
//                        (getWidth() - vpSize)/2, (getHeight() - vpSize)/2,
//                        vpSize, vpSize);
//                panZoomView.setBounds(0, 0, getWidth(), getHeight());
                panZoomView.setBounds(
                        (getWidth() - viewportDim.width) / 2, (getHeight() - viewportDim.height)/2,
                        viewportDim.width, viewportDim.height);
//                panZoomView.setDim(VIEWPORT_SIZE);
//                panZoomView.setPos(new MoPoint( (getWidth() - VIEWPORT_SIZE)/2, (getHeight() - VIEWPORT_SIZE)/2));
                panZoomView.setVisible(true);
                add(panZoomView, JLayeredPane.PALETTE_LAYER);
            }

//            case ZOOM_IN, ZOOM_OUT -> {
//                zoomView = new ZoomView(taskType);
//                zoomView.setBounds(
//                        (getWidth() - vpSize)/2, (getHeight() - vpSize)/2,
//                        vpSize, vpSize);
//                zoomView.setVisible(true);
//                zoomView.setBorder(Constants.BORDERS.BLACK_BORDER);
//                add(zoomView, JLayeredPane.PALETTE_LAYER);
//            }

//            case SCROLL -> {
//                scrollPane = new VTScrollPane(new MoDimension(scrollVPSize))
//                        .setText("lorem")
//                        .setScrollBar(VT_SCROLL_BAR_W_mm, VT_SCROLL_THUMB_H_mm)
//                        .create();
//                scrollPane.setBounds(
//                        (getWidth() - scrollVPSize)/2, (getHeight() - scrollVPSize)/2,
//                        scrollVPSize, scrollVPSize);
//                scrollPane.setLocation((getWidth() - scrollVPSize)/2, (getHeight() - scrollVPSize)/2);
//                scrollPane.setWheelEnabled(true);
//                scrollPane.setVisible(true);
//                add(scrollPane, JLayeredPane.PALETTE_LAYER);
//            }
        }

        // Log
        Logex.get().activateTrial(activeTrial);
        Logex.get().logEvent(TrialEvent.TRIAL_OPEN);
    }

    @Override
    protected boolean wasTrialSeccess() {
        // Check the circles inside the square
        return panZoomView.isViewAlignedToDest();
    }

    // Actions -----------------------------------------------------------------------------------
    private final AbstractAction endTrialAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Logex.get().logEvent(TrialEvent.SPACE_PRESS);
            double enterToSpace = Logex.get().getDurationSec(
                    TrialEvent.getLast(TrialEvent.VIEWPORT_ENTER),
                    TrialEvent.SPACE_PRESS);
            double firstZoomToSpace = Logex.get().getDurationSec(
                    TrialEvent.getFirst(TrialEvent.ZOOM),
                    TrialEvent.SPACE_PRESS);
            double enterToLastZoom = Logex.get().getDurationSec(
                    TrialEvent.getLast(TrialEvent.VIEWPORT_ENTER),
                    TrialEvent.getLast(TrialEvent.ZOOM));
            double firstZoomToLastZoom = Logex.get().getDurationSec(
                    TrialEvent.getFirst(TrialEvent.ZOOM),
                    TrialEvent.getLast(TrialEvent.ZOOM));

            conLog.debug("En->lZ = {}, fZ->lZ = {}", enterToLastZoom, firstZoomToLastZoom);

            endTrial(TrialStatus.HIT);
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
//        if (Logex.get().hasLogged(TrialEvent.TRIAL_OPEN)) Logex.get().logEvent(TrialEvent.MOVE);
        final Point mp = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(mp, this);
        final Point p = e.getPoint();
//        conLog.info("Point: {}", p);
//        conLog.info("Bounds: {}", panZoomView.getBounds());
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        // TODO Show error if not over the ViewPort
    }

    // Moose --------------------------------------------------------------------------------------
//    @Override
//    public void mooseClicked(Memo mem) {
//        Logex.get().logError(ErrorEvent.CLICK, ErrorEvent.OUTSIDE_ZVP);
//        // TODO Show error (no Moose clicking)
//    }
//
//    @Override
//    public void mooseScrolled(Memo mem) {
//
//    }
//
//    @Override
//    public void mooseWheelMoved(Memo mem) {
//        Logex.get().logError(ErrorEvent.CLICK, ErrorEvent.OUTSIDE_ZVP);
//        // TODO Show error if not over ViewPort
//    }
//
//    @Override
//    public void moosePanned(Memo mem) {
//
//    }
//
//    @Override
//    public void mooseGrabbed(Memo mem) {
//
//    }
//
//    @Override
//    public void mooseReleased(Memo mem) {
//
//    }
}
