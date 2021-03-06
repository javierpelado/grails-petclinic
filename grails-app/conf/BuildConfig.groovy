grails.servlet.version = "2.5"
grails.project.work.dir = "target/$grailsVersion"
grails.project.target.level = 1.6
grails.project.source.level = 1.6

//grails.plugin.location.easygrid ="../Easygrid"
grails.project.dependency.resolution = {

    inherits "global"
    log "error"
    checksums true

    repositories {
        inherits true

        grailsPlugins()
        grailsHome()
        grailsCentral()
    }

    dependencies {
        compile('org.mvel:mvel2:2.1.3.Final')
    }

    plugins {
        runtime ":hibernate:$grailsVersion"
        runtime ":jquery:1.8.0", ":jquery-ui:1.8.24"
        runtime ":resources:1.1.6"
        build ":tomcat:$grailsVersion"
        compile ":export:1.5"
        compile ":easygrid:1.3.0"
    }
}
