// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// griffon.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.griffon/${appName}-config.properties",
//                             "file:${userHome}/.griffon/${appName}-config.groovy"]

// if(System.properties["${appName}.config.location"]) {
//    griffon.config.locations << "file:" + System.properties["${appName}.config.location"]
// }


// log4j configuration
log4j {
    appender.stdout = "org.apache.log4j.ConsoleAppender"
    appender.'stdout.layout'="org.apache.log4j.PatternLayout"
    appender.'stdout.layout.ConversionPattern'='[%r] %c{2} %m%n'
    appender.errors = "org.apache.log4j.FileAppender"
    appender.'errors.layout'="org.apache.log4j.PatternLayout"
    appender.'errors.layout.ConversionPattern'='[%r] %c{2} %m%n'
    appender.'errors.File'="stacktrace.log"
    rootLogger="error,stdout"
    logger {
        griffon="error"
        StackTrace="error,errors"
        org {
            codehaus.groovy.griffon.commons="info" // core / classloading
        }
    }
    additivity.StackTrace=false
}

// key signing information
signingkey {
    params {
        keystore = "${basedir}/griffon-app/conf/keys/devKeystore"
        alias = 'development'
        storepass = 'BadStorePassword'
        keypass   = 'BadKeyPassword'
    }
}

griffon {
    jars {
        destDir = "${basedir}/target"
        jarName = "${appName}.jar"
    }
    webstart {
        codebase = "${new File(griffon.jars.destDir).toURI().toASCIIString()}"
        jnlp = "application.jnlp"
    }
    applet {
        jnlp = "applet.jnlp"
        html = "applet.html"
    }
}
