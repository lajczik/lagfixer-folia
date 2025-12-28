plugins {
    id("java")
    id("com.gradleup.shadow") version "9.2.2"
}

val paperRepo = "https://repo.papermc.io/repository/maven-public/"
val sonatypeRepo = "https://oss.sonatype.org/content/groups/public/"
val jitpack = "https://jitpack.io"
val mojang = "https://libraries.minecraft.net"

version = "1.5.1.10"

dependencies {
    implementation(project(":plugin"))
    implementation(project(":nms:v1_20_R1"))
    implementation(project(":nms:v1_20_R2"))
    implementation(project(":nms:v1_20_R3"))
    implementation(project(":nms:v1_20_R4"))
    implementation(project(":nms:v1_21_R1"))
    implementation(project(":nms:v1_21_R2"))
    implementation(project(":nms:v1_21_R3"))
    implementation(project(":nms:v1_21_R4"))
    implementation(project(":nms:v1_21_R5"))
    implementation(project(":nms:v1_21_R7"))
}

tasks {
    shadowJar {
        archiveBaseName.set("LagFixer")
        archiveClassifier.set("folia")
        archiveVersion.set("")

        destinationDirectory.set(file("C:/Users/lajczi/Desktop/testowy/plugins"))
    }
}

allprojects {
    group = "xyz.lychee";

    apply(plugin = "java")

    repositories {
        mavenLocal()
        mavenCentral()
        maven(paperRepo)
        maven(sonatypeRepo)
        maven(mojang)
        maven(jitpack)
    }

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.32")
        annotationProcessor("org.projectlombok:lombok:1.18.32")
    }

    tasks {
        compileJava {
            options.encoding = Charsets.UTF_8.name()
        }
    }
}


java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}