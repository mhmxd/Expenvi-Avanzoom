package logs;

import enums.ErrorEvent;
import model.Trial;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;
import tool.Utils;
import ui.ExpFrame;

import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static tool.Constants.STR;

public class MoLogger {
    private final TaggedLogger conLog = Logger.tag(getClass().getSimpleName());
    private static MoLogger self;

    private Trial activeTrial;

//    private final List<TrialInstants> eventLog = new ArrayList<>();
//    private final List<ErrorEvent> errorLog = new ArrayList<>();
//    private final Map<String, Instant> eventMap = new HashMap<>(); // Keys: TrialInstants strings
//    private final Map<String, ErrorEvent> errorMap = new HashMap<>(); // Keys: ErrorEvent strings

    // Different log files
    private final String TOP_LOG_FOLDER = "/Users/mahmoud/Documents/Academics/PhD/MIDE/– ZoomPan Experiment";
    private final String LOG_FOLDER_NAME = "Logs";

    private final Path logFolder;
    private Path ptcLogFolder;
    private String ptcDateStr;

    private File eventsLogFile;
    private File instantsLogFile;
    private File trialLogFile;

    private PrintWriter eventsLogFilePW;
    private PrintWriter instantsLogFilePW;
    private PrintWriter trialLogFilePW;

    private PanZoomInstants panZoomInstants;

    /**
     * Constructor
     */
    private MoLogger() {
        // Create folders
        final Path parentPath = Paths.get(TOP_LOG_FOLDER);
        logFolder = parentPath.resolve(LOG_FOLDER_NAME);
        createFolderIfNotExisted(logFolder); // Create if the folder doesn't exist

        // Create a folder for the participant (if not already created)
        ptcLogFolder = logFolder.resolve(String.valueOf(ExpFrame.pID));
        createFolderIfNotExisted(ptcLogFolder);

        openFilesToWrite();
    }

    /**
     * Get singleton
     * @return MoLogger instance
     */
    public static MoLogger get() {
        if (self == null) self = new MoLogger();
        return self;
    }

    private void openFilesToWrite() {
        ptcDateStr = ExpFrame.pID + "_" + Utils.nowDate();

        // Log files for the participant
//        eventsLogFile = ptcLogFolder.resolve(ptcDateStr + "_" + "EVENTS.csv").toFile();
        instantsLogFile = ptcLogFolder.resolve(ptcDateStr + "_" + "INSTANTS.csv").toFile();
        trialLogFile = ptcLogFolder.resolve(ptcDateStr + "_" + "TRIALS.csv").toFile();

        // Create files (w/ autoflash) and if not existed, write headers. Append is for later writings
        try {
//            eventsLogFilePW = new PrintWriter(
//                    new FileOutputStream(eventsLogFile, true),
//                    true);

            instantsLogFilePW = new PrintWriter(
                    new FileOutputStream(instantsLogFile, true),
                    true);

            trialLogFilePW = new PrintWriter(
                    new FileOutputStream(trialLogFile, true),
                    true);

            //--- Write headers (only the first time)
//            String trialInfoHeader = getHeader(TrialInfo.class);

//            if (isFileEmpty(eventsLogFile)) {
//                trialLogFilePW.println(trialShowInfoHeader + SP + getEnumHeader(TrialLog.class));
//            }
//
//            if (isFileEmpty(instantsLogFile)) {
//                instantLogFilePW.println(trialShowInfoHeader + SP + getEnumHeader(TrialInstant.class));
//            }
//
//            if (isFileEmpty(durationLogFile)) {
//                durationLogFilePW.println(trialShowInfoHeader + SP + getEnumHeader(DurationLog.class));
//            }
//
//            // Motion file has no header

        } catch (IOException e) {
            conLog.error("Problem in opening files to log!");
        }
    }

    private void openEventLogFile() {
        ptcDateStr = ExpFrame.pID + "_" + Utils.nowDate();

        // Create files (w/ autoflash) and if not existed, write headers. Append is for later writings
        try {
            final String blkTrialStr = "_B" + activeTrial.blockNum + "_T" + activeTrial.trialNum;
            eventsLogFile = ptcLogFolder.resolve(ptcDateStr + blkTrialStr + "_EVENTS.csv").toFile();

            eventsLogFilePW = new PrintWriter(
                    new FileOutputStream(eventsLogFile, true),
                    true);

        } catch (IOException e) {
            conLog.error("Problem in opening motion files to log!");
        }


    }

    /**
     * Start logging events for this trial
     * @param trial Trial
     */
    public void activateTrial(Trial trial) {
        activeTrial = trial;
        openEventLogFile();

        panZoomInstants = new PanZoomInstants();

        // Clear the log maps
//        eventLog.clear();
//        errorLog.clear();

        // Add the trial open log
        panZoomInstants.trialOpen();
    }

