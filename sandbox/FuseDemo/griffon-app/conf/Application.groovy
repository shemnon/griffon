elements {
    root {
        model = 'FuseDemoModel'
        view = 'FuseDemoView'
        controller = 'FuseDemoController'
    }
}

application {
    title="FuseDemo"

    // If you want some non-standard application class, apply it here
    //frameClass = 'javax.swing.JFrame'
}
// The following properties have been added by the Upgrade process...
application.startupGroups=['root'] // default startup group from 0.0

// The following properties have been added by the Upgrade process...
application.autoShutdown=true // default autoShutdown from 0.0
