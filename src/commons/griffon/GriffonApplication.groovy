package griffon

import groovy.swing.SwingBuilder
import java.awt.Toolkit
import javax.swing.SwingUtilities

/**
 * Created by IntelliJ IDEA.
 * User: Danno.Ferrin
 * Date: May 17, 2008
 * Time: 3:28:46 PM
 */
class GriffonApplication {

    Map controllers = [:]
    Map views = [:]
    Binding initBindings = new Binding() 

    static GriffonApplication launch(Closure attachRootPanelDelegate, Closure attachMenuBarDelegate = {}, Class c = "Application" as Class) {

        GriffonApplication app = new GriffonApplication()

        def config = new ConfigSlurper().parse(c)

        app.initBindings.app = app
        app.runScriptInsideEDT('Initialize')

        // init the builders
        // this is where a composite gets made and composites are added
        // for now we punt and make a SwingBuilder
        config.controllers.each {k, v ->
            //todo wire in previous controllers?
            def controller = GriffonApplication.classLoader.loadClass(v).newInstance()
            safeSet(controller, "app", app)
            app.controllers[k] = controller
        }

        // instantiate controllers
        config.viewMap.each {k, v ->
            def controller = app.controllers[k]
            SwingBuilder builder = new SwingBuilder() // use composite here whenr eady
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
        if (config.primaryView =~ /\w+\.\w+/) {
            def loc = config.primaryView.split(/\./)
            attachRootPanelDelegate(app.views[loc[0]][loc[1]])
        }
        if (config.primaryMenuBar =~ /\w+\.\w+/) {
            def loc = config.primaryMenuBar.split(/\./)
            attachMenuBarDelegate(app.views[loc[0]][loc[1]])
        }

        app.runScriptInsideEDT('Startup')

        // wait for EDT to empty out.... somehow
        boolean empty = false
        while (true) {
            SwingUtilities.invokeAndWait {empty = Toolkit.getDefaultToolkit().getSystemEventQueue().peekEvent() == null}
            if (empty) break
            sleep(100)
        }

        app.runScriptInsideEDT('Ready')

        return app
    }


    static void safeSet(reciever, property, value) {
        try {
            reciever."$property" = value
        } catch (MissingPropertyException mpe) {
            /*ignore*/
        }

    }

    public void appletStart() {
        runScriptInsideEDT('AppletStart')
    }


    public void appletStop() {
        runScriptInsideEDT('AppletStart')
    }

    public void appletDestroy() {
        runScriptInsideEDT('AppletDestroy')
    }

    protected void runScriptInsideEDT(String scriptName) {
        try {
            def script = GriffonApplication.classLoader.loadClass(scriptName).newInstance(initBindings)
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