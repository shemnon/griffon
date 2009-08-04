application {
    title='SwingPad'
    startupGroups = ['root','Script']

    // Should Griffon exit when no Griffon created frames are showing?
    autoShutdown = true

    // If you want some non-standard application class, apply it here
    //frameClass = 'javax.swing.JFrame'
}
elements {
    // Element definition for "root"
    root {
        model = 'SwingPadModel'
        view = 'SwingPadView'
        controller = 'SwingPadController'
    }

    // Element definition for "Script"
    Script {
        model = 'ScriptModel'
        view = 'ScriptView'
        controller = 'ScriptController'
    }
}