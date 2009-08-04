application {
    title='FileViewer'
    startupGroups = ['FileViewer']

    // Should Griffon exit when no Griffon created frames are showing?
    autoShutdown = true

    // If you want some non-standard application class, apply it here
    //frameClass = 'javax.swing.JFrame'
}
elements {
    // Element definition for "FilePanel"
    FilePanel {
        model = 'FilePanelModel'
        view = 'FilePanelView'
    }

    // Element definition for "FileViewer"
    FileViewer {
        model = 'FileViewerModel'
        view = 'FileViewerView'
        controller = 'FileViewerController'
    }

}
