plugins {
    java
    alias(libs.plugins.jreleaser)
}

group = "dev.alexjs"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {

    implementation(libs.guava)

    testImplementation(libs.junit)
    testRuntimeOnly(libs.junitEngine)

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
