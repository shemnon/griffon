/**
 * Created by IntelliJ IDEA.
 * User: Danno
 * Date: Nov 7, 2007
 * Time: 2:54:00 PM
 * To change this template use File | Settings | File Templates.
 */
package griffon.builder

import griffon.groovy.swing.SwingBuilder
//import groovy.swing.SwingXBuilder

class UberBuilderConstructorsTest extends GroovyTestCase {

    public void testBuilders() {
        new UberBuilder(new SwingBuilder())
//        new UberBuilder(new SwingXBuilder(), new SwingBuilder())
//        new UberBuilder(jx:new SwingXBuilder(), j:new SwingBuilder())
//        new UberBuilder(new SwingXBuilder(), j:new SwingBuilder())
    }

    public void testClasses() {
        new UberBuilder(SwingBuilder)
//        new UberBuilder(SwingXBuilder, SwingBuilder)
//        new UberBuilder(jx:SwingXBuilder, j:SwingBuilder)
//        new UberBuilder(SwingXBuilder, j:SwingBuilder)
    }

}