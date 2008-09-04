
import java.awt.GridBagConstraints
import javax.swing.JTabbedPane
import javax.swing.SwingConstants
import java.util.GregorianCalendar
import org.jdesktop.swingx.calendar.DaySelectionModel
import org.jdesktop.swingx.calendar.DateSelectionModel.SelectionMode

gridBagLayout()

tabbedPane(tabPlacement:JTabbedPane.LEFT, weightx:1.0, weighty:1.0, fill:GridBagConstraints.BOTH) {
    vbox(title:'JXTitledPanel') {
        jxtitledPanel(title:"Panel 1") {
            label("Content goes here")
        }
        jxtitledPanel(title:"Panel 2") {
            label("Content goes here as well")
        }
        jxtitledPanel(title:"Panel 3") {
            label("Content goes here in addition to the previous two")
        }
    }

    vbox(title:'JXBusyLabel') {
        // default painter is hosed right now :(  Not a groovy problem
	    jxbusyLabel(id:'busy', enabled:true, busy:true, size:[100, 100], preferredSize:[100, 100], minimumSize:[100, 100], maximumSize:[100, 100])
        checkBox("I'm Busy", selected:bind(target:busy, targetProperty:'busy'))	
    }

    vbox(title:'JXLabel') {
        hbox {
          jxlabel('Griffon', icon:imageIcon('/griffon.jpeg'),
                  horizontalTextPosition: SwingConstants.CENTER, verticalTextPosition: SwingConstants.BOTTOM)
          jxlabel('Griffon', icon:imageIcon('/griffon.jpeg'),
                  horizontalTextPosition: SwingConstants.CENTER, verticalTextPosition: SwingConstants.BOTTOM,
                  textRotation:Math.PI/2)
        }
        hbox {
          jxlabel('Griffon', icon:imageIcon('/griffon.jpeg'),
                  horizontalTextPosition: SwingConstants.CENTER, verticalTextPosition: SwingConstants.BOTTOM,
                  textRotation:Math.PI)
          jxlabel('Griffon', icon:imageIcon('/griffon.jpeg'),
                  horizontalTextPosition: SwingConstants.CENTER, verticalTextPosition: SwingConstants.BOTTOM,
                  textRotation:Math.PI/2*3)
        }
    }

    vbox(title:'JXDatePicker'){
	hbox {
	    jxlabel('Default DatePicker: ')
	    datePicker()
        }
        hbox {
	    jxlabel('Preselected Date: ')
	    datePicker(date:new GregorianCalendar(2008,10,10).getTime())
        }
    }
    vbox(title:'JXGradientChooser') {
        gradientChooser()
    }

    vbox(title:'JXMonthView') {
        hbox {
	    jxlabel('Default MonthView: ')
	    monthView()
        }
        hbox {
	    jxlabel('MonthView Interval: ')
	    def model = new DaySelectionModel(selectionMode:SelectionMode.SINGLE_INTERVAL_SELECTION)
	    model.addSelectionInterval(new GregorianCalendar(2008,10,10).getTime(), new GregorianCalendar(2008,10,16).getTime())
	    monthView(firstDisplayedDay:new GregorianCalendar(2008,10,10).getTime(), model:model)
        }
    }
    
}

