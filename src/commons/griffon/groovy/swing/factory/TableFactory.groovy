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

package griffon.groovy.swing.factory

import griffon.groovy.util.FactoryBuilderSupport
import javax.swing.JTable
import javax.swing.table.TableColumn
import javax.swing.table.TableColumnModel
import javax.swing.table.TableModel
import org.codehaus.groovy.runtime.InvokerHelper


class TableFactory extends BeanFactory {

    public TableFactory() {
        this(JTable)
    }

    public TableFactory(Class klass) {
        super(klass, false)
    }

    public Object newInstance(FactoryBuilderSupport builder, Object name, Object value, Map attributes) {
        Object table = super.newInstance(builder, name, value, attributes);
        // insure metaproperties are registered
        enhanceMetaClass(table)
        return table
    }

    public static void enhanceMetaClass(table) {
        Class klass = table.getClass()
        MetaClassRegistry mcr = GroovySystem.metaClassRegistry
        MetaClass mc = mcr.getMetaClass(klass)
        boolean init = false
        if (!(mc instanceof ExpandoMetaClass)) {
            mc = new ExpandoMetaClass(klass)
            init = true
        }
        if (mc.getMetaMethod('getElements') == null) {
            mc.getElements << {->
                def model = delegate.model;
                if (model instanceof javax.swing.table.DefaultTableModel) {
                    return Collections.unmodifiableList(model.getDataVector())
                } else if (model instanceof griffon.groovy.model.DefaultTableModel) {
                    return Collections.unmodifiableList(model.rows)
                }
            }
        }
        if (mc.getMetaMethod('getSelectedElement') == null) {
            mc.getSelectedElement << {->
                return getElement(delegate, delegate.selectedRow)
            }
        }
        if (mc.getMetaMethod('getSelectedElements') == null) {
            mc.getSelectedElements << {->
                def myTable = delegate
                return myTable.getSelectedRows().collect() { getElement(myTable, it) }
            }
        }
        if (init) {
            mc.initialize()
            mcr.setMetaClass(klass, mc)
        }
    }

    public static Object getElement(JTable table, int row) {
        if (row == -1) {
            return null;
        }
        TableModel model = table.model
        if (model instanceof javax.swing.table.DefaultTableModel) {
            // could be groovier, but it works and is a well understookd idiom
            Map value = [:]
            TableColumnModel cmodel = table.columnModel
            for (int i = 0; i < cmodel.getColumnCount(); i++) {
                TableColumn c = cmodel.getColumn(i);
                value.put(c.getIdentifier(), // will fall through to headerValue
                    table.getValueAt(row, c.getModelIndex()))
            }
            return value;
        } else if (model instanceof griffon.groovy.model.DefaultTableModel) {
            Object rowValue = model.getRowsModel().value
            if (rowValue == null) {
                return null;
            } else {
                return InvokerHelper.asList(rowValue)[row]
            }
        }
    }

    public void setChild(FactoryBuilderSupport builder, Object parent, Object child) {
        if (child instanceof TableColumn) {
            parent.addColumn(child);
        } else if (child instanceof TableModel) {
            parent.model = child;
        }
    }

}

