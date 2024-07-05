package ui;

import control.Logex;
import enums.TrialEvent;
import enums.TrialStatus;
import model.Block;
import model.Trial;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;
import tool.MoDimension;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import static tool.Constants.*;

/***
 * JlayeredPane to use indexes for objects
 */
public class TaskPanel extends JLayeredPane {
    private final TaggedLogger conLog = Logger.tag(getClass().getSimpleName());

    // Experiment
    protected ArrayList<Block> blocks = new ArrayList<>();
    protected Block activeBlock;
    protected Trial activeTrial;

    // Config
    private final String CONFIG_FILE_NAME = "config.properties";
    public PropertiesConfiguration config;

    // UI
    JLabel progressLabel = new JLabel();
    JLabel endTaskLabel = new JLabel();
    JButton configButton = new JButton();
    JLabel configLabel = new JLabel();

    // Consts
    final Dimension PRG_LBL_DIM = new MoDimension(200, 30);
    final int PRG_LBL_RIGHT = 10; // Progress label from the right side
    final int PRG_LBL_TOP = 15;
    final int PRG_LBL_FONT_SIZE = 20;

    public TaskPanel(Dimension dim) {
        conLog.info("Width: {}", DISP.mmToPxH(10));

        // Set up progress label
        progressLabel.setBounds(
                dim.width - DISP.mmToPxW(PRG_LBL_RIGHT) - PRG_LBL_DIM.width,
                DISP.mmToPxH(PRG_LBL_TOP),
                PRG_LBL_DIM.width, PRG_LBL_DIM.height);
        progressLabel.setFont(new Font(progressLabel.getFont().getFontName(), Font.PLAIN, PRG_LBL_FONT_SIZE));
        progressLabel.setVerticalAlignment(JLabel.CENTER);
        progressLabel.setHorizontalAlignment(JLabel.CENTER);
//        progressLabel.setVisible(true);


        // Set up the config button
        configButton.setText("Load Config");
        configButton.setBounds(
                DISP.mmToPxW(20),
                dim.height - DISP.mmToPxH(20) - 50,
                200, 50);

        // Set up the config label
        configLabel.setText("Press the button to load config");
        configLabel.setBounds(
                configButton.getX(),
                configButton.getY() + 50,
                2000, 50);

        // Load config at the beginning
//        try {
//            loadConfig();
//        } catch (ConfigurationException e) {
//            configLabel.setText("Problem in loading config");
//            throw new RuntimeException(e);
//        }

    }

    protected void loadConfig() throws ConfigurationException {
        config = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                .configure(new Parameters()
                        .properties()
                        .setFileName(CONFIG_FILE_NAME)
                        .setListDelimiterHandler(new DefaultListDelimiterHandler(',')))
                .getConfiguration();

//        try {
//
//        } catch (ConfigurationException ex) {
//            throw new RuntimeException(ex);
//        }
    }

    protected void createBlocks() {
        // Implemented by the subclasses
    }

    /**
     * Start the current block
     */
    protected void starTask() {
        activeBlock = blocks.getFirst();
        startBlock();
    }

    /**
     * Show a trial
     */
    protected void startBlock() {
        activeTrial = activeBlock.getTrial(1);

        for (Component c : getComponentsInLayer(JLayeredPane.DEFAULT_LAYER)) {
            remove(c);
            repaint();
        }

        add(progressLabel, JLayeredPane.DEFAULT_LAYER);
        add(configButton, JLayeredPane.DEFAULT_LAYER);
        add(configLabel, JLayeredPane.DEFAULT_LAYER);

        // Implemented by the subclasses...
    }

    protected boolean wasTrialSeccess() {
        return false;
    }

    protected void endTrial(int status) {
        Logex.get().logEvent(TrialEvent.TRIAL_CLOSE);
        double openToClose = Logex.get().getDurationSec(TrialEvent.TRIAL_OPEN, TrialEvent.TRIAL_CLOSE);
        conLog.debug("Time: Open to Close = {}", openToClose);
        conLog.debug("--------------------------");
        if (status == TrialStatus.HIT) {
            if (activeBlock.isBlockFinished(activeTrial.trialNum)) { // Block finished -> show break|end
                conLog.info("Block Finished");
                endBlock(); // Got to the next block (checks are done inside that method)
            } else { // More trials in the block
                activeTrial = activeBlock.getTrial(activeTrial.trialNum + 1);
                showActiveTrial();
            }
        } else { // Error
            // Re-insert the trial into the rest randomly, then get the next one
            activeBlock.reInsertTrial(activeTrial.trialNum);
            activeTrial = activeBlock.getTrial(activeTrial.trialNum + 1);
            showActiveTrial();
        }
    }

    protected void endBlock() {
        if (blocks.size() == activeBlock.blockNum) { // No more blocks
            conLog.info("Task Ended!");
            endTask();
        } else { // More blocks -> show break -> (action) next block
            showBreak();
        }
    }

    protected void showActiveTrial() {

    }

    protected void showBreak() {
        BreakPanel breakPanel = new BreakPanel(getSize(), onEndBreakAction);
        removeAll();
        add(breakPanel, DEFAULT_LAYER);
        repaint();
    }

    protected void endTask() {
        endTaskLabel.setText("Task finished. Thank You!");
        endTaskLabel.setFont(new Font("Roboto", Font.BOLD, 50));
        endTaskLabel.setForeground(COLORS.BLACK);
        endTaskLabel.setSize(700, 50);
        endTaskLabel.setHorizontalAlignment(SwingConstants.CENTER);
        endTaskLabel.setVerticalAlignment(SwingConstants.CENTER);
        int centX = (getWidth() - endTaskLabel.getWidth()) / 2;
        int centY = (getHeight() - endTaskLabel.getHeight()) / 2;
        endTaskLabel.setBounds(centX, centY, endTaskLabel.getWidth(), endTaskLabel.getWidth());

        removeAll();
        add(endTaskLabel, DEFAULT_LAYER);
        repaint();
    }

    private final AbstractAction onEndBreakAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            conLog.trace("Break Ended");
            activeBlock = blocks.get(activeBlock.blockNum);
            startBlock();
        }
    };
}
