package griffon.groovy.swing.binding

import javax.swing.JTable
import javax.swing.table.TableColumn
import javax.swing.table.TableColumnModel
import javax.swing.table.TableModel
import org.codehaus.groovy.runtime.InvokerHelper


/**
 * Created by IntelliJ IDEA.
 * User: Danno.Ferrin
 * Date: Jun 19, 2008
 * Time: 12:46:29 PM
 * To change this template use File | Settings | File Templates.
 */
class JTableMetaMethods {

    public static void enhanceMetaClass(table) {
        AbstractSyntheticMetaMethods.enhance(table, [

            getElements:{->
                def model = delegate.model;
                if (model instanceof javax.swing.table.DefaultTableModel) {
                    return Collections.unmodifiableList(model.getDataVector())
                } else if (model instanceof griffon.groovy.model.DefaultTableModel) {
                    return Collections.unmodifiableList(model.rows)
                }
            },
            getSelectedElement:{->
                return getElement(delegate, delegate.selectedRow)
            },
            getSelectedElements:{->
                def myTable = delegate
                return myTable.getSelectedRows().collect() { getElement(myTable, it) }
            }
        ]);
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


}