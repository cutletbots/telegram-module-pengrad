plugins {
    id("java-library")
    id("maven-publish")
}

group = "ru.cutletbots"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven {
        name = "GHP-Cutlet"
        url = uri("https://maven.pkg.github.com/cutletbots/cutlet-api")
        credentials {
            username = System.getenv("PACKAGE_REGISTRY_USERNAME")
            password = System.getenv("PACKAGE_REGISTRY_TOKEN")
        }
    }
}


val apiModuleDependency by configurations.creating
val moduleDependency by configurations.creating {
    extendsFrom(apiModuleDependency)
}

configurations {
    compileClasspath {
        extendsFrom(moduleDependency)
    }

    runtimeClasspath {
        extendsFrom(moduleDependency)
    }

    api {
        extendsFrom(apiModuleDependency)
    }
}

dependencies {
    api("ru.cutletbots:cutlet-api:1.2.0")

    apiModuleDependency("com.github.pengrad:java-telegram-bot-api:6.8.0")

    compileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")

    testCompileOnly("org.projectlombok:lombok:1.18.22")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.22")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

val fatJar = task("fullModule", type = Jar::class) {
    group = "get-jar"
    archiveBaseName.set("${project.name}-shaded")

    from(configurations.getByName("moduleDependency").map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }

    artifacts {
        archives(sourcesJar)
        archives(jar)
    }
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "16"
    targetCompatibility = "16"
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/cutletbots/telegram-module-pengrad")
            credentials {
                username = System.getenv("PACKAGE_REGISTRY_USERNAME")
                password = System.getenv("PACKAGE_REGISTRY_TOKEN")
            }
        }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}