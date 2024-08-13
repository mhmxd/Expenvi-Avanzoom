package ui;

import control.Server;
import enums.Direction;
import model.Config;
import model.ScrollTrial;
import moose.Memo;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;
import tool.MinMax;
import tool.Resources;
import tool.StringWrapper;
import tool.Utils;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static tool.Constants.*;

public class VTScrollPane extends JScrollPane implements MouseListener, MouseWheelListener, PropertyChangeListener {
    private final TaggedLogger conLog = Logger.tag(STR.CONSOLE);

    // Local Constants
    private final Color COLOR_VIEW_BORDER = COLORS.PLATINIUM;
    private final Color COLOR_LINE_NUM_BACK = COLORS.LIGHT_GRAY;
    private final Color COLOR_SCROLLBAR_TRACK = COLORS.VERY_LIGHT_GRAY;
    private final Color COLOR_SCROLLBAR_THUMB = COLORS.GRAY_76;
    private final Color COLOR_LINE_HIGHLIGHT = COLORS.BLUE;
    private final Color COLOR_INDICATOR = COLORS.DARK_GREEN;

    private final int WRAP_CHARS_COUNT = 66;
    private final float TEXT_FONT_SIZE = 20f;
    private final float TEXT_LINE_SPACING = 0.193f;

//    private final int WRAP_CHARS_COUNT = 67;
    private String wrappedFile = "";

//    private final Dimension dim; // in px
    private ArrayList<Integer> charCountInLines = new ArrayList<>();
    private int nLines;

    // Elements
    private JTextPane linesTextPane;
    private JTextPane bodyTextPane;
    private MyScrollBarUI scrollBarUI;

    // Target
    private final MinMax targetMinMax = new MinMax();
    private final MinMax mTargetFullVisScVals = new MinMax(); // Vt scroll values for target fully visible
    private final MinMax mTargetPartVisScVals = new MinMax(); // Vt scroll values for target partially visible

    // Status
    private boolean mCursorIn;
    private boolean mTargetVisible;
    private int mLastScrollVal;  // Keep the last scroll value {to calculate the diff}
    private boolean wheelEnabled;
    private boolean continueScrolling; // To continue scrolling

    private double velocity; // px/s
    private Thread flingThread;

    // CONFIG: Value read from Config (ExFrame)
    private Config config;

//    private PropertiesConfiguration config = new PropertiesConfiguration();
//    private double cnfgVelocityGain;
//    private double cnfgVelocityFriction;
//    private double cnfgMinFlingVelocity;

    // For logging
//    private GeneralInfo mGenInfo = new GeneralInfo();
//    private InstantInfo mInstantInfo = new InstantInfo();
//    private ScrollInfo mScrollInfo = new ScrollInfo();
    private int nTargetAppear;

    // Trial
    private ScrollTrial activeTrial;

    //-------------------------------------------------------------------------------------------------

    public VTScrollPane() {
//        dim = d;

//        setMaximumSize(dim);
//        setMinimumSize(dim);
        setBorder(BorderFactory.createLineBorder(COLOR_VIEW_BORDER));
//        conLog.debug("Dim = {}", dim);

        // Get configs
//        VELOCITY_GAIN = ExpFrame.config.getDouble(STR.VELOCITY_GAIN);
//        VELOCITY_GAIN = (get).getConfig().getDouble(STR.VELOCITY_GAIN);
//        VELOCITY_FRICTION = ExpFrame.config.getDouble(STR.VELOCITY_FRICTION);
//        MIN_FLING_VELOCITY = ExpFrame.config.getDouble(STR.MIN_FLING_VELOCITY);
//
//        conLog.debug("Config: {}, {}, {}", config.getDouble(STR.VELOCITY_GAIN),
//                VELOCITY_FRICTION, MIN_FLING_VELOCITY);
    }

