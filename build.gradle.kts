import gg.meza.stonecraft.mod
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.language.jvm.tasks.ProcessResources
import java.util.*

plugins {
    id("gg.meza.stonecraft")
}

val properties = Properties()
val userPropertiesFile = project.rootProject.file("user.properties")
if (userPropertiesFile.exists()) {
    properties.load(userPropertiesFile.inputStream())
}

fun publishToken(propertyName: String, environmentName: String): String? {
    return properties.getProperty(propertyName)
        ?: providers.gradleProperty(propertyName).orNull
        ?: providers.environmentVariable(environmentName).orNull
}

val ftbQuestsMinimumVersion = when (stonecutter.current.project.substringBeforeLast('-')) {
    "1.20.1" -> "2001.4.14"
    "1.21.1" -> "2101.1.15"
    "1.21.11" -> "2111.1.5"
    else -> error("No minimum FTB Quests version configured for ${stonecutter.current.project}")
}

tasks.named<ProcessResources>("processResources") {
    inputs.property("ftbQuestsMinimumVersion", ftbQuestsMinimumVersion)
    filesMatching(listOf("fabric.mod.json", "META-INF/mods.toml", "META-INF/neoforge.mods.toml")) {
        filter<ReplaceTokens>("tokens" to mapOf("ftbquests_min_version" to ftbQuestsMinimumVersion))
    }
}

afterEvaluate {
    tasks.withType<Jar> {
        manifest {
            attributes(
                "MixinConfigs" to "certain_questing_additions.mixins.json"
            )
        }
    }

    val generatedPackMetaTasks = tasks.matching { it.name == "generatePackMCMetaJson" }
    tasks.matching { it.name.startsWith("net.fabricmc.devlaunchinjector.Main.main") }.configureEach {
        dependsOn(generatedPackMetaTasks)
    }
}

repositories {
    maven("https://maven.fabricmc.net/")
    maven("https://maven.terraformersmc.com/")
    maven("https://maven.ftb.dev/releases")
    flatDir {
        dirs(rootDir.resolve("libs"))
    }
}

