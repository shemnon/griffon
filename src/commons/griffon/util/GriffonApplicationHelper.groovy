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

        def model
        def view
        def controller

        [model, view, controller] = createMVCGroup(app, 'root')


        // attach the root panel
        if (app.config.primaryView) {
            app.attachRootPanel(view[app.config.primaryView])
        }
        if (app.config.primaryMenuBar) {
            app.attachMenuBar(view[app.config.primaryMenuBar])
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
            e.printStackTrace(System.out)
        }
    }

    private static Object createInstance(String mvcName, String className, IGriffonApplication app) {
        ClassLoader classLoader = app.getClass().classLoader

        Class klass = classLoader.loadClass(app.config.mvcGroups[mvcName][className]);

        // inject app via EMC
        // this also insures EMC metaclasses later
        klass.metaClass.app = app
        def instance = klass.newInstance()

        // just in case it's a field... set app
        safeSet(instance, "app", app)
        return instance
    }

    public static createMVCGroup(IGriffonApplication app, def mvcName) {
        //TODO do we get this from app or pass it in as a param?
        SwingBuilder builder = new SwingBuilder() // use composite here when ready

        def model = createInstance(mvcName, "model", app)
        def view = createInstance(mvcName, "view", app)
        def controller = createInstance(mvcName, "controller", app)
        app.models[mvcName] = model
        app.views[mvcName] = view
        app.controllers[mvcName] = controller

        // add declared composite controller injections
        // for now we statically add threading methods
        controller.getMetaClass().edt = builder.&edt
        controller.getMetaClass().doOutside = builder.&doOutside
        controller.getMetaClass().doLater = builder.&doLater

        // set the single frame as the defaut parent
        //TODO get this from usage context, whenw e figure out how it is done
        builder.containingWindows += app.bindings.rootWindow

        safeSet(model,      "controller", controller)
        safeSet(model,      "view",       view)
        safeSet(view,       "model",      model)
        safeSet(view,       "controller", controller)
        safeSet(controller, "model",      model)
        safeSet(controller, 'view',       view)
        safeSet(controller, "builder",    builder)

        builder.controller = controller
        builder.model = model
        builder.edt {builder.build(view) }

        return [model, view, controller]
    }
}