    /**
     * Set the text file for displayed text
     * @param fileName Name of the file (WITHOUT suffix)
     * @return Instance
     */
    public VTScrollPane setText(String fileName, int textWidth, boolean reWrap) {
        final String resFilePath = fileName + ".txt";
        wrappedFile = fileName + "-wrapped.txt";
//        final String wrappedFile = fileName + "-wrapped.txt";

        try {
//            if (reWrap) {
//                final File wFile = new File(wrappedFile);
//                StringWrapper.wrapText(
//                        Resources.TEXT.URI_LOREM,
//                        wrappedFile,
//                        WRAP_CHARS_COUNT);
//            } else {
//                countLines(wrappedFile);
//            }

            // Set the number of lines
            nLines = charCountInLines.size();

            // Body of text
            bodyTextPane = new CustomTextPane(false, textWidth);
            bodyTextPane.read(new FileReader(wrappedFile), "wrapped");
            bodyTextPane.setEditable(false);
            final Font bodyFont = FONTS.SF_LIGHT.deriveFont(TEXT_FONT_SIZE);
            bodyTextPane.setFont(bodyFont);
            bodyTextPane.setSelectionColor(Color.WHITE);

            SimpleAttributeSet bodyStyle = new SimpleAttributeSet();
            StyleConstants.setLineSpacing(bodyStyle,TEXT_LINE_SPACING);

            final int len = bodyTextPane.getStyledDocument().getLength();
            bodyTextPane.getStyledDocument().setParagraphAttributes(0, len, bodyStyle, false);

            // Web page
//            JEditorPane jep = new JEditorPane();
//            jep.setEditable(false);
//
//            try {
//                File file1= new File("lorem.html");
//                jep.setContentType("text/html");
//                jep.setEditable(false);
//                jep.setPage(file1.toURI().toURL());
////                jep.setPage("http://www.google.com");
//            }catch (IOException e) {
//                jep.setContentType("text/html");
//                jep.setText("<html>Could not load</html>");
//            }
//
//            getViewport().add(jep);
            getViewport().add(bodyTextPane);

        } catch (IOException e) {
//            Logs.d(TAG, "Problem createing VTScrollPane -> setText");
            e.printStackTrace();
        }



        return this;
    }

    public void setConfig(Config scrollConfig) {
        config = scrollConfig;
    }

//    public void setConfig(double velGain, double velFriction, double minFlingVel) {
//        cnfgVelocityGain = velGain;
//        cnfgVelocityFriction = velFriction;
//        cnfgMinFlingVelocity = minFlingVel;
//
//        conLog.info("New config set!");
//    }

    /**
     * Set the line numbers (H is the same as the scroll pane)
     * @param lineNumsPaneW Width of the line num pane (mm)
     * @return Current instance
     */
//    public VTScrollPane setLineNums(double lineNumsPaneW) {
//
//        // Set dimention
////        Dimension lnpDim = new Dimension(Utils.mm2px(lineNumsPaneW), dim.height);
//
//        // Set up Line numbers
//        linesTextPane = new JTextPane();
//        linesTextPane.setPreferredSize(lnpDim);
//        linesTextPane.setBackground(COLOR_LINE_NUM_BACK);
//        linesTextPane.setEditable(false);
//        final Font linesFont = FONTS.SF_LIGHT
//                .deriveFont(TEXT_FONT_SIZE)
//                .deriveFont(FONTS.ATTRIB_ITALIC);
//        linesTextPane.setFont(linesFont);
//        linesTextPane.setForeground(Color.GRAY);
//        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
//        StyleConstants.setAlignment(attributeSet, StyleConstants.ALIGN_CENTER);
//        StyleConstants.setLineSpacing(attributeSet,TEXT_LINE_SPACING);
//        final int len = bodyTextPane.getStyledDocument().getLength();
//        linesTextPane.
//                getStyledDocument().
//                setParagraphAttributes(0, len, attributeSet, false);
//
//        linesTextPane.setText(getLineNumbers(charCountInLines.size()));
//
//        // Show the line nums
//        setRowHeaderView(linesTextPane);
//
//        return this;
//    }

    /**
     * Set the scroll bar
     * @param scrollBarW Scroll bar width (mm)
     * @param thumbH Scroll thumb height (mm)
     * @return Current instance
     */
    public VTScrollPane setScrollBar(double scrollBarW, double thumbH) {
        // Set dimentions
        Dimension scBarDim = new Dimension(DISP.mmToPxW(scrollBarW), getHeight());
//        Dimension scThumbDim = new Dimension(scBarDim.width, Utils.mm2px(thumbH));

        // Verticall scroll bar
        scrollBarUI = new MyScrollBarUI(
                COLOR_VIEW_BORDER,
                COLOR_SCROLLBAR_TRACK,
                COLOR_SCROLLBAR_THUMB,
                6);
        getVerticalScrollBar().setUI(scrollBarUI);
        getVerticalScrollBar().setPreferredSize(scBarDim);

        // Scroll thumb
//        UIManager.put("ScrollBar.thumbSize", scThumbDim);

        // Policies
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        return this;
    }


