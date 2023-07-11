plugins {
    java
}

group = "dev.alexjs"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {

    implementation("com.google.guava:guava:31.1-jre")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")

}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks {
    test {
        useJUnitPlatform()
    }

    withType(JavaCompile::class) {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    wrapper {
        gradleVersion = "8.2"
        distributionType = Wrapper.DistributionType.ALL
    }
}
