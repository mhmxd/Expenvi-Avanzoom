package ui;

import com.google.common.util.concurrent.AtomicDouble;
import control.Server;
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
import java.util.concurrent.atomic.AtomicInteger;

import static tool.Constants.*;

public class VTScrollPane extends JScrollPane implements MouseListener, MouseWheelListener, PropertyChangeListener {
    private final TaggedLogger conLog = Logger.tag(getClass().getSimpleName());

    public static final int WRAP_CHARS_COUNT = 70;
//    private final int WRAP_CHARS_COUNT = 67;
//    private final String WRAPPED_FILE_NAME = "./res/wrapped.txt";

    private final Dimension dim; // in px
    private ArrayList<Integer> charCountInLines = new ArrayList<>();
    private int mNumLines;

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
    private boolean isScrolling; // To continue scrolling

    private double velocity; // px/s

    // For logging
//    private GeneralInfo mGenInfo = new GeneralInfo();
//    private InstantInfo mInstantInfo = new InstantInfo();
//    private ScrollInfo mScrollInfo = new ScrollInfo();
    private int nTargetAppear;

    private final Color COLOR_VIEW_BORDER = COLORS.PLATINIUM;
    private final Color COLOR_LINE_NUM_BACK = COLORS.LIGHT_GRAY;
    private final Color COLOR_SCROLLBAR_TRACK = COLORS.VERY_LIGHT_GRAY;
    private final Color COLOR_SCROLLBAR_THUMB = COLORS.GRAY_76;
    private final Color COLOR_LINE_HIGHLIGHT = COLORS.BLUE;
    private final Color COLOR_INDICATOR = COLORS.DARK_GREEN;

    private final float TEXT_FONT_SIZE = 20.5f;
    private final float TEXT_LINE_SPACING = 0.193f;


    //-------------------------------------------------------------------------------------------------

    public VTScrollPane(Dimension d) {
        dim = d;
        setPreferredSize(dim);
        setBorder(BorderFactory.createLineBorder(COLOR_VIEW_BORDER));
        conLog.info("Dim = {}", dim);
    }

    /**
     * Set the text file for displayed text
     * @param fileName Name of the file (WITHOUT suffix)
     * @return Instance
     */
    public VTScrollPane setText(String fileName) {
        final String resFilePath = fileName + ".txt";
        final String wrappedFile = fileName + "-wrapped.txt";

        try {
            if (!(new File(wrappedFile).isFile())) {
                charCountInLines = StringWrapper.wrapFile(
                        Resources.TEXT.URI_LOREM,
                        wrappedFile,
                        WRAP_CHARS_COUNT);
            } else {
                countLines(wrappedFile);
            }

            // Set the number of lines
            mNumLines = charCountInLines.size();

            // Body of text
            bodyTextPane = new CustomTextPane(false);
            bodyTextPane.read(new FileReader(wrappedFile), "wrapped");
            bodyTextPane.setEditable(false);
            final Font bodyFont = FONTS.SF_LIGHT.deriveFont(TEXT_FONT_SIZE);
            bodyTextPane.setFont(bodyFont);
            bodyTextPane.setSelectionColor(Color.WHITE);

            SimpleAttributeSet bodyStyle = new SimpleAttributeSet();
            StyleConstants.setLineSpacing(bodyStyle,TEXT_LINE_SPACING);

            final int len = bodyTextPane.getStyledDocument().getLength();
            bodyTextPane.getStyledDocument().setParagraphAttributes(0, len, bodyStyle, false);

            getViewport().add(bodyTextPane);

        } catch (IOException e) {
//            Logs.d(TAG, "Problem createing VTScrollPane -> setText");
            e.printStackTrace();
        }

        return this;
    }

    /**
     * Set the line numbers (H is the same as the scroll pane)
     * @param lineNumsPaneW Width of the line num pane (mm)
     * @return Current instance
     */
    public VTScrollPane setLineNums(double lineNumsPaneW) {

        // Set dimention
        Dimension lnpDim = new Dimension(Utils.mm2px(lineNumsPaneW), dim.height);

        // Set up Line numbers
        linesTextPane = new JTextPane();
        linesTextPane.setPreferredSize(lnpDim);
        linesTextPane.setBackground(COLOR_LINE_NUM_BACK);
        linesTextPane.setEditable(false);
        final Font linesFont = FONTS.SF_LIGHT
                .deriveFont(TEXT_FONT_SIZE)
                .deriveFont(FONTS.ATTRIB_ITALIC);
        linesTextPane.setFont(linesFont);
        linesTextPane.setForeground(Color.GRAY);
        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        StyleConstants.setAlignment(attributeSet, StyleConstants.ALIGN_CENTER);
        StyleConstants.setLineSpacing(attributeSet,TEXT_LINE_SPACING);
        final int len = bodyTextPane.getStyledDocument().getLength();
        linesTextPane.
                getStyledDocument().
                setParagraphAttributes(0, len, attributeSet, false);

        linesTextPane.setText(getLineNumbers(charCountInLines.size()));

        // Show the line nums
        setRowHeaderView(linesTextPane);

        return this;
    }

