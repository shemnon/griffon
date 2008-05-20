package greet;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Danno.Ferrin
 * Date: May 6, 2008
 * Time: 6:55:39 PM
 */
public class ScrollablePanel extends JPanel implements Scrollable{
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
       return 10;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return ((orientation == SwingConstants.VERTICAL) ? visibleRect.height : visibleRect.width) - 10;
    }

    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
