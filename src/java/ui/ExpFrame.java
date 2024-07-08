package ui;

//import enums.TaskType;
//import enums.Technique;
//import moose.Moose;
//import control.Server;
import control.Server;
import enums.TaskType;
import enums.Technique;
import moose.Moose;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static tool.Constants.*;

public class ExpFrame extends JFrame {
    private final TaggedLogger conLog = Logger.tag(getClass().getSimpleName());

    public static String pID = "1000";

    private final String CONFIG_FILE_NAME = "config.properties";
    public static PropertiesConfiguration config;

    // Zoom -----------------------------------------------
    public static int MAX_NOTCHES = 120; // Arbitrary (between Win 44 and Mac 300)
    public static int NOTCHES_IN_ELEMENT = 6;

    // Pan ------------------------------------------------
    public static final int NUM_PAN_BLOCKS = 2;

    public static final double PAN_MOOSE_GAIN = 0.5;

    public static double LR_MARGIN_MM = 20.0;

    // PanZoom ------------------------------------------------
    public static final Color HIGHLIGHT_COLOR = Color.BLUE;
    public static final int ZOOM_OUT_INFO_THRESHOLD = 60; // Zooming out more than this (zlvl < n) => info disappear

    // ------------------------------------------------------------------------------------------
    private Rectangle scrBound;
    private int scrW, scrH;
    private int frW, frH;
    public static int titleBarH;

    private Moose moose;

    private JDialog infoDialog;


    // ------------------------------------------------------------------------------------------

    /**
     * Constructor
     */
    public ExpFrame() {
        setDisplayConfig();
        setBackground(Color.WHITE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                Server.get().shutDown();
            }
        });

        mapKeys();

        loadConfig();
    }

    public void loadConfig() {
        // Load config
        try {
            config = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                    .configure(new Parameters()
                            .properties()
                            .setFileName(CONFIG_FILE_NAME)
                            .setListDelimiterHandler(new DefaultListDelimiterHandler(',')))
                    .getConfiguration();

//            Parameters params = new Parameters();
//            FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
//                    new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
//                            .configure(params.properties()
//                                    .setFileName("config.properties")
//                                    .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
//            config = builder.getConfiguration();
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Start the experiment
     */
    public void begin() {
        moose = new Moose();

        Server.get().setMoose(moose);
        Server.get().start();

        titleBarH = getInsets().top;

        // Show the info frame
        SwingUtilities.invokeLater(() -> {
            // First show the full panel, so the dimensions are correct
            setVisible(true);

            // Create and show the info dialog
            JPanel infoPanel = new InfoPanel(pID, showExperimentAction);

            infoDialog = new JDialog();
            infoDialog.add(infoPanel);
            infoDialog.setSize(680, 440);
            infoDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            infoDialog.addWindowListener(new WindowAdapter() {
                @Override public void windowClosed(WindowEvent e) {
                    System.exit(0);
                }
            });
            // Put the dialog in the center
            infoDialog.setLocation(scrBound.x, scrBound.y);
            int dialogLocX = (scrW - infoDialog.getWidth()) / 2;
            int dialogLocY = (scrH - infoDialog.getHeight()) / 2;
            infoDialog.setLocation(scrBound.x + dialogLocX, scrBound.y + dialogLocY);

            // Show the dialog
            infoDialog.setVisible(true);
        });


    }

    // ------------------------------------------------------------------------------------------
    /**
     * Passed to the InfoPanel (called from there)
     */
    final AbstractAction showExperimentAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            conLog.trace("Info: {}, {}, {}",
                    getValue(STR.PID),
                    getValue(STR.TASK),
                    getValue(STR.TECHNIQUE));

            // Get values from the info dialog
            pID = (String) getValue(STR.PID);
            TaskType taskType = (TaskType) getValue(STR.TASK);
            Technique technique = (Technique) getValue(STR.TECHNIQUE);

            SwingUtilities.invokeLater(() -> {
                // Create the panel based on the chosen taskType
                TaskPanel taskPanel = null;
                switch (taskType) {
                    case ZOOM_OUT, ZOOM_IN, PAN_ZOOM -> {
                        taskPanel = new PanZoomPanel(getContentPane().getSize(), moose, taskType);
                    }

                    case PAN -> {
                        taskPanel = new PanPanel(getContentPane().getSize(), moose, taskType);
                    }

                    case SCROLL -> {
                        taskPanel = new ScrollPanel(getContentPane().getSize(), moose, taskType);
                    }
                }

                // Open the frame with the panel
                add(taskPanel);

                // Close the dialog
                infoDialog.setVisible(false);
                taskPanel.setVisible(true);
                taskPanel.requestFocus();
            });
        }
    };

    // ------------------------------------------------------------------------------------------

    /**
     * Set the config for showing panels
     */
    private void setDisplayConfig() {
        setExtendedState(JFrame.MAXIMIZED_BOTH); // maximized frame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // close on exit

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();

        scrBound = gd[1].getDefaultConfiguration().getBounds();
        scrW = scrBound.width;
        scrH = scrBound.height;

        frW = getSize().width;
        frH = getSize().height;

        // Put at center
        setLocation(
                ((scrW / 2) - (frW / 2)) + scrBound.x,
                ((scrH / 2) - (frH / 2)) + scrBound.y
        );
    }

    private void mapKeys() {

    }


}
