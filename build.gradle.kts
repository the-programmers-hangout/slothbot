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

dependencies {
    implementation("com.eclipsesource.minimal-json:minimal-json:0.9.5")
    implementation("com.github.boscojared.disparse:disparse-smalld:master-SNAPSHOT")
    implementation("com.github.princesslana:smalld:0.2.6")
    implementation("com.google.guava:guava:30.0-jre")
    implementation("io.github.cdimascio:dotenv-java:2.2.0")
    implementation("org.apache.logging.log4j:log4j-api:2.14.0")
    runtimeOnly("org.apache.logging.log4j:log4j-core:2.14.0")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j18-impl:2.14.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.1")
    testImplementation("org.assertj:assertj-core:3.18.1")
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
