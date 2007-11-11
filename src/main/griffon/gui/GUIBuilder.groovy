/**
 * Created by IntelliJ IDEA.
 * User: Danno
 * Date: Nov 7, 2007
 * Time: 3:55:26 PM
 * To change this template use File | Settings | File Templates.
 */
package griffon.gui

import griffon.builder.UberBuilder
import groovy.swing.SwingBuilder
import groovy.swing.SwingXBuilder
import groovy.swing.j2d.GraphicsBuilder

class GUIBuilder extends UberBuilder {

    public GUIBuilder() {
        super(['default'] as Object[])
    }

    public GUIBuilder(Object[] builders) {
        super(['default', *builders] as Object[])
    }

    protected Object loadBuilderLookups() {
        println 'uber!'
        builderLookup['default'] = ['swing', 'swingx', 'gfx', [j:'swing', jx:'swingx']] as Object[]
        builderLookup.swing = SwingBuilder
        builderLookup.SwingBuilder = SwingBuilder
        builderLookup.swingx = SwingXBuilder
        builderLookup.SwingXBuilder = SwingXBuilder
        builderLookup.gfx = GraphicsBuilder
        builderLookup.GraphicsBuilder = GraphicsBuilder
    }
}