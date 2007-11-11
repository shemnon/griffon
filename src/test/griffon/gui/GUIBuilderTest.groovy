package griffon.gui;

import groovy.util.GroovyTestCase;

/**
 * Created by IntelliJ IDEA.
 * User: Danno
 * Date: Nov 7, 2007
 * Time: 4:11:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class GUIBuilderTest extends GroovyTestCase {

    public void testConstructor() {
        new GUIBuilder()
        new GUIBuilder('gfx', 'swingx')
        new GUIBuilder()
    }
}
