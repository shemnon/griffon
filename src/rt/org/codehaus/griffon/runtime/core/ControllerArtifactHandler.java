/*
 * Copyright 2009-2010 the original author or authors.
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

package org.codehaus.griffon.runtime.core;

import griffon.core.GriffonApplication;
import griffon.core.GriffonClass;
import griffon.core.GriffonControllerClass;

/**
 * Handler for 'Controller' artifacts.
 *
 * @author Andres Almiray
 *
 * @since 0.9.1
 */
public class ControllerArtifactHandler extends ArtifactHandlerAdapter {
    public ControllerArtifactHandler(GriffonApplication app) {
        super(app, GriffonControllerClass.TYPE, GriffonControllerClass.TRAILING);
    }

    protected GriffonClass newGriffonClassInstance(Class clazz) {
        return new DefaultGriffonControllerClass(getApp(), clazz);
    }
}