/*
 * Copyright 2003-2008 the original author or authors.
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
package org.codehaus.groovy.reflection;

import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: Danno Ferrin
 * Date: Jan 17, 2008
 * Time: 7:37:06 PM
 */
public class ReflectionUtils {
    // these are packages in the call stack that are only part of the groovy MOP

    private static final Set<String> ignoredPackages = new HashSet<String>();
    static {
        //ignoredPackages.add("java.lang.reflect");
        ignoredPackages.add("groovy.lang");
        ignoredPackages.add("org.codehaus.groovy.reflection");
        ignoredPackages.add("org.codehaus.groovy.runtime.callsite");
        ignoredPackages.add("org.codehaus.groovy.runtime");
        ignoredPackages.add("sun.reflect");
    }

    public static Class getCallingClass() {
        return getCallingClass(1);
    }

    public static Class getCallingClass(int matchLevel) {
        int depth = 0;
        try {
            Class c;
            do {
                do {
                    c = sun.reflect.Reflection.getCallerClass(depth++);
                } while (c != null && c.getPackage() != null && ignoredPackages.contains(c.getPackage().getName()));
            } while (c != null && matchLevel-- > 0);
            return c;
        } catch (Exception e) {
            return null;
        }
    }

    public static Class getCallingClass(int matchLevel, Collection<String> extraIgnoredPackages) {
        int depth = 0;
        try {
            Class c;
            do {
                do {
                    c = sun.reflect.Reflection.getCallerClass(depth++);
                } while ((c != null)
                      && (c.getPackage() != null)
                      && (ignoredPackages.contains(c.getPackage().getName())
                          || extraIgnoredPackages.contains(c.getPackage().getName())));
            } while (c != null && matchLevel-- > 0);
            return c;
        } catch (Exception e) {
            return null;
        }
    }
}
