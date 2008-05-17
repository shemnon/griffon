
target ( "default" : "Prints Hello World, proves the scripts are working") {
   helloWorld()
}

target( "helloWorld" : "Do it") {
    println "Hello World"
}
