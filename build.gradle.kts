import org.jreleaser.model.Active
import org.jreleaser.model.Signing.Mode

plugins {
    java
    alias(libs.plugins.jreleaser)
}

group = "dev.alexjs"
version = "2.2.0"

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
        license = "MIT"
    }

    signing {
        active = Active.ALWAYS
        mode = Mode.COMMAND
        armored = true
    }

//    release {
//        github {
//            repoOwner = "ajs1998"
//            name = "Dag" // TODO remove after repo is renamed
//            overwrite = true
//        }
//    }

    deploy {
        maven {
            nexus2 {
                create("maven-central") {
                    applyMavenCentralRules = true
                    active = Active.ALWAYS
                    url = "https://s01.oss.sonatype.org/service/local"
                    snapshotUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                    closeRepository = true
                    releaseRepository = true
                    stagingRepository("build/libs")
                }
            }
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
