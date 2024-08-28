plugins {
    java
    application
}

group = "hellov"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}
application {

    // Define the main class for the application
    mainClassName = "hello.App"

}

// Configure the java plugin
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    manifest {
        attributes["Main-Class"] = application.mainClassName
    }
}
dependencies {
    compile("io.vertx:vertx-core:3.5.3")
    compile("io.vertx:vertx-web:3.5.3")
    compile("io.vertx:vertx-web-api-contract:3.5.3")
    compile("io.vertx:vertx-web-client:3.5.3")
    compile("io.vertx:vertx-rx-java2:3.5.3")
    compile("io.reactivex.rxjava2:rxjava:2.2.0")
    compile("org.slf4j:slf4j-simple:1.7.25")

    testCompile("junit", "junit", "4.12")
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

    getByName<JavaExec>("run") {
        args("client")
        classpath(" lib/" + configurations.runtime.map{ it.name }.joinToString(" lib/"))
        workingDir("$buildDir/libs")
    }
}