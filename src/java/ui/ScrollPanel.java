package ui;

import enums.Task;
import model.Block;
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

import static ui.ExpFrame.NUM_ZOOM_BLOCKS;
import static ui.ExpFrame.NUM_ZOOM_REPS;

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
    private final Task task;
//    private ScrollTrial trial;
    private final Moose moose;

    public ScrollPanel(Dimension dim, Moose ms, Task tsk) {
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

        task = tsk;
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

            final double velGain = config.getDouble(STRINGS.VELOCITY_GAIN);
            final double velFriction = config.getDouble(STRINGS.VELOCITY_FRICTION);
            final double minFlingVal = config.getDouble(STRINGS.MIN_FLING_VELOCITY);

            // Set in the scrollPane
            scrollPane.setConfig(velGain, velFriction, minFlingVal);

            // Show config
            configLabel.setText(String.format("V Gain = %.2f | V Friction = %.2f | Min Fling V = %.2f",
                    velGain, velFriction, minFlingVal));

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

        for (int i = 0; i < NUM_ZOOM_BLOCKS; i++) {
            blocks.add(new Block(i + 1, task, NUM_ZOOM_REPS));
        }
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
