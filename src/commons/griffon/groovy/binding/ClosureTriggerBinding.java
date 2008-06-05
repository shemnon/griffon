package griffon.groovy.binding;

import org.codehaus.groovy.binding.*;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.StackTraceUtils;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MetaClass;

import java.util.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public class ClosureTriggerBinding implements TriggerBinding, SourceBinding {

    Closure closure;

    public Closure getClosure() {
        return closure;
    }

    public void setClosure(Closure closure) {
        this.closure = closure;
    }

    private BindPath createBindPath(String propertyName, FullInterceptorProxy proxy) {
        BindPath bp = new BindPath();
        bp.propertyName = propertyName;
        List<BindPath> childPaths = new ArrayList<BindPath>();
        for (Map.Entry<String, FullInterceptorProxy> entry : proxy.fields.entrySet()) {
            childPaths.add(createBindPath(entry.getKey(), entry.getValue()));
        }
        bp.children = childPaths.toArray(new BindPath[childPaths.size()]);
        return bp;
    }

    public FullBinding createBinding(SourceBinding source, TargetBinding target) {
        if (source != this) {
            throw new RuntimeException("Source binding must the Trigger Binding as well");
        }
        FullInterceptorProxy delegate = new FullInterceptorProxy();
        try {
            // first, pick out the property references
            synchronized (closure) {
                // be obsessive and asseret total ownership over the closure while being snopped at
                Object oldDelegate = closure.getDelegate();
                int oldResolveStrategy = closure.getResolveStrategy();

                try {
                    closure.setDelegate(delegate);
                    closure.setResolveStrategy(Closure.DELEGATE_ONLY);
                    closure.call();
                } catch (Exception e) {
                    //LOGME
                    // ignore it, likely failing because we are faking out properties
                } finally {
                    closure.setResolveStrategy(oldResolveStrategy);
                    closure.setDelegate(oldDelegate);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            throw new RuntimeException("A closure expression binding could not be created because of " + e.getClass().getName() + ":\n\t" + e.getMessage());
        }
        List<BindPath> rootPaths = new ArrayList<BindPath>();
        for (Map.Entry<String, FullInterceptorProxy> entry : delegate.fields.entrySet()) {
            BindPath bp =createBindPath(entry.getKey(), entry.getValue());
            bp.currentObject = closure;
            rootPaths.add(bp);
        }
        //System.out.println(rootPaths);
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

class BindPath {
    Object currentObject;
    String propertyName;
    boolean boundGlobal;
    boolean boundName;

    BindPath[] children;


    public synchronized void updatePath(PropertyChangeListener listener, Object newObject, Set updateSet) {
        if (currentObject != newObject) {
            removeListeners(listener);
        }
        //System.out.println(children);
        //System.out.println(children.length);
        if ((children != null) && (children.length > 0)) {
            try {
                Object newValue = null;
                if (newObject != null) {
                    updateSet.add(newObject);
                    newValue = InvokerHelper.getProperty(newObject, propertyName);
                }
                //System.out.println("Considering " + newObject + " as value for " + propertyName);
                for (BindPath child : children) {
                    child.updatePath(listener, newValue, updateSet);
                }
            } catch (Exception e) {
                //LOGME
                // do we ignore it, or fail?
            }
        }
        if (currentObject != newObject) {
            addListeners(listener, newObject, updateSet);
        }
    }

    public void addAllListeners(PropertyChangeListener listener, Object newObject, Set updateSet) {
        addListeners(listener, newObject, updateSet);
        //System.out.println(children);
        //System.out.println(children.length);
        if ((children != null) && (children.length > 0)) {
            try {
                Object newValue = null;
                if (newObject != null) {
                    updateSet.add(newObject);
                    newValue = InvokerHelper.getProperty(newObject, propertyName);
                }
                //System.out.println("Considering " + newObject + " as value for " + propertyName);
                for (BindPath child : children) {
                    child.addAllListeners(listener, newValue, updateSet);
                }
            } catch (Exception e) {
                //LOGME
                // do we ignore it, or fail?
            }
        }
    }

    static final Class[] NAME_PARAMS = {String.class, PropertyChangeListener.class};
    static final Class[] GLOBAL_PARAMS = {PropertyChangeListener.class};

    public void addListeners(PropertyChangeListener listener, Object newObject, Set updateSet) {
        boundName = false;
        boundGlobal = false;
        if (newObject != null) {
//            try {
                MetaClass mc = InvokerHelper.getMetaClass(newObject);
                if (!mc.respondsTo(newObject, "addPropertyChangeListener", NAME_PARAMS).isEmpty()) {
                    InvokerHelper.invokeMethod(newObject, "addPropertyChangeListener", new Object[] {propertyName, listener});
                    //System.out.println("Bound to name " + propertyName + " on " + newObject);
                    boundName = true;
                    updateSet.add(newObject);
                } else if (!mc.respondsTo(newObject, "addPropertyChangeListener", GLOBAL_PARAMS).isEmpty()) {
                    InvokerHelper.invokeMethod(newObject, "addPropertyChangeListener", listener);
                    //System.out.println("Bound  globally on " + newObject);
                    boundGlobal = true;
                    updateSet.add(newObject);
                } else {
                    //System.out.println("Not Bound at all on " + newObject);
                }
//            } catch (Exception e) {
//                // didn't bind to propery name for some reason
//                // possibly because they don't have property scope change listeners
//                    System.out.println(Arrays.asList(newObject.getClass().getDeclaredMethods()));
//                    System.out.println(Arrays.asList(newObject.getClass().getDeclaredFields()));
//                    //LOGME
//                    // do we ignore it, or fail?
//                }
//            }
        }
        currentObject = newObject;
    }

    public void removeListeners(PropertyChangeListener listener) {
        if (boundGlobal) {
            try {
                InvokerHelper.invokeMethod(currentObject, "removePropertyChangeListener", listener);
            } catch (Exception e) {
                //LOGME ignore the failure
            }
        }
        if (boundName) {
            try {
                InvokerHelper.invokeMethod(currentObject, "removePropertyChangeListener", new Object[] {propertyName, listener});
            } catch (Exception e) {
                //LOGME ignore the failure
            }
        }
    }

    public String toString() {
        return propertyName + " { " + Arrays.asList(children) + " ) ";
    }
}

class PropertyPathFullBinding extends AbstractFullBinding implements PropertyChangeListener {

    /**
     * The set of all objects where a property change does incur a listener-re-check
     */
    Set updateObjects = new HashSet();
    BindPath[] bindPaths;
    boolean bound;

    public void bind() {
        //System.out.println("Binding!");
        //Closure refClosure = ((ClosureSourceBinding)sourceBinding).getClosure();
        updateObjects.clear();
        //System.out.println("Update objects cleard");
        for (BindPath bp : bindPaths) {
            //System.out.println("Binding path " + bp);
            bp.addAllListeners(this, bp.currentObject, updateObjects);
        }
        bound = true;
    }

    public void unbind() {
        updateObjects.clear();
        for (BindPath path : bindPaths) {
            path.removeListeners(this);
        }
        bound = false;
    }

    public void rebind() {
        if (bound) bind();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        //System.out.println(evt);
        //System.out.println(updateObjects);                
        if (updateObjects.contains(evt.getSource())) {
            bind();
        }
        update();
    }
}

class DeadEndObject {
    public Object getProperty(String property) {
        throw new RuntimeException("Cannot bind to a property on the return value of a method call");
    }
    public Object invokeMethod(String name, Object args) {
        return this;
    }
}

class FullInterceptorProxy extends GroovyObjectSupport {
    static final DeadEndObject DEAD_END = new DeadEndObject();

    Map<String, FullInterceptorProxy> fields = new LinkedHashMap<String, FullInterceptorProxy>();

    public Object getProperty(String property) {
        if (fields.containsKey(property)) {
            //System.out.println("Faking property " + property + " with existing " + fields.get(property));
            return fields.get(property);
        } else {
            FullInterceptorProxy proxy = new FullInterceptorProxy();
            fields.put(property, proxy);
            //System.out.println("Faking property " + property + " with new " + fields.get(property));
            return proxy; 
        }
    }

    public Object invokeMethod(String name, Object args) {
        //System.out.println("Faking method " + name + " with DeadEnd");
        return DEAD_END;
    }


    public String toString() {
        return fields.toString();
    }
}
