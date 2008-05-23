


// List the controllers to be instantiated and the names to sotre in the builder
controllers = [greet:'greet.GreetController']


// list of views to be auto-wired with certian controllers
viewMap = [ greet:[greetPanel:'greet.GreetPanel'] ]


// The primary builder.
// for an Applicaiton this will be placed in a JFrame
// for an Applet, this will be placed in the JApplet
primaryView="greet.greetPanel"

// the menu bar to attach to the applet or JFrame, if present.
//primaryMenuBar =  basicController.menu

// configuration for default frames in applicaiton mode
application {
    title = "Greet - A Groovy Twitter Client"
    size = [320, 640]
    //location = [50, 50] // a null will cause platform automated locaiton
}