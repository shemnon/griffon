/*
 * Copyright 2008-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language govnerning permissions and
 * limitations under the License.
 */
package griffon.swing

import java.awt.Window
import griffon.core.GriffonApplication

/**
 * Default implementation of {@code WindowDisplayManage} that simply makes the window visible on show() and disposes it on hide().
 *
 * @author Andres Almiray
 * @since 0.3.1
 */
class DefaultWindowDisplayHandler implements WindowDisplayHandler {
    void show(Window window, GriffonApplication application) {
        window.visible = true
    }

    void hide(Window window, GriffonApplication application) {
        window.dispose()
    }
}