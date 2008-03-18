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
import org.codehaus.groovy.reflection.ReflectionUtils

class GUIBuilder extends UberBuilder {

    protected static final Set<String> builderPackages = new HashSet<String>();
    static {
        builderPackages.add("groovy.util");
        //builderPackages.add("griffon.gui");
    }

    public GUIBuilder() {
        super(['default'] as Object[])
    }

    public GUIBuilder(Object[] builders) {
        super(builders)
    }

    protected Object loadBuilderLookups() {
        // looping proble with graphisBuidler.getProperty
        this.@builderLookup['default'] = ['swing', 'swingx', 'gfx', [j:'swing', jx:'swingx']] as Object[]
        //builderLookup['default'] = ['swing', 'swingx', [j:'swing', jx:'swingx']] as Object[]
        this.@builderLookup.swing = SwingBuilder
        this.@builderLookup.SwingBuilder = SwingBuilder
        this.@builderLookup.swingx = SwingXBuilder
        this.@builderLookup.SwingXBuilder = SwingXBuilder
        // looping proble with graphisBuidler.getProperty
        this.@builderLookup.gfx = GraphicsBuilder
        this.@builderLookup.GraphicsBuilder = GraphicsBuilder
    }

    public ResourceBundle getResources() {
        return ResourceBundle.getBundle(ReflectionUtils.getCallingClass(1, builderPackages)
                .name
                .split(/\$/)[0]
                .replace('.', '/'))
    }
}