plugins {
    java
    application
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation(files("libs/core.jar"))
}

application {
    mainClass.set("Display")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "Display"
    }
}