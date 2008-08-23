

mvcGroups {
    root {
        model = 'greet.GreetModel'
        view = 'greet.GreetView'
        controller = 'greet.GreetController'
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
    frameClass = 'org.jdesktop.swingx.JXFrame'
    title = "Greet - A Groovy Twitter Client"
    size = [320, 640]
    //location = [50, 50] // a null will cause platform automated locaiton
}