import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion = project.properties["ktorVersion"] as String
val coroutinesVersion = project.properties["coroutinesVersion"] as String
val slf4jVersion = project.properties["slf4jVersion"] as String

plugins {
    kotlin("jvm") version "1.5.21"
    `java-library`
    `maven-publish`
}

group = "me.maya.revolt"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    // implementation(kotlin("stdlib"))
    // implementation(kotlin("reflect"))

    implementation("io.ktor", "ktor-client-core", ktorVersion)
    implementation("io.ktor", "ktor-client-cio", ktorVersion)
    implementation("io.ktor", "ktor-client-logging", ktorVersion)
    implementation("io.ktor", "ktor-utils", ktorVersion)
    implementation("io.ktor", "ktor-http", ktorVersion)

    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", coroutinesVersion)

    implementation("org.slf4j", "slf4j-api", slf4jVersion)
    // implementation("org.slf4j", "slf4j-simple", slf4jVersion)

    implementation(files(".\\libs\\JsonKt_mpp-jvm-1.0-SNAPSHOT.jar"))
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

java {
    withSourcesJar()
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.freeCompilerArgs = listOf("-Xopt-in=kotlinx.coroutines.DelicateCoroutinesApi", "-Xopt-in=kotlin.time.ExperimentalTime")
    }

    jar {
        manifest {
            attributes(mapOf("ImplementationTitle" to project.name,
            "Implementation-Version" to project.version))
        }
    }
}

val fatJar by tasks.creating(Jar::class) {
    archiveClassifier.set("full")
    group = JavaBasePlugin.BUILD_TASK_NAME
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    description = "Takes all dependencies"
    exclude("**/*.kotlin_metadata")
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks["jar"] as CopySpec)
}

publishing {
    publications {
        create<MavenPublication>("default") {
            from(components["java"])
            artifact(fatJar)
        }
    }
    repositories {
        maven {
            url = uri("G:\\Programming\\Kotlin\\Revolt\\buildRepo")
        }
    }
}

tasks.wrapper {
    gradleVersion = "7.2"
}
