/*
 * Copyright 2004-2008 the original author or authors.
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
 * Gant script that creates a new Griffon Model-View-Controller triads
 *
 * @author Danno Ferrin
 * @author Graeme Rocher
 *
 */

import org.codehaus.griffon.commons.GriffonClassUtils as GCU

includeTargets << griffonScript("Init")
includeTargets << griffonScript("CreateIntegrationTest")

target (createElement : "Creates a new Element") {
    depends(checkVersion, parseArguments)
    promptForName(type: "Element")
    def (pkg, name) = extractArtifactName(argsMap["params"][0])
    def fqn = "${pkg?pkg:''}${pkg?'.':''}${GCU.getClassNameRepresentation(name)}"

    createArtifact(
        name: fqn,
        suffix: "Model",
        type: "Model",
        path: "griffon-app/models")

    createArtifact(
        name: fqn,
        suffix: "View",
        type: "View",
        path: "griffon-app/views")

    createArtifact(
        name: fqn,
        suffix: "Controller",
        type: "Controller",
        path: "griffon-app/controllers")

    createIntegrationTest(name: name, suffix: "")

    def applicationConfigFile = new File("${basedir}/griffon-app/conf/Application.groovy")
    def configText = applicationConfigFile.text
    if (!(configText =~ /\s*elements\s*\{/)) {
        configText += """
elements {
}
"""
    }
    applicationConfigFile.withWriter { it.write configText.replaceAll(/\s*elements\s*\{/, """
elements {
    // Element definition for "$args"
    '$name' {
        model = '${fqn}Model'
        view = '${fqn}View'
        controller = '${fqn}Controller'
    }
""") }
}

setDefaultTarget(createElement)