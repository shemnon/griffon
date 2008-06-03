package greet;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Created by IntelliJ IDEA.
 * User: Danno.Ferrin
 * Date: May 8, 2008
 * Time: 6:08:55 PM
 */
class RoundedPanel extends JPanel {

    public RoundedPanel() {
        setOpaque(false);
    }

    public boolean isOptimizedDrawingEnabled() {
        return false;
    }


    protected void paintComponent(Graphics g) {
        Object oldhint = ((Graphics2D)g).getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(getForeground());
        g.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldhint);
    }


}