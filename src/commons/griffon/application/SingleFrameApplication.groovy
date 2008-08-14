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
package griffon.application

import griffon.util.GriffonApplicationHelper
import java.awt.Dimension
import java.awt.Point
import javax.swing.JFrame
import griffon.util.IGriffonApplication
import java.awt.Container


class SingleFrameApplication implements IGriffonApplication {

    Map models = [:]
    Map views = [:]
    Map controllers = [:]
    Binding bindings = new Binding()
    ConfigObject config
    ConfigObject builderConfig

    JFrame mainFrame

    public SingleFrameApplication() {
        GriffonApplicationHelper.prepare(this)

        // prepare the frame
        mainFrame = new JFrame(config.application?.title ?: "")
        mainFrame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        mainFrame.windowClosing = { shutdown() }

        if (config.application?.size) {
            mainFrame.size = config.application.size as Dimension
        }
        if (config.application?.location) {
            mainFrame.location = config.applicaiton.location as Point
        } else {
            mainFrame.locationByPlatform = true
        }
        bindings.rootWindow = mainFrame

        GriffonApplicationHelper.startup(this)

        mainFrame.show()

        GriffonApplicationHelper.callReady(this)
    }

    public void attachMenuBar(Container menuBar) {
        mainFrame.JMenuBar = menuBar
    }

    public void attachRootPanel(Container rootPane) {
        mainFrame.contentPane = rootPane
    }

    public Class getConfigClass() {
        return getClass().classLoader.loadClass("Application") 
    }

    public Class getBuilderClass() {
        return getClass().classLoader.loadClass("Builder")
    }

    public void initialize() {
        GriffonApplicationHelper.runScriptInsideEDT("Initialize", this)
    }

    public void ready() {
        GriffonApplicationHelper.runScriptInsideEDT("Ready", this)
    }

    public void shutdown() {
        GriffonApplicationHelper.runScriptInsideEDT("Shutdown", this)
    }

    public void startup() {
        GriffonApplicationHelper.runScriptInsideEDT("Startup", this)
    }

    public static void main(String[] args) {
        new SingleFrameApplication();
    }
}
