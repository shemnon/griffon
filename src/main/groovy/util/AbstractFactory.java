/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovy.util;

import java.util.Map;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.com>
 */
public abstract class AbstractFactory implements Factory {
    public boolean isLeaf() {
        return false;
    }

    public boolean onHandleNodeAttributes( FactoryBuilderSupport builder, Object node,
            Map attributes ) {
        return true;
    }

    public void onNodeCompleted( FactoryBuilderSupport builder, Object parent, Object node ) {

    }

    public void setParent( FactoryBuilderSupport builder, Object parent, Object child ) {

    }

    public void setChild( FactoryBuilderSupport builder, Object parent, Object child ) {

    }

}
