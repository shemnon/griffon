

mvcGroups {
    root {
        model = '@griffon.project.name@Model'
        view = '@griffon.project.name@View'
        controller = '@griffon.project.name@Controller'
    }
}


// The primary builder.
// for an Applicaiton this will be placed in a JFrame
// for an Applet, this will be placed in the JApplet
primaryView="mainPanel"

// the menu bar to attach to the applet or JFrame, if present.
//primaryMenuBar =  basicController.menu

// configuration for default frames in applicaiton mode
application {
    //frameClass = 'javax.swing.JFrame'
    title = "@griffon.project.name@"

    // a null will cause .pack() to be called
    // but will cause problems in an applet
    //size = [200, 200]

    // a null will cause platform automated location
    //location = [50, 50]
}