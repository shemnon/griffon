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

import org.codehaus.groovy.runtime.InvokerHelper;

import java.beans.PropertyChangeListener;
import java.util.Set;

import groovy.lang.MetaClass;

/**
 * The bind path object.  This class represents one "step" in the bind path.
 */
@SuppressWarnings({"unchecked"}) // all are of type Object, so generics are useless
public class BindPath {

    /**
     * The object we think we are bound to
     */
    Object currentObject;

    /**
     * The proeprty we are intiereted in
     */
    String propertyName;

    /**
     * Are we bound to the object-wide PropertyChangeListener?
     */
    boolean boundGlobal;

    /**
     * Are we bound to a property-specific PropertyChangeListner
     */
    boolean boundName;

    /**
     * The steps further down the path from us
     */
    BindPath[] children;

    /**
     * Called when we detect a change somewherer down our path.
     * First, check to see if our object is changing.  If so remove our old listener
     * Next, update the reference object the children have and recurse
     * Finally, add listeners if we have a different object
     *
     * @param listener This listener to attach.
     * @param newObject The object we should read our property off of.
     * @param updateSet The list of objects we have added listeners to
     */
    public synchronized void updatePath(PropertyChangeListener listener, Object newObject, Set updateSet) {
        if (currentObject != newObject) {
            removeListeners(listener);
        }
        if ((children != null) && (children.length > 0)) {
            try {
                Object newValue = null;
                if (newObject != null) {
                    updateSet.add(newObject);
                    newValue = InvokerHelper.getProperty(newObject, propertyName);
                }
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

    /**
     * Adds all the listeners to the objects in the bind path.
     * This assumes that we are not added as listeners to any of them, hence
     * it is not idempotent.
     *
     * @param listener This listener to attach.
     * @param newObject The object we should read our property off of.
     * @param updateSet The list of objects we have added listeners to
     */
    public void addAllListeners(PropertyChangeListener listener, Object newObject, Set updateSet) {
        addListeners(listener, newObject, updateSet);
        if ((children != null) && (children.length > 0)) {
            try {
                Object newValue = null;
                if (newObject != null) {
                    updateSet.add(newObject);
                    newValue = InvokerHelper.getProperty(newObject, propertyName);
                }
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

    /**
     * Add listeners to a specific object.  Updates the bould flags and update set
     *
     * @param listener This listener to attach.
     * @param newObject The object we should read our property off of.
     * @param updateSet The list of objects we have added listeners to
     */
    public void addListeners(PropertyChangeListener listener, Object newObject, Set updateSet) {
        boundName = false;
        boundGlobal = false;
        if (newObject != null) {
            MetaClass mc = InvokerHelper.getMetaClass(newObject);
            if (!mc.respondsTo(newObject, "addPropertyChangeListener", NAME_PARAMS).isEmpty()) {
                InvokerHelper.invokeMethod(newObject, "addPropertyChangeListener", new Object[] {propertyName, listener});
                boundName = true;
                updateSet.add(newObject);
            } else if (!mc.respondsTo(newObject, "addPropertyChangeListener", GLOBAL_PARAMS).isEmpty()) {
                InvokerHelper.invokeMethod(newObject, "addPropertyChangeListener", listener);
                boundGlobal = true;
                updateSet.add(newObject);
            }
        }
        currentObject = newObject;
    }

    /**
     * Remove listeners, believeing that our bould flags are accurate and it removes
     * only as declared.
     *
     * @param listener This listener to attach.
     */
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

}
