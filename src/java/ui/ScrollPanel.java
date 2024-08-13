package ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import enums.Direction;
import enums.TaskType;
import enums.TrialStatus;
import model.Config;
import model.ScrollBlock;
import model.ScrollTrial;
import moose.Moose;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;
import tool.Constants;
import tool.Sounder;
import tool.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static tool.Constants.*;

public class ScrollPanel extends TaskPanel{
    private final TaggedLogger conLog = Logger.tag(STR.CONSOLE);

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
    private Point lastViewPosition = new Point();

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

        final KeyStroke spaceKey = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
        getInputMap().put(spaceKey, spaceKey.toString());
        getActionMap().put(spaceKey.toString(), SPACE_ACTION);

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
            ObjectMapper objectMapper = new ObjectMapper();
            Config scrollConfig = objectMapper.readValue(
                    new File("scroll-config.json"),
                    Config.class);

            // Set the config in scroll pane
            scrollPane.setConfig(scrollConfig);

            // Display values
            configLabel.setText(scrollConfig.toString());
        } catch (IOException e) {
            conLog.error("Could not read config json file!");
            throw new RuntimeException(e);
        }

//        List<String> keyValues = new ArrayList<>();
//
//        final String flingVelGainKey = String.join(".",
//                STR.FLING, STR.VELOCITY, STR.GAIN);
//        final double flingVelGain = config.getDouble(flingVelGainKey);
//        keyValues.add(flingVelGainKey + " = " + String.format("%.2f", flingVelGain));
//
//        final String flingVelFrictionKey = String.join(".",
//                STR.FLING, STR.VELOCITY, STR.FRICTION);
//        final double flingVelFriction = config.getDouble(flingVelFrictionKey);
//        keyValues.add(flingVelFrictionKey + " = " + String.format("%.2f", flingVelFriction));
//
//        final String flingMinVelocityKey = String.join(".",
//                STR.FLING, STR.MIN, STR.VELOCITY);
//        final double flingMinVelocity = config.getDouble(flingMinVelocityKey);
//        keyValues.add(flingMinVelocityKey + " = " + String.format("%.2f", flingMinVelocity));
//
//        // Set in the scrollPane
//        scrollPane.setConfig(flingVelGain, flingVelFriction, flingMinVelocity);
//
//        // Show config in the label
//        configLabel.setText(String.join(" | ", keyValues));

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
        final int numBlocks = expDesign.numScrollBlocks;
        final Direction[] directions = new Direction[]{Direction.N, Direction.S};
        final int[] distances = expDesign.scrollDistances;
        final int[] tolerances = expDesign.scrollTolerances;

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
        conLog.info("Trial: {}", activeTrial.toString());
        // Set up and add the scrollPane
        scrollPane = new VTScrollPane()
                .setText("lorem", DISP.mmToPxW(TEXT_W_mm), true)
                .setScrollBar(SCROLL_BAR_W_mm, SCROLL_THUMB_H_mm)
                .create();
//        Dimension viewDim = scrollPane.getPreferredSize();
        lastViewPosition = getRandPosition(viewportDim);
//        scrollPane.setBounds(
//                (getWidth() - viewportDim.width) / 2, (getHeight() - viewportDim.height) / 2,
//                viewportDim.width, viewportDim.height);
        scrollPane.setBounds(lastViewPosition.x, lastViewPosition.y, viewportDim.width, viewportDim.height);
        scrollPane.setWheelEnabled(true);
        scrollPane.setTrial(trial);
        scrollPane.setVisible(true);

        add(scrollPane, DEFAULT_LAYER);

        // Set up and add the indicator
        final int lineH = scrollPane.getLineHeight();

        indicator.setSize(new Dimension(
                DISP.mmToPxW(expDesign.scrollIndicWidth),
                trial.tolerance * lineH));
        indicator.setLocation(
                scrollPane.getX() - indicator.getWidth(),
                scrollPane.getY() + ((scrollPane.getNLinesInside() - trial.tolerance) / 2) * lineH);
        indicator.setBackground(COLORS.BLUE);

        add(indicator, PALETTE_LAYER);

        // Show progress info
        progressLabel.setText(String.format("Block %d – Trial %d", activeBlock.blockNum, activeTrial.trialNum));

        // Load config
        loadConfig();
    }

    /**
     * Generate a random position for a pane
     * Based on the size and dimensions of the displace area
     * @param paneDim Dimension of the pane
     * @return A random position
     */
    private Point getRandPosition(Dimension paneDim) {
        final int lrMargin = DISP.mmToPxW(DISP.LR_MARGIN_mm);

        final int minX = lrMargin;
        final int maxX = getWidth() - (lrMargin + paneDim.width);

        final int midY = (getHeight() - paneDim.height) / 2;

        if (minX >= maxX) return new Point(); // Invalid dimensions
        else {
            int randX = 0;
            do {
                randX = Utils.randInt(minX, maxX);
            } while (Math.abs(randX - lastViewPosition.x) <= paneDim.width); // New position shuold be further than W

            return new Point(randX, midY);
        }
    }

    @Override
    protected boolean isTrialSuccess() {
        return scrollPane.isTargetWithinIndicator();
    }

    //---------------------------------------------------------------------------
    // Actions
    private final AbstractAction SPACE_ACTION = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            conLog.info("SPACE Pressed");
            final boolean isSuccess = isTrialSuccess();
            conLog.debug("Result = {}", isSuccess);

            remove(scrollPane);
            repaint();
            if (isSuccess) {
                Sounder.playHit();
                endTrial(TrialStatus.SUCCESS);
            } else {
                Sounder.playMiss();
                // TODO Shuffle trial in the block
                endTrial(TrialStatus.FAIL);
            }

        }
    };

}