dependencies {
    val platform = stonecutter.current.project.substringAfterLast('-')
    val version = stonecutter.current.project.substringBeforeLast('-')
    val emiVersion = when (version) {
        "1.21.11" -> "1.1.24+1.21.1"
        else -> "1.1.22+$version"
    }

    modCompileOnly("dev.emi:emi-$platform:$emiVersion:api")
    if (version != "1.21.11") {
        modRuntimeOnly("dev.emi:emi-$platform:$emiVersion")
    }

    when (version) {
        "1.20.1" -> {
            val ftblib = "2001.2.10"
            val ftbquest = "2001.4.22"
            val ftbteams = "2001.3.1"
            val arch = "9.2.14"
            if (platform == "fabric") {
                modImplementation("lib:ftb-library-fabric:$ftblib")
                modImplementation("lib:ftb-quests-fabric:$ftbquest")
                modImplementation("lib:ftb-teams-fabric:$ftbteams")
                modImplementation("dev.architectury:architectury-fabric:$arch")
                modRuntimeOnly("teamreborn:energy:3.0.0")
            } else {
                val mixinExtrasVersion = "0.5.0"

                modImplementation("lib:ftb-library-forge:$ftblib")
                modImplementation("lib:ftb-quests-forge:$ftbquest")
                modImplementation("lib:ftb-teams-forge:$ftbteams")
                modImplementation("lib:architectury:$arch-forge")
                annotationProcessor("io.github.llamalad7:mixinextras-common:$mixinExtrasVersion")
                compileOnly("io.github.llamalad7:mixinextras-common:$mixinExtrasVersion")
                add("forgeRuntimeLibrary", "io.github.llamalad7:mixinextras-forge:$mixinExtrasVersion")
                include("io.github.llamalad7:mixinextras-forge:$mixinExtrasVersion")
            }
        }

        "1.21.1" -> {
            val ftblib = "2101.1.37"
            val ftbquest = "2101.1.36"
            val ftbteams = "2101.1.11"
            val arch = "13.0.8"
            if (platform == "fabric") {
                modImplementation("lib:ftb-library-fabric:$ftblib")
                modImplementation("lib:ftb-quests-fabric:$ftbquest")
                modImplementation("lib:ftb-teams-fabric:$ftbteams")
                modImplementation("dev.architectury:architectury-fabric:$arch")
                modRuntimeOnly("teamreborn:energy:4.1.0")
            } else {
                modImplementation("lib:ftb-library-neoforge:$ftblib")
                modImplementation("lib:ftb-quests-neoforge:$ftbquest")
                modImplementation("lib:ftb-teams-neoforge:$ftbteams")
                modImplementation("lib:architectury:$arch-neoforge")
            }
        }

        "1.21.11" -> {
            val ftblib = "2111.1.1"
            val ftbquest = "2111.1.5"
            val ftbteams = "2111.1.1"
            val arch = "19.0.1"
            if (platform == "fabric") {
                modImplementation("dev.ftb.mods:ftb-library-fabric:$ftblib")
                modImplementation("dev.ftb.mods:ftb-quests-fabric:$ftbquest")
                modImplementation("dev.ftb.mods:ftb-teams-fabric:$ftbteams")
                modImplementation("dev.architectury:architectury-fabric:$arch")
                modRuntimeOnly("teamreborn:energy:4.1.0")
            } else {
                modImplementation("dev.ftb.mods:ftb-library-neoforge:$ftblib")
                modImplementation("dev.ftb.mods:ftb-quests-neoforge:$ftbquest")
                modImplementation("dev.ftb.mods:ftb-teams-neoforge:$ftbteams")
                modImplementation("dev.architectury:architectury-neoforge:$arch")
            }
        }
    }
}

loom {
    val platform = stonecutter.current.project.substringAfterLast('-')
    val version = stonecutter.current.project.substringBeforeLast('-')

    if(version != "1.21.11") {
        mixin.useLegacyMixinAp.set(true)
        mixin.add(sourceSets.named("main").get(), "certain-questing-additions.refmap.json")
    }
}

val supportedPackFormats = listOf(15, 18, 22, 34, 86)
val templatesDir = rootProject.layout.projectDirectory.dir("templates")
val outputDir = rootProject.layout.buildDirectory.dir("resourcepacks")
val targetShaderPath = "assets/certain_questing_additions/shaders/core/custom_background.fsh"

val zipTaskNames = mutableListOf<String>()

templatesDir.asFileTree.files.filter { it.name.endsWith(".glsl") }.forEach { shaderFile ->
    val shaderName = shaderFile.nameWithoutExtension
    val taskName = "zipResourcePack${shaderName.capitalize()}"
    val packDescription = "Шейдер '$shaderName' (1.20.1 - 1.21.11)"

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
    description = "Создает мультиверсионные ресурспаки (1.20.1 - 1.21.11) для каждого шейдера."

    dependsOn(zipTaskNames)
}

publishMods {
    dryRun = false

    modrinth {
        projectId = "5BPpCYUe"
        accessToken = publishToken("modrinthToken", "MODRINTH_TOKEN")
        minecraftVersions.set(listOf(mod.minecraftVersion))
        version.set(mod.version)
        modLoaders.set(listOf(mod.loader))

        requires("architectury-api")
    }

    curseforge {
        projectId = "1372051"
        accessToken = publishToken("curseforgeToken", "CURSEFORGE_TOKEN")

        minecraftVersions.set(listOf(mod.minecraftVersion))
        version.set(mod.version)
        modLoaders.set(listOf(mod.loader))

        clientRequired = true
        serverRequired = false

        requires("architectury-api")
        if (mod.isFabric) {
            requires("ftb-quests-fabric")
        } else {
            requires("ftb-quests-forge")
        }
    }
}
