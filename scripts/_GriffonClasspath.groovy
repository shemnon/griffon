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

import griffon.util.PlatformUtils
import org.codehaus.groovy.control.CompilerConfiguration
import org.springframework.core.io.FileSystemResource

/**
 * Gant script containing the Griffon classpath setup.
 *
 * @author Peter Ledbrook (Grails 1.1)
 */

// No point doing this stuff more than once.
if (getBinding().variables.containsKey("_griffon_classpath_called")) return
_griffon_classpath_called = true

includeTargets << griffonScript("_GriffonSettings")

classpathSet = false
includePluginJarsOnClasspath = true

target(name:'classpath', description: "Sets the Griffon classpath", prehook:null, posthook:null) {
    setClasspath()
}

/**
 * Obtains all of the plug-in Lib directories
 * @deprecated Use "pluginSettings.pluginLibDirectories"
 */
getPluginLibDirs = {
    pluginSettings.pluginLibDirectories
}

/**
 * Obtains an array of all plug-in JAR files as Spring Resource objects
 * @deprecated Use "pluginSettings.pluginJarFiles".
 */
getPluginJarFiles = {
    pluginSettings.pluginJarFiles
}

/**
 * Obtains an array of all plug-in test JAR files as Spring Resource objects
 */
getPluginTestFiles = {
    pluginSettings.pluginTestFiles
}

getJarFiles = {->
    def jarFiles = resolveResources("file:${basedir}/lib/*.jar").toList()
    if(includePluginJarsOnClasspath) {

        def pluginJars = pluginSettings.pluginJarFiles

        for (pluginJar in pluginJars) {
            boolean matches = jarFiles.any {it.file.name == pluginJar.file.name}
            if (!matches) jarFiles.add(pluginJar)
        }
    }

    def userJars = resolveResources("file:${userHome}/.griffon/lib/*.jar")
    for (userJar in userJars) {
        jarFiles.add(userJar)
    }

// XXX -- NATIVE
    resolveResources("file:${basedir}/lib/${PlatformUtils.platform}/*.jar").each { platformJar ->
        jarFiles << platformJar
    }
    resolveResources("file:${pluginsHome}/*/lib/${PlatformUtils.platform}/*.jar").each { platformPluginJar ->
        jarFiles << platformPluginJar
    }
// XXX -- NATIVE

    jarFiles.addAll(getExtraDependencies())

    jarFiles
}

getExtraDependencies = {
    def jarFiles =[]
    if(buildConfig?.griffon?.compiler?.dependencies) {
        def extraDeps = ant.fileScanner(buildConfig.griffon.compiler.dependencies)
        for(jar in extraDeps) {
            jarFiles << new FileSystemResource(jar)
        }
    }
    jarFiles
}

commonClasspath = {
    def griffonDir = resolveResources("file:${basedir}/griffon-app/*")
    for (d in griffonDir) {
        pathelement(location: "${d.file.absolutePath}")
    }
    // pathelement(location: "${griffonSettings.sourceDir}/main")

    def pluginLibDirs = pluginSettings.pluginLibDirectories.findAll { it.exists() }
    for (pluginLib in pluginLibDirs) {
        fileset(dir: pluginLib.file.absolutePath)
    }

// XXX -- NATIVE
    resolveResources("file:${basedir}/lib/${PlatformUtils.platform}").each { platformLib ->
        if(platformLib.file.exists()) fileset(dir: platformLib.file.absolutePath)
    }
    resolveResources("file:${pluginsHome}/*/lib/${PlatformUtils.platform}").each { platformPluginLib ->
        if(platformPluginLib.file.exists()) fileset(dir: platformPluginLib.file.absolutePath)
    }
// XXX -- NATIVE
}

compileClasspath = {
    commonClasspath.delegate = delegate
    commonClasspath.call()

    def dependencies = griffonSettings.compileDependencies
    if(dependencies) {
        for(File f in dependencies) {
            if(f)
                pathelement(location: f.absolutePath)
        }
    }
    pathelement(location: "${classesDir.absolutePath}")
    pathelement(location: "${pluginClassesDir.absolutePath}")
}

