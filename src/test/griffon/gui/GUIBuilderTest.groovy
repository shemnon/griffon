package griffon.gui;

import groovy.util.GroovyTestCase
import groovy.swing.SwingBuilder
import groovy.swing.factory.BeanFactory
import javax.swing.JPanel
import groovy.swing.SwingXBuilder

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
        new GUIBuilder('swingx')
        // GraphicsBuilder looping issue
        //new GUIBuilder('gfx', 'swingx')
        new GUIBuilder()
    }

    public void testSingle() {
        def gb = new GUIBuilder('swing')
        gb.panel(id:'p1') {
            assert currentBuilder instanceof SwingBuilder
        }
        assert gb.p1

        gb = new GUIBuilder(j:'swing')
        gb.jpanel(id:'p2') {
            assert currentBuilder instanceof SwingBuilder
        }
        assert gb.p2

        gb = new GUIBuilder(spanel: new BeanFactory(JPanel))
        def p3 = gb.spanel() { // nothing is defining id: in this instance
            assert currentBuilder instanceof GUIBuilder
        }
        assert p3
    }

    public void testNestedMono() {
        def gb = new GUIBuilder('swing')
        gb.panel(id:'p1') {
            assert currentBuilder instanceof SwingBuilder
            panel(id:'p11') {
                assert currentBuilder instanceof SwingBuilder
            }
        }
        assert gb.p1
        assert gb.p11

        gb = new GUIBuilder(j:'swing')
        gb.jpanel(id:'p2') {
            assert currentBuilder instanceof SwingBuilder
            jpanel(id:'p21') {
                assert currentBuilder instanceof SwingBuilder
            }
        }
        assert gb.p2
        assert gb.p21

        gb = new GUIBuilder(spanel: new BeanFactory(JPanel))
        def p31
        def p3 = gb.spanel() { // nothing is defining id: in this instance
            assert currentBuilder instanceof GUIBuilder
            p31 = spanel() {
                assert currentBuilder instanceof GUIBuilder
            }
        }
        assert p3
        assert p31
    }

    public void testNestedPoly() {
        def gb = new GUIBuilder('swingx', 'swing')
        gb.panel(id:'p1') {
            assert currentBuilder instanceof SwingXBuilder
            checkBox(id:'p11') {
                assert currentBuilder instanceof SwingXBuilder
            }
        }
        assert gb.p1
        assert gb.p11

        gb = new GUIBuilder('swing', 'swingx')
        gb.panel(id:'p2') {
            assert !(currentBuilder instanceof SwingXBuilder)
            checkBox(id:'p21') {
                assert !(currentBuilder instanceof SwingXBuilder)
            }
        }
        assert gb.p2
        assert gb.p21

        gb = new GUIBuilder(j:'swing', jx:'swingx')
        gb.jxpanel(id:'p3') {
            assert (currentBuilder instanceof SwingXBuilder)
            jpanel(id:'p31') {
                assert !(currentBuilder instanceof SwingXBuilder)
            }
        }
        assert gb.p3
        assert gb.p31

        gb.jpanel(id:'p4') {
            assert !(currentBuilder instanceof SwingXBuilder)
            jxpanel(id:'p41') {
                assert (currentBuilder instanceof SwingXBuilder)
            }
        }
        assert gb.p4
        assert gb.p41

        //todo text with custom factory?
    }

    public void testResources() {
        def gb = new GUIBuilder('swingx', 'swing')

        assert gb.resources.getString('foo') == 'bar'
        gb.frame(id:'f')  {
            // note the class here is actually
            // griffon.gui.GUIBuilderTest$_testResources_closureX
            // we are testing closure unwrapping
            assert resources.getString('foo') == 'bar'
        }

        shouldFail(MissingResourceException) {
            ExteriorClass.test(gb)
        }
    }
}

class ExteriorClass {
    // must not have a griffon.gui.ExteriorClass.properties file
    public static void test(GUIBuilder gb) {
        gb.resources
    }
}

