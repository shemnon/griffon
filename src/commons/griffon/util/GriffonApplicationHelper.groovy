package griffon.util

import groovy.swing.SwingBuilder
import java.awt.Toolkit
import javax.swing.SwingUtilities

/**
 * Created by IntelliJ IDEA.
 * User: Danno.Ferrin
 * Date: May 17, 2008
 * Time: 3:28:46 PM
 */
class GriffonApplicationHelper {

    static void prepare(IGriffonApplication app) {

        app.config = new ConfigSlurper().parse(app.configClass)

        app.bindings.app = app

        app.initialize();
    }

    static void startup(IGriffonApplication app) {
        // init the builders
        // this is where a composite gets made and composites are added
        // for now we punt and make a SwingBuilder
        app.config.controllers.each {k, v ->
            //todo wire in previous controllers?
            def controller = app.getClass().classLoader.loadClass(v).newInstance()
            safeSet(controller, "app", app)
            app.controllers[k] = controller
        }

        // instantiate controllers
        app.config.viewMap.each {k, v ->
            def controller = app.controllers[k]
            SwingBuilder builder = new SwingBuilder() // use composite here when ready
            builder.containingWindows += app.bindings.rootWindow
            safeSet(controller, "builder", builder)
            builder.controller = controller
            app.views[k] = [:]
            // link views to controllers
            v.each {label, klass ->
                def built
                builder.edt {built = builder.build(klass as Class) }
                safeSet(controller, label, built)
                app.views[k][label] = built
            }
        }

        // attach the root panel
        if (app.config.primaryView =~ /\w+\.\w+/) {
            def loc = app.config.primaryView.split(/\./)
            app.attachRootPanel(app.views[loc[0]][loc[1]])
        }
        if (app.config.primaryMenuBar =~ /\w+\.\w+/) {
            def loc = app.config.primaryMenuBar.split(/\./)
            app.attachMenuBar(app.views[loc[0]][loc[1]])
        }

        app.startup();
    }

    /**
     * Calls the ready lifecycle mehtod after the EDT calms down
     */
    public static void callReady(IGriffonApplication app) {
        // wait for EDT to empty out.... somehow
        boolean empty = false
        while (true) {
            SwingUtilities.invokeAndWait {empty = Toolkit.getDefaultToolkit().getSystemEventQueue().peekEvent() == null}
            if (empty) break
            sleep(100)
        }

        app.ready();
    }


    static void safeSet(reciever, property, value) {
        try {
            reciever."$property" = value
        } catch (MissingPropertyException mpe) {
            /*ignore*/
        }

    }

    public static void runScriptInsideEDT(String scriptName, IGriffonApplication app) {
        try {
            def script = GriffonApplicationHelper.classLoader.loadClass(scriptName).newInstance(app.bindings)
            if (SwingUtilities.isEventDispatchThread()) {
                script.run()
            } else {
                SwingUtilities.invokeAndWait script.&run
            }
        } catch (Exception e) {
            //e.printStackTrace(System.out)
        }
    }
}