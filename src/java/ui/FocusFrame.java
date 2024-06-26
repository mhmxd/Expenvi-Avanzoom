package ui;

import org.tinylog.Logger;
import org.tinylog.TaggedLogger;
import tool.Constants;

import javax.swing.*;
import java.awt.*;

public class FocusFrame extends JPanel {
    private final TaggedLogger conLog = Logger.tag(getClass().getSimpleName());

    private boolean active;

    public FocusFrame() {
        setLayout(null);
        setOpaque(false);
    }

    /**
     * Paints the component, drawing a rectangle representing the focus area.
     * If active, the rectangle is filled with green color, otherwise with red color.
     *
     * @param g the graphics context
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.DARK_GRAY);
        g2d.setStroke(new BasicStroke(Constants.BORDERS.THICKNESS_1));
        g2d.drawRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
    }

    /**
     * Checks if the focus area is active.
     *
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return this.active;
    }

    /**
     * Sets the focus area as active or inactive.
     *
     * @param active true to set active, false to set inactive
     */
    public void setActive(boolean active) {
        this.active = active;
    }
}
