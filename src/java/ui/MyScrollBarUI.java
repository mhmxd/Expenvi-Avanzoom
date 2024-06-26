package ui;

import tool.MinMax;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public class MyScrollBarUI extends BasicScrollBarUI {
    private Color mBorderClr;
    private Color mTrackClr;
    private Color mThumbClr;
    private Color mIndicatorClr;
    private int mThumbSideOffset; // dist. to the sides of thumb

    private int highlightMin, highlightMax; // Min/max value of hightlight area

    private int mVtIndicVal;
    private int mHzIndicVal;

    /**
     * Create the scroll bar
     * @param borderClr Border color
     * @param trackClr Track color
     * @param thumbClr Thumb color
     * @param offs Offset
     */
    public MyScrollBarUI(Color borderClr, Color trackClr, Color thumbClr, int offs) {
        mBorderClr = borderClr;
        mTrackClr = trackClr;
        mThumbClr = thumbClr;
        mThumbSideOffset = offs;
    }

    /**
     * Highligh
     * @param hlColor Highlight color
     * @param hlMin Highlight min value
     * @param hlMax Highlight max value
     */
    public void setIndicator(Color hlColor, int hlMin, int hlMax) {
        mIndicatorClr = hlColor;
        highlightMin = hlMin;
        highlightMax = hlMax;
    }

    public void setVtIndicator(Color clr, int val) {
        mIndicatorClr = clr;
        mVtIndicVal = val;
    }

    public void setHzIndicator(Color clr, int val) {
        mIndicatorClr = clr;
        mHzIndicVal = val;
    }

    /**
     * Set the frame
     * @param hlColor Highlight color
     * @param hlMinMax Min/max of the frame
     */
    public void setHighlightFrame(Color hlColor, MinMax hlMinMax) {
        mIndicatorClr = hlColor;
        highlightMin = hlMinMax.getMin();
        highlightMax = hlMinMax.getMax();
    }

    @Override
    protected void installComponents() {
        switch (scrollbar.getOrientation()) {
            case JScrollBar.VERTICAL:
                incrButton = createIncreaseButton(SOUTH);
                decrButton = createDecreaseButton(NORTH);
                break;

            case JScrollBar.HORIZONTAL:
                if (scrollbar.getComponentOrientation().isLeftToRight()) {
                    incrButton = createIncreaseButton(EAST);
                    decrButton = createDecreaseButton(WEST);
                } else {
                    incrButton = createIncreaseButton(WEST);
                    decrButton = createDecreaseButton(EAST);
                }
                break;
        }
        scrollbar.add(incrButton);
        scrollbar.add(decrButton);
        // Force the children's enabled state to be updated.
        scrollbar.setEnabled(scrollbar.isEnabled());
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        // Track
        g.setColor(mTrackClr);
        g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        // Border
        g.setColor(mBorderClr);
        g.drawRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);

        // Highlight the rectangle on the track (if there's highlight)
        if (mIndicatorClr != null) {
            if (scrollbar.getOrientation() == HORIZONTAL) {
                double ratio = trackBounds.width / (scrollbar.getMaximum() * 1.0);
                int hlX = (int) (highlightMin * ratio);
                int hlXMax = (int) (highlightMax * ratio);
                int hlW = hlXMax - hlX + thumbRect.width;
                hlX = (int) (mHzIndicVal * ratio) + thumbRect.width - 1; // -1 for length 3
                hlW = 3;
                g.setColor(mIndicatorClr);
                g.fillRect(hlX, trackBounds.y + 1, hlW, trackBounds.height);
            } else { // VERTICAL
                double ratio = trackBounds.height / (scrollbar.getMaximum() * 1.0);
                int hlYMin = (int) (highlightMin * ratio);
                int hlYMax = (int) (highlightMax * ratio);
//                int hlH = hlYMax - hlYMin + thumbRect.height;
                hlYMin = (int) (mVtIndicVal * ratio) + thumbRect.height - 1;
                int hlH = 3;
                g.setColor(mIndicatorClr);
                g.fillRect(trackBounds.x + 1, hlYMin, trackBounds.width, hlH);
            }

        }
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        Graphics2D graphics2D = (Graphics2D) g;

        graphics2D.setColor(mThumbClr);
        graphics2D.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (scrollbar.getOrientation() == HORIZONTAL) {
            graphics2D.fillRoundRect(
                    thumbBounds.x,
                    thumbBounds.y + (mThumbSideOffset / 2),
                    thumbBounds.width, thumbBounds.height - mThumbSideOffset,
                    5, 5);
        } else { // VERTICAL
            graphics2D.fillRoundRect(
                    thumbBounds.x + (mThumbSideOffset / 2),
                    thumbBounds.y,
                    thumbBounds.width - mThumbSideOffset, thumbBounds.height,
                    5, 5);
        }

    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createZeroButton();
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return createZeroButton();
    }

    /**
     * Create a dummy button (used for inc/dec buttons
     * @return JButton
     */
    protected JButton createZeroButton() {
        JButton button = new JButton("zero button");
        Dimension zeroDim = new Dimension(0,0);
        button.setPreferredSize(zeroDim);
        button.setMinimumSize(zeroDim);
        button.setMaximumSize(zeroDim);
        return button;
    }
}
