/*
 * Copyright 2004-2010 the original author or authors.
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

import griffon.util.RunMode
import griffon.util.Environment
import org.codehaus.griffon.commons.ApplicationHolder

/**
 * Gant script that bootstraps a Griffon application
 *
 * @author Graeme Rocher
 */

// No point doing this stuff more than once.
if (getBinding().variables.containsKey("_bootstrap_called")) return
_bootstrap_called = true

includeTargets << griffonScript('Package')

target(bootstrap: 'Loads and configures a Griffon instance') {
    loadApp()
}

target(loadApp:'Loads the Griffon application object') {
    depends(prepackage)
    event('AppLoadStart', ['Loading Griffon Application'])

    [classesDir, pluginClassesDir, i18nDir, resourcesDir].each { d ->
        addUrlIfNotPresent rootLoader, d
    }
    setupRuntimeJars().each { j ->
        addUrlIfNotPresent rootLoader, j
    }
    setupJavaOpts().each { op ->
        def nameValueSwitch = op =~ "-D(.*?)=(.*)"
        if (nameValueSwitch.matches()) {
            System.setProperty(nameValueSwitch[0][1], nameValueSwitch[0][2])
        }
    }

    griffonApp = rootLoader.loadClass(griffonApplicationClass, false).newInstance()
    griffonApp.bootstrap()
    ApplicationHolder.application = griffonApp
    event('AppLoadEnd', ['Loading Griffon Application'])
}

setupRuntimeJars = {
    def runtimeJars = []

    File jardir = new File(ant.antProject.replaceProperties(buildConfig.griffon.jars.destDir))
    // list all jars
    jardir.eachFileMatch(~/.*\.jar/) {f ->
        runtimeJars += f
    }

// XXX -- NATIVE
    platformDir = new File(jardir.absolutePath, platform)
    if(platformDir.exists()) {
        platformDir.eachFileMatch(~/.*\.jar/) {f ->
            runtimeJars += f
        }
    }
// XXX -- NATIVE

    return runtimeJars
}

setupJavaOpts = { includeNative = true ->
    def javaOpts = []

    File jardir = new File(ant.antProject.replaceProperties(buildConfig.griffon.jars.destDir))
    def env = System.getProperty(Environment.KEY)
    javaOpts << "-D${Environment.KEY}=${env}"
    javaOpts << "-D${RunMode.KEY}=${RunMode.current}"

    if (buildConfig.griffon.app?.javaOpts) {
        buildConfig.griffon.app?.javaOpts.each { javaOpts << it }
    }
    if (argsMap.javaOpts) {
        javaOpts << argsMap.javaOpts
    }

    javaOpts << "-Dgriffon.start.dir=\""+jardir.parentFile.absolutePath+"\""

// XXX -- NATIVE
    platformDir = new File(jardir.absolutePath, platform)
    File nativeLibDir = new File(platformDir.absolutePath, 'native')
    if(nativeLibDir.exists()) {
        String libraryPath = System.getProperty('java.library.path')
        libraryPath = libraryPath + File.pathSeparator + nativeLibDir.absolutePath
        javaOpts << "-Djava.library.path=$libraryPath".toString()
    }
// XXX -- NATIVE

    return javaOpts
}
