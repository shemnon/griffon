/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Created by IntelliJ IDEA.
 * User: Danno
 * Date: Nov 7, 2007
 * Time: 3:55:26 PM
 * To change this template use File | Settings | File Templates.
 */
package griffon.gui

import griffon.builder.UberBuilder
import griffon.groovy.swing.SwingBuilder
//import groovy.swing.SwingXBuilder
//import groovy.swing.j2d.GraphicsBuilder
import org.codehaus.groovy.reflection.ReflectionUtils

class GUIBuilder extends UberBuilder {

    protected static final Set<String> builderPackages = new HashSet<String>();
    static {
        builderPackages.add("org.codehaus.groovy.runtime.metaclass");
        builderPackages.add("griffon.builder");
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
//        this.@builderLookup.swingx = SwingXBuilder
//        this.@builderLookup.SwingXBuilder = SwingXBuilder
        // looping proble with graphisBuidler.getProperty
//        this.@builderLookup.gfx = GraphicsBuilder
//        this.@builderLookup.GraphicsBuilder = GraphicsBuilder

        registerExplicitProperty(
            'resources',
            {-> ResourceBundle.getBundle(ReflectionUtils.getCallingClass(1, builderPackages)
                .name
                .split('$')[0]
                .replace('.', '/'))},
            null);
    }

    ///////////////////////////////////////////////////////////////////////////
    // these appear to be Groovy 1.6beta2 bugs?  It breaks without it!
    ///////////////////////////////////////////////////////////////////////////
    public Object invokeMethod(String methodName) {
        return super.invokeMethod(methodName)
    }

    ///////////////////////////////////////////////////////////////////////////
    // these appear to be Groovy 1.6beta2 bugs?  It breaks without it!
    ///////////////////////////////////////////////////////////////////////////
    public Object invokeMethod(String methodName, Object args) {
        return super.invokeMethod(methodName, args)
    }

    ///////////////////////////////////////////////////////////////////////////
    // these appear to be Groovy 1.6beta2 bugs?  It breaks without it!
    ///////////////////////////////////////////////////////////////////////////
    public Object getProperty(String property) {
        return super.getProperty(property)
    }

    ///////////////////////////////////////////////////////////////////////////
    // these appear to be Groovy 1.6beta2 bugs?  It breaks without it!
    ///////////////////////////////////////////////////////////////////////////
    public void setProperty(String property, Object newValue) {
        super.setProperty(property, newValue)
    }

}