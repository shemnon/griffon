package griffon.groovy.swing.binding
/**
 * Created by IntelliJ IDEA.
 * User: Danno.Ferrin
 * Date: Jun 19, 2008
 * Time: 12:52:31 PM
 * To change this template use File | Settings | File Templates.
 */
class AbstractSyntheticMetaMethods {

    static void enhance(Object o, Map enhancedMethods) {
        Class klass = o.getClass()
        MetaClassRegistry mcr = GroovySystem.metaClassRegistry
        MetaClass mc = mcr.getMetaClass(klass)
        boolean init = false
        mcr.removeMetaClass klass //??
        //if (!(mc instanceof ExpandoMetaClass)) {
            mc = new ExpandoMetaClass(klass)
            init = true
        //}
        enhancedMethods.each {k, v ->
            if (mc.getMetaMethod(k) == null) {
                mc.registerInstanceMethod(k, v)
            }
        }
        if (init) {
            mc.initialize()
            mcr.setMetaClass(klass, mc)
        }

    }

}
