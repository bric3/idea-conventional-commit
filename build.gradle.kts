@file:Suppress("TrailingComma")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
  java
  id("org.jetbrains.intellij") version "1.1.4"
  kotlin("jvm") version "1.5.21"
}

group = "com.github.lppedd"

repositories {
  //maven("https://dl.bintray.com/kotlin/kotlin-eap")
  maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
  maven("https://jitpack.io")
  mavenCentral()
}

dependencies {
  implementation(kotlin("stdlib-jdk8", "1.5.21"))

  implementation("commons-validator", "commons-validator", "1.7") {
    exclude("commons-beanutils", "commons-beanutils")
  }

  implementation("org.json", "json", "20210307")
  implementation("com.github.everit-org.json-schema", "org.everit.json.schema", "1.13.0")

  testImplementation("junit:junit:4.12")
}

intellij {
  version.set(properties("platformVersion"))
  downloadSources.set(true)
  pluginName.set("idea-conventional-commit")
  plugins.set(listOf("java"))
}

configure<JavaPluginConvention> {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

/** Points to the Java executable (usually `java.exe`) of a DCEVM-enabled JVM. */
val dcevmExecutable: String? by project

tasks {
  runIde {
    if (project.hasProperty("dcevm")) {
      dcevmExecutable?.let(::setExecutable)
    }
  }

  val kotlinSettings: KotlinCompile.() -> Unit = {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs += listOf(
      "-Xno-call-assertions",
      "-Xno-receiver-assertions",
      "-Xno-param-assertions",
      "-Xjvm-default=all",
      "-Xallow-kotlin-package",
      "-Xopt-in=kotlin.ExperimentalStdlibApi",
      "-Xopt-in=kotlin.ExperimentalUnsignedTypes",
      "-Xopt-in=kotlin.contracts.ExperimentalContracts",
      "-XXLanguage:+InlineClasses",
      "-XXLanguage:+UnitConversion"
    )
  }

  compileKotlin(kotlinSettings)
  compileTestKotlin(kotlinSettings)

  patchPluginXml {
    version.set(project.version.toString())
    sinceBuild.set(properties("pluginSinceBuild"))
    untilBuild.set(properties("pluginUntilBuild"))

    val projectPath = projectDir.path
    pluginDescription.set((File("$projectPath/plugin-description.html").readText(Charsets.UTF_8)))
    changeNotes.set((File("$projectPath/change-notes/${version.get().replace('.', '_')}.html").readText(Charsets.UTF_8)))
  }
}
