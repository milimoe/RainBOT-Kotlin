plugins {
    val kotlinVersion = "1.6.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.12.3"
}

group = "org.milimoe"
version = "1.2.5"

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}
