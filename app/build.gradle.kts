import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")
    // Apply the Application plugin to add support for building an executable JVM application.
    application
}

dependencies {
    implementation("net.bytebuddy:byte-buddy:1.18.8")
    implementation("net.bytebuddy:byte-buddy-agent:1.18.8")
    implementation("net.java.dev.jna:jna:5.17.0")
    implementation("net.java.dev.jna:jna-platform:5.17.0")
    
    compileOnly(rootProject.files("libs/STM32CubeMX.jar"))
}

application {
    // Define the Fully Qualified Name for the application main class
    // (Note that Kotlin compiles `App.kt` to a class with FQN `com.example.app.AppKt`.)
    mainClass = "com.shacha.mxpatcher.AppKt"
}

tasks.named<ShadowJar>("shadowJar") {
    minimize()
    manifest {
        attributes(
            "Can-Redefine-Classes" to "true",
            "Can-Retransform-Classes" to "true",
            "Premain-Class" to "com.shacha.mxpatcher.Agent"
        )
    }
}