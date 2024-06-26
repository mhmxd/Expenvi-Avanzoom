package ui;

import enums.Task;
import model.BaseBlock;
import moose.Moose;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;
import tool.MoDimension;
import tool.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import static ui.ExpFrame.NUM_ZOOM_BLOCKS;
import static ui.ExpFrame.NUM_ZOOM_REPS;

public class ScrollPanel extends TaskPanel{
    private final TaggedLogger conLog = Logger.tag(getClass().getSimpleName());

    // Constants
    private final double SCROLL_VP_SIZE_mm = 200;
    private final int SCROLL_VP_SIZE = Utils.mm2px(SCROLL_VP_SIZE_mm);

    private final double VT_SCROLL_BAR_W_mm = 5.0;
    private final double VT_SCROLL_THUMB_H_mm = 6.0;

    // View
    private VTScrollPane scrollPane;

    // Experiment
    private final Task task;
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

        createBlocks();

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
            blocks.add(new BaseBlock(i + 1, task, NUM_ZOOM_REPS));
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
        scrollPane = new VTScrollPane(new MoDimension(SCROLL_VP_SIZE))
                .setText("lorem")
                .setScrollBar(VT_SCROLL_BAR_W_mm, VT_SCROLL_THUMB_H_mm)
                .create();
        scrollPane.setBounds(
                (getWidth() - SCROLL_VP_SIZE)/2, (getHeight() - SCROLL_VP_SIZE)/2,
                SCROLL_VP_SIZE, SCROLL_VP_SIZE);
        scrollPane.setLocation((getWidth() - SCROLL_VP_SIZE)/2, (getHeight() - SCROLL_VP_SIZE)/2);
        scrollPane.setWheelEnabled(true);
        scrollPane.setVisible(true);

        add(scrollPane, JLayeredPane.PALETTE_LAYER);
    }
}
