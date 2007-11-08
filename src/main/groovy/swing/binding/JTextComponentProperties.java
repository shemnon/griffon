/*
 * Copyright 2007 the original author or authors.
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
package groovy.swing.binding;

import org.codehaus.groovy.binding.*;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;


/**
 * @author <a href="mailto:shemnon@yahoo.com">Danno Ferrin</a>
 * @version $Revision: 7953 $
 * @since Groovy 1.1
 */
public class JTextComponentProperties {

    public static Map<String, TriggerBinding> getSyntheticProperties() {
        Map<String, TriggerBinding> result = new HashMap<String, TriggerBinding>();
        result.put(JTextComponent.class.getName() + "#text",
            new TriggerBinding() {
                public FullBinding createBinding(SourceBinding source, TargetBinding target) {
                    return new JTextComponentTextBinding((PropertyBinding) source, target);
                }
            });
        return result;
    }
}


class JTextComponentTextBinding extends AbstractFullBinding implements PropertyChangeListener, DocumentListener {
    boolean bound;
    JTextComponent boundTextComponent;

    public JTextComponentTextBinding(PropertyBinding source, TargetBinding target) {
        bound = false;
        setSourceBinding(source);
        setTargetBinding(target);
    }

    public synchronized void bind() {
        if (!bound) {
            boundTextComponent = (JTextComponent) ((PropertyBinding)sourceBinding).getBean();
            try {
                boundTextComponent.addPropertyChangeListener("document", this);
                boundTextComponent.getDocument().addDocumentListener(this);
                bound = true;
            } catch (RuntimeException re) {
                try {
                    boundTextComponent.removePropertyChangeListener("document", this);
                    boundTextComponent.getDocument().removeDocumentListener(this);
                } catch (Exception e) {
                    // ignore as we are re-throwing the original cause
                }
                throw re;
            }
        }
    }

    public synchronized void unbind() {
        if (bound) {
            bound = false;
            // fail dirty, no checks
            boundTextComponent.removePropertyChangeListener("document", this);
            boundTextComponent.getDocument().removeDocumentListener(this);
            boundTextComponent = null;
        }
    }

    public void rebind() {
        if (bound) {
            unbind();
            bind();
        }
    }

    public void setSourceBinding(SourceBinding source) {
        if (!(source instanceof PropertyBinding)) {
            throw new IllegalArgumentException("Only PropertyBindings are accepted");
        }

        if (!"text".equals(((PropertyBinding)source).getPropertyName())) {
            throw new IllegalArgumentException("PropertyName must be 'text'");
        }
        if (!(((PropertyBinding)source).getBean() instanceof JTextComponent)) {
            throw new IllegalArgumentException("SourceBean must be a TextComponent");
        }
        super.setSourceBinding(source);
    }

    public void setTargetBinding(TargetBinding target) {
        super.setTargetBinding(target);
    }

    public void propertyChange(PropertyChangeEvent event) {
        update();
        ((Document)event.getOldValue()).removeDocumentListener(this);
        ((Document)event.getNewValue()).addDocumentListener(this);
    }

    public void changedUpdate(DocumentEvent event) {
        update();
    }

    public void insertUpdate(DocumentEvent event) {
        update();
    }

    public void removeUpdate(DocumentEvent event) {
        update();
    }

}