    /**
     * Log a trial event (Instant is already in it)
     * @param event TrialInstants
     */
    public void logEvent(TrialInstants event) {
        // Add the event to the list
//        eventLog.add(event);

        // TODO process the specific times
    }

    /**
     * Called from outside
     * Log an event using only the key (manage the first/last yourself!)
     * @param key Key from TrialInstants
     */
//    public void logEvent(String key) {
//
//        switch (key) {
//            case null -> {
//                // Do nothing
//            }
//
//            // No first, last
//            case TrialInstants.TRIAL_OPEN, TrialInstants.TRIAL_CLOSE -> {
//                eventLog.add( new TrialInstants(key));
//                conLog.debug("Logged {}", key);
//            }
//
//            case TrialInstants.SPACE_PRESS -> {
//                eventLog.add( new TrialInstants(key));
//
//                // Log durations
//                double enterToSpace = getDurationSec(
//                        STR.last(TrialInstants.VIEWPORT_ENTER),
//                        TrialInstants.SPACE_PRESS);
//
//                double firstZoomToSpace = getDurationSec(
//                        STR.first(TrialInstants.ZOOM),
//                        TrialInstants.SPACE_PRESS);
//                double enterToLastZoom = getDurationSec(
//                        STR.last(TrialInstants.VIEWPORT_ENTER),
//                        STR.last(TrialInstants.ZOOM));
//                double firstZoomToLastZoom = getFirstToLastDurSec(
//                        TrialInstants.ZOOM);
//
//
//            }
//
//
//            // Log first, last
//            default -> {
//                String eventKeyFirst = STR.first(key);
//                String eventKeyLast = STR.last(key);
//
//                // If first is empty, add it
//                if (!hasLogged(eventKeyFirst)) {
//                    eventLog.add(new TrialInstants(eventKeyFirst));
//                    conLog.debug("Logged {}, {}", eventKeyFirst, getTrialInstant(eventKeyFirst));
//                }
//
//                // Add last
//                eventLog.add(new TrialInstants(eventKeyLast));
//                conLog.trace("Logged {}", eventKeyLast);
//            }
//        }
//
//
//    }

    /**
     * Log an error
     * @param errKey String key
     * @param errCode int code (from ErrorEvent)
     */
//    public void logError(String errKey, int errCode) {
//        errorLog.add(new ErrorEvent(errKey, errCode));
//    }

    public void logMouseEvent(MouseEvent mevent) {
//        try {
//            // Open logs if not opened
//            if (mTimeLogFilePW == null) openLogFilesToWrite();
//
//            mTimeLogFilePW.println(genLog + SP + timeLog);
//
//        } catch (NullPointerException e) {
////            Main.showDialog("Problem in logging time!");
//        }
    }

    /**
     * Get the Instant of a TrialInstants
     * @param key String key of TrialInstants
     * @return Instant
     */
//    public Instant getTrialInstant(String key) {
//        for (TrialInstants event : eventLog) {
//            if (STR.equals(event.getKey(), key)) return event.getInstant();
//        }
//
//        return Instant.MIN;
//    }

    /**
     * Return the duration (in sec.) between two Instants (indicated by begin and end keys)
     * @param beginKey String begin key
     * @param endKey String end key
     * @return double Duration (in seconds)
     */
//    public double getDurationSec(String beginKey, String endKey) {
//        Instant beginInst = getTrialInstant(beginKey);
//        Instant endInst = getTrialInstant(endKey);
//        conLog.debug("Begin = {}, End = {}", beginInst, endInst);
//        if (beginInst.equals(Instant.MIN) || endInst.equals(Instant.MIN)) return Double.NaN;
//        else return Duration.between(beginInst, endInst).toMillis() / 1000.0;
//    }
//
//    public double getFirstToLastDurSec(String key) {
//        return getDurationSec(STR.first(key), STR.last(key));
//    }

//    /**
//     * Has it logged this key (currently checking last occurance)
//     * @param key String key from TrialInstants keys
//     * @return True if events for this key are logged
//     */
//    public boolean hasLoggedKey(String key) {
//        String lastName = TrialInstants.getLast(key);
//        if (!lastName.isEmpty()) return eventMap.containsKey(lastName);
//        return false;
//    }

//    public boolean hasLogged(String key) {
//        for (TrialInstants event : eventLog) {
//            if (STR.equals(event.getKey(), key)) return true;
//        }
//
//        return false;
//    }

    /**
     * Create a folder if it doesn't exist
     * @param folder Path to the dir
     */
    private void createFolderIfNotExisted(Path folder) {

        if (!Files.isDirectory(folder)) {
            try {
                Files.createDirectory(folder);
            } catch (IOException ioe) {
                conLog.error("Could not create folder: ", folder);
                // TODO: Maybe show a dialog?
            }
        }
    }
}
