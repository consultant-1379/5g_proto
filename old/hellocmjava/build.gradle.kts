import org.gradle.jvm.tasks.Jar

version = "0.0.1-9"

plugins {
    java
    application
    // Plugin providing the taskTree task which when invoked shows a dependency graph
    id("com.dorongold.task-tree").version("1.3")
}

// Configure the application plugin
application {

    // Define the main class for the application
    //mainClassName = "com.thedevpiece.mss.Application"
    mainClassName = "com.ericsson.esc.bsf.main.App"

}

// Configure the java plugin
java {
    sourceCompatibility = JavaVersion.VERSION_1_9
    targetCompatibility = JavaVersion.VERSION_1_9
    manifest {
        attributes["Main-Class"] = application.mainClassName
        }
    }

dependencies {
    compile("com.google.guava:guava:23.0")
    compile("io.undertow:undertow-core:2.0.12.Final")
    compile("io.undertow:undertow-servlet:2.0.12.Final")
    compile("org.jboss.resteasy:resteasy-undertow:3.6.1.Final")
    compile("org.jboss.resteasy:resteasy-jaxb-provider:3.6.1.Final")
    compile("org.jboss.resteasy:resteasy-jackson2-provider:3.6.1.Final")
    compile("javax.xml.bind:jaxb-api:2.3.0")
    compile("javax.activation:activation:1.1.1")
    compile("io.reactivex:rxjava:1.2.5")

    testCompile("junit:junit:4.12")
}

// Configure the repository to use
repositories {
    jcenter()
}

tasks {
    // Create a new task to copy dependencies to $buildDir/lib/
    val copyDependencies by tasks.creating(Copy::class){
        from(configurations.compile)
        into("$buildDir/libs/lib")
    }

    // Configure the 'jar' task provided by the java plugin
    getByName<Jar>("jar") {
        dependsOn(copyDependencies)
        manifest {
            attributes(mapOf(
                    "Main-Class" to application.mainClassName,
                    "Class-Path" to " lib/" + configurations.runtime.map{ it.name }.joinToString(" lib/")))
        }
    }
}
