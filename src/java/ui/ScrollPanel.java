package ui;

import enums.Direction;
import enums.TaskType;
import model.ScrollBlock;
import model.ScrollTrial;
import moose.Moose;
import org.apache.commons.configuration2.ex.ConfigurationException;
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
        try {
            super.loadConfig();

            List<String> keyValues = new ArrayList<>();

            final String flingVelGainKey = String.join(".",
                    STRINGS.FLING, STRINGS.VELOCITY, STRINGS.GAIN);
            final double flingVelGain = config.getDouble(flingVelGainKey);
            keyValues.add(flingVelGainKey + " = " + String.format("%.2f", flingVelGain));

            final String flingVelFrictionKey = String.join(".",
                    STRINGS.FLING, STRINGS.VELOCITY, STRINGS.FRICTION);
            final double flingVelFriction = config.getDouble(flingVelFrictionKey);
            keyValues.add(flingVelFrictionKey + " = " + String.format("%.2f", flingVelFriction));

            final String flingMinVelocityKey = String.join(".",
                    STRINGS.FLING, STRINGS.MIN, STRINGS.VELOCITY);
            final double flingMinVelocity = config.getDouble(flingMinVelocityKey);
            keyValues.add(flingMinVelocityKey + " = " + String.format("%.2f", flingMinVelocity));

            // Set in the scrollPane
            scrollPane.setConfig(flingVelGain, flingVelFriction, flingMinVelocity);

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

    @Override
    protected void createBlocks() {
        super.createBlocks();

        // Extract the design factor values
        final String prefix = "scroll.";
        final int numBlocks = expDesign.getInt(prefix + STRINGS.NUM_BLOCKS);
        final List<Direction> directions = expDesign.getList(Direction.class, prefix + STRINGS.DIRECTIONS);
        final List<Integer> distances = expDesign.getList(Integer.class, prefix + STRINGS.DISTANCES);
        final List<Integer> indicSizes = expDesign.getList(Integer.class, prefix + STRINGS.INDICATOR_SIZES);

        // Create blocks
        for (int b = 1; b <= numBlocks; b++) {
            blocks.add(new ScrollBlock(b, directions, distances, indicSizes));
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

        scrollPane = new VTScrollPane()
                .setText("lorem", DISP.mmToPxW(TEXT_W_mm), true)
                .setScrollBar(SCROLL_BAR_W_mm, SCROLL_THUMB_H_mm)
                .create();
        scrollPane.setBounds(
                (getWidth() - viewportDim.width) / 2, (getHeight() - viewportDim.height) / 2,
                viewportDim.width, viewportDim.height);
//        scrollPane.setLocation((getWidth() - SCROLL_VP_SIZE)/2, (getHeight() - SCROLL_VP_SIZE)/2);
        scrollPane.setWheelEnabled(true);
        scrollPane.setVisible(true);

        add(scrollPane, DEFAULT_LAYER);

        // Show progress info
        progressLabel.setText(String.format("Block %d – Trial %d", activeBlock.blockNum, activeTrial.trialNum));

        // Load config
        loadConfig();
    }

}
