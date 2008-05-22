package griffon.application

import griffon.util.GriffonApplicationHelper
import java.awt.Dimension
import java.awt.Point
import javax.swing.JFrame
import griffon.util.IGriffonApplication
import java.awt.Container


class SingleFrameApplication implements IGriffonApplication {

    Map controllers = [:]
    Map views = [:]
    Binding bindings = new Binding()
    ConfigObject config
    JFrame mainFrame

    public SingleFrameApplication() {
        GriffonApplicationHelper.prepare(this)

        // prepare the frame
        mainFrame = new JFrame(config.application?.title ?: "")
        mainFrame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        mainFrame.windowClosing = this.&shutdown

        if (config.application?.size) {
            mainFrame.size = config.application.size as Dimension
        }
        if (config.application?.location) {
            mainFrame.location = config.applicaiton.location as Point
        } else {
            mainFrame.locationByPlatform = true
        }

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
