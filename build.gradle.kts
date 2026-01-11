import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.compileOnly
import org.gradle.kotlin.dsl.invoke

plugins {
    id("gg.meza.stonecraft")
}

repositories {
    maven("https://maven.terraformersmc.com/")
    flatDir {
        dirs(rootDir.resolve("libs"))
    }
}

dependencies {
    val platform = stonecutter.current.project.substringAfterLast('-')
    val version = stonecutter.current.project.substringBeforeLast('-')

    modCompileOnly("dev.emi:emi-$platform:1.1.22+$version:api")
    modRuntimeOnly("dev.emi:emi-$platform:1.1.22+$version")

    when (version) {
        "1.20.1" -> {
            if(platform == "fabric") {
                modImplementation("lib:ftb-library-fabric:2001.2.10")
                modImplementation("lib:ftb-quests-fabric:2001.4.14")
                modImplementation("lib:ftb-teams-fabric:2001.3.1")
            } else {
                modImplementation("lib:ftb-library-forge:2001.2.10")
                modImplementation("lib:ftb-quests-forge:2001.4.14")
                modImplementation("lib:ftb-teams-forge:2001.3.1")
                modImplementation("lib:architectury:9.2.14-forge")
                annotationProcessor("io.github.llamalad7:mixinextras-common:0.5.0")
                compileOnly("io.github.llamalad7:mixinextras-common:0.5.0")
                modImplementation("io.github.llamalad7:mixinextras-forge:0.5.0")
                include("io.github.llamalad7:mixinextras-forge:0.5.0")
            }
        }
        "1.21.1" -> {
            if(platform == "fabric") {
                modImplementation("lib:ftb-library-fabric:2101.1.20")
                modImplementation("lib:ftb-quests-fabric:2101.1.15")
                modImplementation("lib:ftb-teams-fabric:2101.1.4")
            } else {
                modImplementation("lib:ftb-library-neoforge:2101.1.20")
                modImplementation("lib:ftb-quests-neoforge:2101.1.15")
                modImplementation("lib:ftb-teams-neoforge:2101.1.4")
                modImplementation("lib:architectury:13.0.8-neoforge")
            }
        }
    }
}

val supportedPackFormats = listOf(15, 18, 22, 34)
val templatesDir = rootProject.layout.projectDirectory.dir("templates")
val outputDir = rootProject.layout.buildDirectory.dir("resourcepacks")
val targetShaderPath = "assets/certain_questing_additions/shaders/core/custom_background.fsh"

val zipTaskNames = mutableListOf<String>()

templatesDir.asFileTree.files.filter { it.name.endsWith(".glsl") }.forEach { shaderFile ->
    val shaderName = shaderFile.nameWithoutExtension
    val taskName = "zipResourcePack${shaderName.capitalize()}"
    val packDescription = "Шейдер '$shaderName' (1.20.1 - 1.21.1)"

    tasks.register<Zip>(taskName) {
        archiveFileName.set("resourcepack-${shaderName}-1.20_1.21.zip")
        destinationDirectory.set(outputDir)

        from(templatesDir) {
            include(shaderFile.name)
            rename { targetShaderPath }
        }

        val formatsArray = supportedPackFormats.joinToString(prefix = "[", separator = ", ", postfix = "]")

        val mcMetaContent = """
            {
              "pack": {
                "pack_format": $formatsArray,
                "description": "$packDescription"
              }
            }
        """.trimIndent()

        val mcMetaFile = temporaryDir.resolve("pack.mcmeta").apply {
            writeText(mcMetaContent)
        }

        from(mcMetaFile) {
            rename { "pack.mcmeta" }
        }
    }
    zipTaskNames.add(taskName)
}

tasks.register("createMultiVersionResourcePacks") {
    group = "resourcepack"
    description = "Создает мультиверсионные ресурспаки (1.20.1 - 1.21.1) для каждого шейдера."

    dependsOn(zipTaskNames)
}