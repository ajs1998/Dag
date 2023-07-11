plugins {
    java
    alias(libs.plugins.jreleaser)
}

group = "dev.alexjs"
version = "0.0.0"

repositories {
    mavenCentral()
}

dependencies {

    implementation(libs.guava)

    testImplementation(libs.junit)
    testRuntimeOnly(libs.junitEngine)

}

jreleaser {
    project {
        authors.set(listOf("Alex"))
        license = "MIT" // TODO Handle license better
    }

    release {
        github {
            repoOwner = "ajs1998"
            name = "Dag"
//            host = "github.com"
//            apiEndpoint = "https://api.github.com"
            overwrite = true
        }
    }
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
        distributionType = if (System.getenv("CI").toBoolean()) {
            Wrapper.DistributionType.BIN
        } else {
            Wrapper.DistributionType.ALL
        }
    }
}
