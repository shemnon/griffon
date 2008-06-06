/*
 * Copyright 2008 the original author or authors.
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
package griffon.groovy.binding;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import org.codehaus.groovy.binding.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ClosureTriggerBinding implements TriggerBinding, SourceBinding {

    Closure closure;

    public Closure getClosure() {
        return closure;
    }

    public void setClosure(Closure closure) {
        this.closure = closure;
    }

    private BindPath createBindPath(String propertyName, BindPathSnopper proxy) {
        BindPath bp = new BindPath();
        bp.propertyName = propertyName;
        List<BindPath> childPaths = new ArrayList<BindPath>();
        for (Map.Entry<String, BindPathSnopper> entry : proxy.fields.entrySet()) {
            childPaths.add(createBindPath(entry.getKey(), entry.getValue()));
        }
        bp.children = childPaths.toArray(new BindPath[childPaths.size()]);
        return bp;
    }

    public FullBinding createBinding(SourceBinding source, TargetBinding target) {
        if (source != this) {
            throw new RuntimeException("Source binding must the Trigger Binding as well");
        }
        BindPathSnopper delegate = new BindPathSnopper();
        try {
            // first, pick out the property references
            final Closure closureLocalCopy = closure;
            synchronized (closureLocalCopy) {
                // be obsessive and asseret total ownership over the closure while being snopped at
                Object oldDelegate = closureLocalCopy.getDelegate();
                int oldResolveStrategy = closureLocalCopy.getResolveStrategy();

                try {
                    closureLocalCopy.setDelegate(delegate);
                    closureLocalCopy.setResolveStrategy(Closure.DELEGATE_ONLY);
                    closureLocalCopy.call();
                } catch (DeadEndException e) {
                    // we want this exception exposed.
                    throw e;
                } catch (Exception e) {
                    //LOGME
                    // ignore it, likely failing because we are faking out properties
                } finally {
                    closureLocalCopy.setResolveStrategy(oldResolveStrategy);
                    closureLocalCopy.setDelegate(oldDelegate);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            throw new RuntimeException("A closure expression binding could not be created because of " + e.getClass().getName() + ":\n\t" + e.getMessage());
        }
        List<BindPath> rootPaths = new ArrayList<BindPath>();
        for (Map.Entry<String, BindPathSnopper> entry : delegate.fields.entrySet()) {
            BindPath bp =createBindPath(entry.getKey(), entry.getValue());
            bp.currentObject = closure;
            rootPaths.add(bp);
        }
        PropertyPathFullBinding fb = new PropertyPathFullBinding();
        fb.setSourceBinding(new ClosureSourceBinding(closure));
        fb.setTargetBinding(target);
        fb.bindPaths = rootPaths.toArray(new BindPath[rootPaths.size()]);
        return fb;
    }

    public Object getSourceValue() {
        return closure.call();
    }
}

class DeadEndException extends RuntimeException {
    DeadEndException(String message) { super(message); }
}

class DeadEndObject {
    public Object getProperty(String property) {
        throw new DeadEndException("Cannot bind to a property on the return value of a method call");
    }
    public Object invokeMethod(String name, Object args) {
        return this;
    }
}

class BindPathSnopper extends GroovyObjectSupport {
    static final DeadEndObject DEAD_END = new DeadEndObject();

    Map<String, BindPathSnopper> fields = new LinkedHashMap<String, BindPathSnopper>();

    public Object getProperty(String property) {
        if (fields.containsKey(property)) {
            return fields.get(property);
        } else {
            BindPathSnopper proxy = new BindPathSnopper();
            fields.put(property, proxy);
            return proxy;
        }
    }

    public Object invokeMethod(String name, Object args) {
        return DEAD_END;
    }
}