    /**
     * Create the pane (last method)
     * @return The VTScrollPane instance
     */
    public VTScrollPane create() {
        getViewport().getView().addMouseListener(this);
        addMouseWheelListener(this);

        setWheelScrollingEnabled(false); // Don't scroll by default

        Server.get().addPropertyChangeListener(this);

        return this;
    }

    public void setTrial(ScrollTrial trial) {
        activeTrial = trial;

        //--- Choose a target line based on the distance and direction
        // General min/max
//        int minInd = mVTScrollPane.getNVisibleLines(); // No highlight at the top
//        int maxInd = (mVTScrollPane.getNLines() - 1) - mVTScrollPane.getNVisibleLines();
//
//        if (trial.direction == Direction.N) {
//            maxInd -= mGenInfo.trial.getVtDist();
//        } else {
//            minInd += mGenInfo.trial.getVtDist();
//        }
//
//        return mVTScrollPane.getRandLine(minInd, maxInd);

        // Highlight the line
        final int targetLineInd = randLineInd();
        highlight(targetLineInd);

        // Set the initial scroll position
        if (activeTrial.direction == Direction.N) {
            conLog.warn("Target Ind = {}, Init Ind = {}", targetLineInd,
                    targetLineInd + activeTrial.distance);
            setScrollPosition(targetLineInd + activeTrial.distance);
        } else {
            conLog.warn("Target Ind = {}, Init Ind = {}", targetLineInd,
                    targetLineInd - activeTrial.distance);
            setScrollPosition(targetLineInd - activeTrial.distance);
        }



//        highlight(randLineInd());
    }

    /**
     * Enable/disable mouse wheel
     * @param state Boolean
     */
    public void setWheelEnabled(boolean state) {
        wheelEnabled = state;
    }

