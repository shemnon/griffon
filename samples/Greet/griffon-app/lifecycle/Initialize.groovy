import griffon.util.GriffonPlatformHelper
import groovy.swing.SwingBuilder

/*
* This script is executed inside the EDT, so be sure to
* call long running code in another thread.
*
* You have the following options
* - SwingBuilder.doOutside { // your code  }
* - Thread.start { // your code }
* - SwingXBuilder.withWorker( start: true ) {
*      onInit { // initialization (optional, runs in current thread) }
*      work { // your code }
*      onDone { // finish (runs inside EDT) }
*   }
*
* You have the following options to run code again inside EDT
* - SwingBuilder.doLater { // your code }
* - SwingBuilder.edt { // your code }
* - SwingUtilities.invokeLater { // your code }
*/

GriffonPlatformHelper.tweakForNativePlatform(app)
SwingBuilder.lookAndFeel('nimbus', 'mac', 'gtk', ['metal', [boldFonts: false]])
System.properties['http.maxRedirects']='2'