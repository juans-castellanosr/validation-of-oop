plugins {
    application
    jacoco
}

group = "co.edu.autonoma.ancientseas"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass = "co.edu.autonoma.ancientseas.app.AncientSeasApp"
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 17
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror"))
}

tasks.withType<Javadoc>().configureEach {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:all,-missing", "-quiet")
    (options as StandardJavadocDocletOptions).addBooleanOption("Xwerror", true)
}

jacoco {
    toolVersion = "0.8.14"
}

tasks.test {
    useJUnitPlatform()
    systemProperty("java.awt.headless", "true")
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        html.required = true
        xml.required = true
    }
}

tasks.jar {
    manifest {
        attributes("Main-Class" to application.mainClass.get())
    }
}

tasks.build {
    dependsOn(tasks.javadoc)
}

val deliverablesTest: SourceSet = sourceSets.create("deliverablesTest") {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}

configurations[deliverablesTest.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
configurations[deliverablesTest.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())

val deliverablesDirectory = layout.buildDirectory.dir("deliverables")

val deliverables = tasks.register<Sync>("deliverables") {
    group = "distribution"
    description = "Collects the JAR, starter data, the sample save, Javadoc and coverage report in build/deliverables."
    dependsOn(tasks.jar, tasks.javadoc, tasks.jacocoTestReport)
    into(deliverablesDirectory)
    from(tasks.jar) { into("app") }
    from("src/main/resources/data") {
        exclude("saves/**")
        into("app/data")
    }
    from("src/main/resources/data/saves/sample-save.xml") { into("app/examples") }
    from(tasks.javadoc) { into("javadoc") }
    from(layout.buildDirectory.dir("reports/jacoco/test/html")) { into("coverage") }
}

val verifyDeliverables = tasks.register<Test>("deliverablesTest") {
    group = "verification"
    description = "Starts the game services on a copy of build/deliverables/app to prove a clean first run."
    dependsOn(deliverables)
    testClassesDirs = deliverablesTest.output.classesDirs
    classpath = deliverablesTest.runtimeClasspath
    useJUnitPlatform()
    systemProperty("java.awt.headless", "true")
    systemProperty("deliverables.app", deliverablesDirectory.get().dir("app").asFile.absolutePath)
    inputs.dir(deliverablesDirectory)
}

deliverables {
    finalizedBy(verifyDeliverables)
}
