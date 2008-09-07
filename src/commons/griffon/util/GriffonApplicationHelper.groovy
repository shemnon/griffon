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

import griffon.builder.UberBuilder
import java.awt.Toolkit
import javax.swing.SwingUtilities
import javax.swing.JFrame

/**
 * Created by IntelliJ IDEA.
 * User: Danno.Ferrin
 * Date: May 17, 2008
 * Time: 3:28:46 PM
 */
class GriffonApplicationHelper {

    static void prepare(IGriffonApplication app) {
        app.config = new ConfigSlurper().parse(app.configClass)
        app.builderConfig = new ConfigSlurper().parse(app.builderClass)
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
            if (mpe.property != property) {
                throw mpe
            }
            /* else ignore*/
        }
    }

    static void safeCall(reciever, method) {
        try {
            reciever."$method"()
        } catch (MissingMethodException mme) {
            if (mme.method != method) {
                throw mme
            }
            /* else ignore*/
        }
    }

    public static void runScriptInsideEDT(String scriptName, IGriffonApplication app) {
        def script = null
        try {
            script = GriffonApplicationHelper.classLoader.loadClass(scriptName).newInstance(app.bindings)
        } catch (ClassNotFoundException cnfe) {
            if (cnfe.getMessage() == scriptName) {
                // the script must not exist, do nothing
                //LOGME - may be because of chained failures
                return
            } else {
                throw cnfe;
            }
        }
        if (SwingUtilities.isEventDispatchThread()) {
            script.run()
        } else {
            SwingUtilities.invokeAndWait script.&run
        }
    }

    private static Class createInstance(String mvcName, String className, IGriffonApplication app) {
        ClassLoader classLoader = app.getClass().classLoader

        Class klass = classLoader.loadClass(app.config.mvcGroups[mvcName][className]);

        // inject app via EMC
        // this also insures EMC metaclasses later
        klass.metaClass.app = app
        return klass
    }

    public static createMVCGroup(IGriffonApplication app, def mvcType, def mvcName = mvcType) {
        Class modelKlass = createInstance(mvcType, "model", app)
        Class viewKlass = createInstance(mvcType, "view", app)
        Class controllerKlass = createInstance(mvcType, "controller", app)

        UberBuilder builder = CompositeBuilderHelper.createBuilder(app,
            [model:modelKlass, view:viewKlass, controller:controllerKlass])

        def model = modelKlass.newInstance()
        def view = viewKlass.newInstance()
        def controller = controllerKlass.newInstance()

        app.models[mvcName] = model
        app.views[mvcName] = view
        app.controllers[mvcName] = controller
        app.builders[mvcName] = builder

        safeSet(model,      "controller", controller)
        safeSet(model,      "view",       view)
        safeSet(view,       "model",      model)
        safeSet(view,       "controller", controller)
        safeSet(controller, "model",      model)
        safeSet(controller, 'view',       view)
        safeSet(controller, "builder",    builder)

        safeCall(model,          "grinit")
        safeCall(controller,     "grinit")

        builder.controller = controller
        builder.model = model
        builder.edt({builder.build(view) })

        return [model, view, controller]
    }

    public static def createJFrameApplication(IGriffonApplication app) {
        Object frame = null
        // try config specified first
        if (app.config.application?.frameClass) {
            try {
                frame = getClass().getClassLoader().loadClass(app.config.application?.frameClass)
            } catch (Throwable t) {
                // ignore
            }
        }
        if (frame == null) {
            // JXFrame, it's nice.  Try it!
            try {
                ClassLoader cl = getClass().getClassLoader();
                if (cl) {
                    frame = cl.loadClass('org.jdesktop.swingx.JXFrame').newInstance()
                } else {
                    frame = Class.forName('org.jdesktop.swingx.JXFrame').newInstance()
                }
            } catch (Throwable t) {
                // ignore
            }
            // this will work for sure
            if (frame == null) {
                frame = new JFrame()
            }

            // do some standard tweaking
            frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        }
        return frame
    }


}