    /**
     * Highlight a line indicated by targetLineInd
     * @param targetLineInd Index of the line (starting from 1)
     */
    public void highlight(int targetLineInd) {

        try {
            int stIndex = (targetLineInd - 1) * (WRAP_CHARS_COUNT + 1);
//            int endIndex = stIndex + charCountInLines.get(targetLineInd); // highlight the whole line
            final String line = bodyTextPane.getText(stIndex, WRAP_CHARS_COUNT + 1);
            conLog.trace("Line: {}", line);
            final int endPadLen = line.length() - line.trim().length();
            conLog.trace("nBlanks = {}", endPadLen);

            int endIndex = stIndex + WRAP_CHARS_COUNT + 1 - endPadLen;

            DefaultHighlighter.DefaultHighlightPainter highlighter =
                    new DefaultHighlighter
                    .DefaultHighlightPainter(COLOR_LINE_HIGHLIGHT);
            bodyTextPane.getHighlighter().removeAllHighlights();
            bodyTextPane.getHighlighter().addHighlight(stIndex, endIndex,
                    highlighter);
//            bodyTextPane.getHighlighter().addHighlight(stIndex, endIndex, highlighter);
//            Logs.d(TAG, bodyTextPane.getText(stIndex, 10));

        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        // Scrollbar hint
        int nVisibleLines = getNLinesInside();
        int indicOffset = (nVisibleLines - activeTrial.tolerance) / 2;
        int lineH = getLineHeight();

        targetMinMax.setMin(((targetLineInd - (activeTrial.tolerance) - 1) - indicOffset) * lineH);
        targetMinMax.setMax((targetLineInd - activeTrial.tolerance) * lineH);

        scrollBarUI.setIndicator(
                COLOR_LINE_HIGHLIGHT,
                targetMinMax.getMin(),
                targetMinMax.getMax());

        final int targetPos = (targetLineInd - nVisibleLines) * lineH;
        scrollBarUI.setVtIndicator(COLOR_INDICATOR, targetPos);

        getVerticalScrollBar().setUI(scrollBarUI);

        //-- Set the scroll values once
        mTargetFullVisScVals.setMin((targetLineInd - nVisibleLines + 1) * lineH);
        mTargetFullVisScVals.setMax(targetLineInd * lineH);
    }

    /**
     * Count the number of lines and chars in each line
     * Line num = number of \n
     */
    public void countLines(String filePath) {
        try {
            String content = Files.readString(Path.of(filePath));
            String[] lines = content.split("\n");
            for (String line : lines) {
                charCountInLines.add(line.length());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the line numbers to show
     * @param nLines Number of lines
     * @return String of line numbers
     */
    public String getLineNumbers(int nLines) {
//        Logs.d(this.getClass().getName(), "Total lines = " + nLines);
        StringBuilder text = new StringBuilder("0" + System.lineSeparator());
        for(int i = 1; i < nLines + 1; i++){
            text.append(i).append(System.lineSeparator());
        }
        return text.toString();
    }

    /**
     * Get the number of lines
     * @return Number of lines
     */
    public int getNLines() {
        return StringWrapper.countLines(wrappedFile);
//        return nLines;
    }

    /**
     * Get the height of one line
     * @return Line height (in px)
     */
    public int getLineHeight() {
//        Logs.d(TAG, "", getPreferredSize().height, getNVisibleLines());
//        return getPreferredSize().height / getNVisibleLines();
        int bodyPaneH = getViewport().getView().getPreferredSize().height;
//        Logs.d(TAG, "", bodyPaneH, mNumLines);
//        return bodyPaneH / nLines;
        return bodyPaneH / StringWrapper.countLines(wrappedFile);
    }

    /**
     * Get the number of visible lines
     * @return Number of visible lines
     */
    public int getNLinesInside() {
        if (getLineHeight() == 0) return 0;

        return getHeight() / getLineHeight();
//        return dim.height / getLineHeight();
    }

    /**
     * Get the maximum value of the scroll bar
     * @return Maximum scroll value
     */
    public int getMaxScrollVal() {
        return getVerticalScrollBar().getMaximum();
    }

    /**
     * Check if the Target is inside the frame
     * @return 1: inside, 0: outside
     */
    public int isTargetInFrame() {
        final int vtScrollVal = getVerticalScrollBar().getValue();
        return targetMinMax.isWithin(vtScrollVal) ? 1 : 0;
    }

    /**
     * Check if the target is visible (has entered the viewport)
     * @param fully Check for fully visible (true) or partially (false)
     * @return True/false
     */
    public boolean isTargetVisible(boolean fully) {
        final int vtScrollVal = getVerticalScrollBar().getValue();

        if (fully) {
            return mTargetFullVisScVals.isWithin(vtScrollVal);
        } else {
            return mTargetPartVisScVals.isWithin(vtScrollVal);
        }

    }

    /**
     * Get a random line index
     * NOTE: Indexes start from 1
     * @return A random line index
     */
    private int randLineInd() {
        // General min/max
        int minInd = getNLinesInside(); // No highlight in the top window (if scrolled all the way up)
        int maxInd = (getNLines() - 1) - getNLinesInside(); // No highlights in the bottom window
        conLog.info("MinInd = {}, MaxInd = {}", minInd, maxInd);
        // Modif based on direction
        if (activeTrial.direction == Direction.N) {
            maxInd -= activeTrial.distance;
        } else {
            minInd += activeTrial.distance;
        }

        // Get a random line in between that is not blank
        int lineInd;
//        do {
//            lineInd = Utils.randInt(minInd, maxInd);
//        } while (charCountInLines.get(lineInd) == 0);
        do {
            lineInd = Utils.randInt(minInd, maxInd);
        } while (StringWrapper.getLine(wrappedFile, lineInd).isEmpty());
        conLog.info("Rand Line Ind = {}", lineInd);
        return lineInd;
    }


    /**
     * Get the number of Target appearances
     * @return number of Target appearances
     */
    public int getNTargetAppear() {
        return nTargetAppear;
    }

    public boolean isTargetWithinIndicator() {
        final int scrollVal = getVerticalScrollBar().getValue();
        return targetMinMax.isWithin(scrollVal);
    }

    private void setScrollPosition(int lineInd) {
        conLog.info("Scroll pos = {}", lineInd * getLineHeight());
        final int scrollAmt = lineInd * getLineHeight();
        Dimension vpDim = getViewport().getView().getPreferredSize(); // Can be also Preferred
        int extent = getVerticalScrollBar().getModel().getExtent();
        conLog.info("vpDim = {}, extent = {}", vpDim, extent);
        Point vpPos = getViewport().getViewPosition();
        int newY = vpPos.y + scrollAmt;
        conLog.info("vpPos = {}, newPos = {}", vpPos.y, newY);
        if (newY != vpPos.y && newY >= 0 && newY <= (vpDim.height - extent)) {
            getViewport().setViewPosition(new Point(vpPos.x, newY));
        }

        repaint();
    }

    /**
     * Scroll a certain amount
     * @param scrollAmt Amount to scroll (in px)
     */
    public void scroll(int scrollAmt) {
        conLog.trace("Scrolling {}", scrollAmt);
        // Scroll only if cursor is inside
        if (mCursorIn) {
            Dimension vpDim = getViewport().getView().getSize(); // Can be also Preferred
            int extent = getVerticalScrollBar().getModel().getExtent();

            Point vpPos = getViewport().getViewPosition();
            int newY = vpPos.y + scrollAmt;
            if (newY != vpPos.y && newY >= 0 && newY <= (vpDim.height - extent)) {
                getViewport().setViewPosition(new Point(vpPos.x, newY));
            }

            repaint();

            // Log
//            logScroll();
        }

    }

    private void fling(double v) {
        conLog.info("Fling with v = {}", v);
        velocity += v; // Cumulative velocity
        conLog.info("Veloctity = {}", velocity);
        flingThread = new Thread(() -> {
            conLog.info("Thread!! {}, {}", continueScrolling, Math.abs(velocity) );
            while (continueScrolling && Math.abs(velocity) > config.flingMinVelocity) {
                final int dY = (int) (velocity * 0.01); // 0.01s => 0.01Vel px
                scroll(dY);

                velocity -= velocity * config.flingVelocityFriction; // Reduce velocity with friction

                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    break;
                }
            }
        });
        flingThread.start();
    }

    private void stopScroll() {
        conLog.trace("Stop!");
        continueScrolling = false;
        velocity = 0;
//        flingThread.stop();
    }

    // MouseListener ========================================================================================
    @Override
    public void mouseClicked(MouseEvent e) {
//        velocity += 1000;
//        new Thread(() -> {
//            while (Math.abs(velocity) > 10) {
//                final int dY = (int) (velocity * 0.1); // 0.1s => 0.1Vel px
//                scroll(dY);
//                velocity *= 0.9; // Friction
//                try {
//                    Thread.sleep(10);
//                } catch (InterruptedException ex) {
//                    break;
//                }
//            }
//        }).start();
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        mCursorIn = true;
//        if (mInstantInfo.firstEntry == 0) {
//            mInstantInfo.firstEntry = Utils.nowInMillis();
//            mInstantInfo.lastEntry = mInstantInfo.firstEntry;
//        }
//        else mInstantInfo.lastEntry = Utils.nowInMillis();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        mCursorIn = false;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {

        if (wheelEnabled) {
            final double nNotches = e.getPreciseWheelRotation();
            conLog.info("Notches = {}", nNotches);
            final int scrollAmt = (int) (nNotches * config.scrollWheelGain);
            scroll(scrollAmt); // Logging is done inside scroll()
        }

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Is it related to Moose?
        if (STR.equals(evt.getPropertyName(), STR.MOOSE) && (evt.getNewValue() != null)) {
            Memo memo = (Memo) evt.getNewValue();
            conLog.trace("Memo: {}", memo);
//            if (memo.isAction(STR.SCROLL)) {
//                if (memo.isMode(STR.DISPLACE)) scroll(memo.getV2Int());
//                if (memo.isMode(STR.STOP)) stopScroll();
//            }

            if (memo.isAction(STR.PAN)) {
                if (memo.isMode(STR.DISPLACE)) {
                    stopScroll(); // Stop flinging when displacing
                    scroll(memo.getV2Int());
                }
                if (memo.isMode(STR.STOP)) stopScroll();
            }

            if (memo.isAction(STR.FLING)) {
                continueScrolling = true;
                fling(memo.getV2Float() * config.flingVelocityGain);
            }
        }
    }
}
