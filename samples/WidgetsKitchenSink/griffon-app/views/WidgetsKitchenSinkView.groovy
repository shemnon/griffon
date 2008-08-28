

mainPanel = panel {
    borderLayout()
    label("How many Widgets are enough?", constraints:NORTH)

    tabbedPane(constraints:CENTER) {
        panel(title:'JIDE') {
            build(JideFlair)
        }

        panel(title:'SwingX') {
            build(SwingXFlair)
        }
    }
}
