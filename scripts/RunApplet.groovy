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
/**
 * Created by IntelliJ IDEA.
 *@author Danno.Ferrin
 * Date: Aug 5, 2008
 * Time: 10:35:06 PM
 */


Ant.property(environment: "env")
griffonHome = Ant.antProject.properties."env.GRIFFON_HOME"

includeTargets << new File("${griffonHome}/scripts/Package.groovy")
//includeTargets << new File ( "${griffonHome}/scripts/_PackagePlugins.groovy" )

target('default': "Runs the applicaiton from the command line") {
    depends(checkVersion)
    runApp()
}


target(runApp: "Does the actual command line execution") {
    depends(packageApp)

    // calculate the needed jars
    File jardir = new File(Ant.antProject.replaceProperties(config.griffon.jars.destDir))
    runtimeJars = []
    jardir.eachFileMatch(~/.*\.jar/) {f ->
        runtimeJars += f
    }

    // setup the vm
    if (!binding.variables.webstartVM) {
        webstartVM = [System.properties['java.home'], 'bin', 'javaws'].join(File.separator)
    }

    // start the processess
    Process p = "$webstartVM ${config.griffon.applet.jnlp}".execute(null as String[], jardir)

    // pipe the output
    p.consumeProcessOutput(System.out, System.err)

    // wait for it.... wait for it...
    p.waitFor()
}