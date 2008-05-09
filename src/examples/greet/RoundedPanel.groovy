package greet

import javax.swing.JPanel
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints

/**
 * Created by IntelliJ IDEA.
 * User: Danno.Ferrin
 * Date: May 8, 2008
 * Time: 6:08:55 PM
 */
class RoundedPanel extends JPanel {

    public RoundedPanel() {
        opaque = false
    }

    public boolean isOptimizedDrawingEnabled() {
        return false;
    }


    protected void paintComponent(Graphics g) {
        def oldhint = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING)
        g.setRenderingHint RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON
        g.color = foreground
        ((Graphics2D)g).fillRoundRect 0, 0, width, height, 15, 15
        g.setRenderingHint RenderingHints.KEY_ANTIALIASING, oldhint
    }


}