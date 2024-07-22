package ui;

import enums.Direction;
import enums.TaskType;
import model.ScrollBlock;
import model.ScrollTrial;
import moose.Moose;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;
import tool.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import static tool.Constants.*;

public class ScrollPanel extends TaskPanel{
    private final TaggedLogger conLog = Logger.tag(getClass().getSimpleName());

    // Constants
    private final double VIEWPORT_SIZE_mm = 200;
    public final Dimension viewportDim = new Dimension(
            Constants.DISP.mmToPxW(VIEWPORT_SIZE_mm),
            Constants.DISP.mmToPxH(VIEWPORT_SIZE_mm));
//    private final int SCROLL_VP_SIZE = Utils.mm2px(SCROLL_VP_SIZE_mm);

    private final double SCROLL_BAR_W_mm = 5.0;
    private final double SCROLL_THUMB_H_mm = 6.0;
    private final double TEXT_W_mm = VIEWPORT_SIZE_mm - SCROLL_BAR_W_mm;

    // View
    private VTScrollPane scrollPane;
    private JPanel indicator = new JPanel();

    // Config
    private String configStr;

    // Experiment
    private final TaskType taskType;
//    private ScrollTrial trial;
    private final Moose moose;

    public ScrollPanel(Dimension dim, Moose ms, TaskType tsk) {
        super(dim);

        setSize(dim);
        setLayout(null);

        getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0),
                "Space");
        getActionMap().put("Space", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        taskType = tsk;
        moose = ms;

        configButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadConfig();
            }
        });

        createBlocks();

    }

    @Override
    protected void loadConfig() {
        super.loadConfig();

        List<String> keyValues = new ArrayList<>();

        final String flingVelGainKey = String.join(".",
                STR.FLING, STR.VELOCITY, STR.GAIN);
        final double flingVelGain = config.getDouble(flingVelGainKey);
        keyValues.add(flingVelGainKey + " = " + String.format("%.2f", flingVelGain));

        final String flingVelFrictionKey = String.join(".",
                STR.FLING, STR.VELOCITY, STR.FRICTION);
        final double flingVelFriction = config.getDouble(flingVelFrictionKey);
        keyValues.add(flingVelFrictionKey + " = " + String.format("%.2f", flingVelFriction));

        final String flingMinVelocityKey = String.join(".",
                STR.FLING, STR.MIN, STR.VELOCITY);
        final double flingMinVelocity = config.getDouble(flingMinVelocityKey);
        keyValues.add(flingMinVelocityKey + " = " + String.format("%.2f", flingMinVelocity));

        // Set in the scrollPane
        scrollPane.setConfig(flingVelGain, flingVelFriction, flingMinVelocity);

        // Show config in the label
        configLabel.setText(String.join(" | ", keyValues));

    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);

        if (aFlag) starTask();
    }

    @Override
    protected void createBlocks() {
        super.createBlocks();

        // Extract the design factor values
        final String prefix = "scroll";
        final int numBlocks = design.getInt(
                String.join(".", prefix, STR.NUM, STR.BLOCKS));
        final List<Direction> directions = design.getList(Direction.class,
                String.join(".", prefix, STR.DIRECTIONS));
        final List<Integer> distances = design.getList(Integer.class,
                String.join(".", prefix, STR.DISTANCES));
        final List<Integer> tolerances = design.getList(Integer.class,
                String.join(".", prefix, STR.TOLERANCES));

        // Create blocks
        for (int b = 1; b <= numBlocks; b++) {
            blocks.add(new ScrollBlock(b, directions, distances, tolerances));
        }

        conLog.info("Blocks: {}", blocks);
    }

    /**
     * Start a block
     */
    @Override
    protected void startBlock() {
        super.startBlock();
        showActiveTrial();
    }

    @Override
    protected void showActiveTrial() {
        final ScrollTrial trial = (ScrollTrial) activeTrial;

        // Set up and add the scrollPane
        scrollPane = new VTScrollPane()
                .setText("lorem", DISP.mmToPxW(TEXT_W_mm), true)
                .setScrollBar(SCROLL_BAR_W_mm, SCROLL_THUMB_H_mm)
                .create();
        scrollPane.setBounds(
                (getWidth() - viewportDim.width) / 2, (getHeight() - viewportDim.height) / 2,
                viewportDim.width, viewportDim.height);
        scrollPane.setWheelEnabled(true);
        scrollPane.setTrial(trial);
        scrollPane.setVisible(true);

        add(scrollPane, DEFAULT_LAYER);

        // Set up and add the indicator
        final int lineH = scrollPane.getLineHeight();
        final double indicWidthMM = config.getDouble(
                String.join(".", STR.SCROLL, STR.INDICATOR, STR.WIDTH));

        indicator.setSize(new Dimension(
                DISP.mmToPxW(indicWidthMM),
                trial.tolerance * lineH));
        indicator.setLocation(
                scrollPane.getX() - indicator.getWidth(),
                scrollPane.getY() + ((scrollPane.getNLinesInside() - trial.tolerance) / 2) * lineH);
        indicator.setBackground(COLORS.BLUE);

        add(indicator, DEFAULT_LAYER);

        // Show progress info
        progressLabel.setText(String.format("Block %d – Trial %d", activeBlock.blockNum, activeTrial.trialNum));

        // Load config
        loadConfig();
    }

}
