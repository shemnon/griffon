
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import javax.swing.JTabbedPane

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
}