    /**
     * Set the scroll bar
     * @param scrollBarW Scroll bar width (mm)
     * @param thumbH Scroll thumb height (mm)
     * @return Current instance
     */
    public VTScrollPane setScrollBar(double scrollBarW, double thumbH) {
        // Set dimentions
        Dimension scBarDim = new Dimension(Utils.mm2px(scrollBarW), dim.height);
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
     * @param frameSizeLines Size of the frame (in lines)
     */
    public void highlight(int targetLineInd, int frameSizeLines) {

        // Highlight line
        try {
            int stIndex = 0;
            for (int li = 0; li < targetLineInd; li++) {
                stIndex += charCountInLines.get(li) + 1; // prev. lines + \n
            }
            int endIndex = stIndex + charCountInLines.get(targetLineInd); // highlight the whole line
//            Logs.d(TAG, charCountInLines.size(), targetLineInd, frameSizeLines, stIndex, endIndex);
            DefaultHighlighter.DefaultHighlightPainter highlighter =
                    new DefaultHighlighter
                    .DefaultHighlightPainter(COLOR_LINE_HIGHLIGHT);
            bodyTextPane.getHighlighter().removeAllHighlights();
            bodyTextPane.getHighlighter().addHighlight(stIndex, endIndex, highlighter);
//            Logs.d(TAG, bodyTextPane.getText(stIndex, 10));

        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        // Indicator
        int nVisibleLines = getNVisibleLines();
        int frOffset = (nVisibleLines - frameSizeLines) / 2;
        int lineH = getLineHeight();

        targetMinMax.setMin((targetLineInd - (frameSizeLines - 1) - frOffset) * lineH);
        targetMinMax.setMax((targetLineInd - frOffset) * lineH);

        scrollBarUI.setIndicator(
                COLOR_LINE_HIGHLIGHT,
                targetMinMax.getMin(),
                targetMinMax.getMax());

        final int targetPos = (targetLineInd - nVisibleLines) * lineH;
        scrollBarUI.setVtIndicator(COLOR_INDICATOR, targetPos);

        getVerticalScrollBar().setUI(scrollBarUI);
//        Logs.d(TAG, "Indicator", nVisibleLines, frameSizeLines, frOffset, lineH,
//                mTargetMinMax.getMin(), mTargetMinMax.getMax());

        //-- Set the scroll values once
        mTargetFullVisScVals.setMin((targetLineInd - nVisibleLines + 1) * lineH);
        mTargetFullVisScVals.setMax(targetLineInd * lineH);
    }

    /**
     * Scroll a certain amount
     * @param scrollAmt Amount to scroll (in px)
     */
    public void scroll(int scrollAmt) {
        conLog.info("Scrolling {}", scrollAmt);
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

    private void scroll(double v) {
        velocity += v;
        new Thread(() -> {
            while (isScrolling && Math.abs(velocity) > 10) {
                final int dY = (int) (velocity * 0.1); // 0.1s => 0.1Vel px
                scroll(dY);
                velocity *= 0.9; // Friction
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    break;
                }
            }
        }).start();
    }

    private void stopScroll() {
        conLog.info("Stop!");
        isScrolling = false;
        velocity = 0;
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
        return mNumLines;
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
        return bodyPaneH / mNumLines;
    }

    /**
     * Get the number of visible lines
     * @return Number of visible lines
     */
    public int getNVisibleLines() {
        return dim.height / getLineHeight();
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
     * Get a random line between the two values
     * @param min Min line index (inclusive)
     * @param max Max line index (exclusive)
     * @return Line number
     */
    public int getRandLine(int min, int max) {

        int lineInd = 0;
        do {
            lineInd = Utils.randInt(min, max);
        } while (charCountInLines.get(lineInd) == 0);

        return lineInd;
    }


    /**
     * Get the number of Target appearances
     * @return number of Target appearances
     */
    public int getNTargetAppear() {
        return nTargetAppear;
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
            final double unitsToScroll = e.getPreciseWheelRotation();
            final int dY = (int) (unitsToScroll * 5);
            scroll(dY); // Logging is done inside scroll()
        }

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Is it related to Moose?
        if (STRINGS.equals(evt.getPropertyName(), STRINGS.MOOSE) && (evt.getNewValue() != null)) {
            Memo memo = (Memo) evt.getNewValue();
            conLog.info("Memo: {}", memo);
            if (memo.isAction(STRINGS.SCROLL)) {
                switch (memo.getMode()) {
                    case STRINGS.DISPLACE -> scroll(memo.getV2Int());
                    case STRINGS.FLING -> {
                        isScrolling = true;
                        scroll(memo.getV2Float());
                    }
                    case STRINGS.STOP -> stopScroll();
                }

            }
        }
    }
}
