import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")
    // Apply the Application plugin to add support for building an executable JVM application.
    application
}

sourceSets {
    val stub by creating {
        java.srcDir("src/stub/java")
    }
    
    val debug by creating {
        java.srcDir("src/debug/kotlin")
    }
}

dependencies {
    implementation("net.bytebuddy:byte-buddy:1.18.8")
    implementation("net.bytebuddy:byte-buddy-agent:1.18.8")
    implementation("net.java.dev.jna:jna:5.17.0")
    implementation("net.java.dev.jna:jna-platform:5.17.0")
    implementation("com.tangorabox:component-inspector-swing:1.1.0")
    compileOnly(sourceSets["stub"].output)
    "debugImplementation"(files("C:\\Program Files\\STMicroelectronics\\STM32Cube\\STM32CubeMX\\STM32CubeMX.jar"))
    "debugImplementation"(files("C:\\Program Files\\STMicroelectronics\\STM32Cube\\STM32CubeMX\\plugins\\projectmanager.jar"))
}

application {
    // Define the Fully Qualified Name for the application main class
    // (Note that Kotlin compiles `App.kt` to a class with FQN `com.example.app.AppKt`.)
    mainClass = "com.shacha.mxpatcher.AgentKt"
}

tasks.withType<ShadowJar> {
    manifest {
        attributes(
            "Can-Redefine-Classes" to "true",
            "Can-Retransform-Classes" to "true",
            "Premain-Class" to "com.shacha.mxpatcher.Agent",
            "Main-Class" to application.mainClass
        )
    }
}

tasks.register<ShadowJar>("liteJar") {
    archiveClassifier.set("lite")
    from(sourceSets.main.get().output)
    configurations = listOf(project.configurations.runtimeClasspath.get())
    dependencies {
        exclude(dependency("net.java.dev.jna:jna:.*"))
        exclude(dependency("net.java.dev.jna:jna-platform:.*"))
    }
    minimize()
}

tasks.build {
    dependsOn("liteJar")
}

tasks.register<JavaExec>("runDebug") {
    dependsOn("build")
    group = "application"

    mainClass.set("debug.MainKt")

    classpath = sourceSets["debug"].runtimeClasspath

    jvmArgs = listOf(
        "-javaagent:C:\\Users\\shach\\IdeaProjects\\STM32CubeMXPatch\\app\\build\\libs\\app-lite.jar=debug,inspector"
    )
    
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(21))
    })
}