plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("com.diffplug.spotless") version "5.8.1"
    id("com.adarshr.test-logger") version "2.1.1"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
}

// To update dependencies: rm gradle.lockfile && ./gradlew dependencies --write-locks
dependencyLocking {
    lockAllConfigurations()
    lockMode.set(LockMode.STRICT)
}

dependencies {
    implementation("com.eclipsesource.minimal-json:minimal-json:latest.release")
    implementation("com.github.boscojared.disparse:disparse-smalld:master-SNAPSHOT")
    implementation("com.github.princesslana:jsonf:latest.release")
    implementation("com.github.princesslana:smalld:latest.release")
    implementation("com.google.code.gson:gson:latest.release")
    implementation("com.google.guava:guava:latest.release")
    implementation("io.github.cdimascio:dotenv-java:latest.release")
    implementation("org.apache.commons:commons-lang3:latest.release")
    implementation("org.apache.logging.log4j:log4j-api:latest.release")
    runtimeOnly("org.apache.logging.log4j:log4j-core:latest.release")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j18-impl:latest.release")

    testImplementation("org.junit.jupiter:junit-jupiter-api:latest.release")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:latest.release")
    testImplementation("org.assertj:assertj-core:latest.release")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

application {
    mainClassName = "com.github.princesslana.slothbot.App"
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform()
}

spotless {
    java {
        googleJavaFormat("1.9")
    }
}
