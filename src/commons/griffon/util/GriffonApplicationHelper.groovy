/*
 * Copyright 2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package griffon.util

import griffon.groovy.swing.SwingBuilder
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
            Class controllerClass = app.getClass().classLoader.loadClass(v)

            // inject app via EMC
            controllerClass.metaClass.app = app

            def controller = controllerClass.newInstance()

            // just in case it's a field... set app
            safeSet(controller, "app", app)

            app.controllers[k] = controller
        }

        // instantiate controllers
        app.config.viewMap.each {k, v ->
            def controller = app.controllers[k]
            SwingBuilder builder = new SwingBuilder() // use composite here when ready

            // add declared composite controller injections
            // for now we statically add threading methods
            Class controllerClass = controller.getClass()
            controllerClass.metaClass.edt = builder.&edt
            controllerClass.metaClass.doOutside = builder.&doOutside
            controllerClass.metaClass.doLater = builder.&doLater

            // set the single frame as the defaut parent
            builder.containingWindows += app.bindings.rootWindow

            // inject builder via EMC
            controllerClass.metaClass.builder = builder
            // just in case it's a field...
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