testClasspath = {
    commonClasspath.delegate = delegate
    commonClasspath.call()

    def dependencies = griffonSettings.testDependencies
    if(dependencies) {

        for(File f in dependencies) {
            if(f) {
                pathelement(location: f.absolutePath)
            }
        }
    }

    pathelement(location: "${classesDir.absolutePath}")
    pathelement(location: "${pluginClassesDir.absolutePath}")
    pathelement(location: "${griffonSettings.testClassesDir}/shared")
    pathelement(location: "${griffonSettings.testResourcesDir}")

    for (pluginTestJar in getPluginTestFiles()) {
        if(pluginTestJar.file.exists()) file(file: pluginTestJar.file.absolutePath)
    }
}

runtimeClasspath = {
    commonClasspath.delegate = delegate
    commonClasspath.call()

    def dependencies = griffonSettings.runtimeDependencies
    if(dependencies) {        
        for(File f in dependencies) {
            if(f)
                pathelement(location: f.absolutePath)
        }
    }

    pathelement(location: "${pluginClassesDir.absolutePath}")
    pathelement(location: "${classesDir.absolutePath}")
}

/**
 * Converts an Ant path into a list of URLs.
 */
classpathToUrls = { String classpathId ->
    def propName = "converted.classpath"
    ant.pathconvert(refid: classpathId, dirsep: "/", pathsep: ":", property: propName)

    return ant.project.properties.get(propName).split(":").collect { new File(it).toURI().toURL() }
}


void setClasspath() {
    // Make sure the following code is only executed once.
    if (classpathSet) return

    if(isApplicationProject || isPluginProject) {
        if(!(new File(classesDir.absolutePath).exists())) ant.mkdir(dir: classesDir.absolutePath)
        if(!(new File(pluginClassesDir.absolutePath).exists())) ant.mkdir(dir: pluginClassesDir.absolutePath)
        if(!(new File("${griffonSettings.testClassesDir}/shared").exists())) ant.mkdir(dir: "${griffonSettings.testClassesDir}/shared")
        if(!(griffonSettings.testResourcesDir.exists())) ant.mkdir(dir: griffonSettings.testResourcesDir)
    }

    ant.path(id: "griffon.compile.classpath", compileClasspath)
    ant.path(id: "griffon.test.classpath", testClasspath)
    ant.path(id: "griffon.runtime.classpath", runtimeClasspath)

    def griffonDir = resolveResources("file:${basedir}/griffon-app/*")
    StringBuffer cpath = new StringBuffer("")

    def jarFiles = getJarFiles()

    for (dir in griffonDir) {
        cpath << dir.file.absolutePath << File.pathSeparator
        // Adding the griffon-app folders to the root loader causes re-load issues as
        // root loader returns old class before the griffon GCL attempts to recompile it
        //rootLoader?.addURL(dir.URL)
    }
    cpath << classesDirPath << File.pathSeparator
    cpath << pluginClassesDirPath << File.pathSeparator
    
    for (jar in jarFiles) {
        cpath << jar.file.absolutePath << File.pathSeparator
        addUrlIfNotPresent rootLoader, jar.file
    }
    cpath << testResourcesDirPath << File.pathSeparator

    // We need to set up this configuration so that we can compile the
    // plugin descriptors, which lurk in the root of the plugin's project
    // directory.
    compConfig = new CompilerConfiguration()
    compConfig.setClasspath(cpath.toString());
    compConfig.sourceEncoding = "UTF-8"

    if(isApplicationProject || isPluginProject) {
        // if(!resourcesDir.exists()) ant.mkdir(dir: resourcesDirPath)
        // if(!griffonSettings.testResourcesDir.exists()) ant.mkdir(dir: griffonSettings.testResourcesDir)
        addUrlIfNotPresent rootLoader, resourcesDirPath
        addUrlIfNotPresent rootLoader, griffonSettings.testResourcesDir
    }

    classpathSet = true
}

addUrlIfNotPresent = { to, what ->
    if(!to || !what) return
    def urls = to.URLs.toList()
    switch(what.class) {
         case URL:
             if(!urls.contains(what)) {
                 to.addURL(what)
             }
             return
         case String: what = new File(what); break
         case GString: what = new File(what.toString()); break
         case File: break; // ok
         default:
             println "Don't know how to deal with $what as it is not an URL nor a File"
             System.exit(1)
    }

    if(what.directory && !what.exists()) what.mkdirs()
    def url = what.toURI().toURL()
    if(!urls.contains(url)) {
        to.addURL(url)
    }
}

dirNotEmpty = { Map args ->
    if(!args.dir.exists()) retunr false
    return ant.fileset(args).size() > 0
}
