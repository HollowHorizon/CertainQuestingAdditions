import gg.meza.stonecraft.mod
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
            if (platform == "fabric") {
                modImplementation("lib:ftb-library-fabric:2001.2.10")
                modImplementation("lib:ftb-quests-fabric:2001.4.22")
                modImplementation("lib:ftb-teams-fabric:2001.3.1")
                modImplementation("dev.architectury:architectury-fabric:9.2.14")
                modRuntimeOnly("teamreborn:energy:3.0.0")
            } else {
                val mixinExtrasVersion = "0.5.0"

                modImplementation("lib:ftb-library-forge:2001.2.10")
                modImplementation("lib:ftb-quests-forge:2001.4.22")
                modImplementation("lib:ftb-teams-forge:2001.3.1")
                modImplementation("lib:architectury:9.2.14-forge")
                annotationProcessor("io.github.llamalad7:mixinextras-common:$mixinExtrasVersion")
                compileOnly("io.github.llamalad7:mixinextras-common:$mixinExtrasVersion")
                add("forgeRuntimeLibrary", "io.github.llamalad7:mixinextras-forge:$mixinExtrasVersion")
                include("io.github.llamalad7:mixinextras-forge:$mixinExtrasVersion")
            }
        }

        "1.21.1" -> {
            if (platform == "fabric") {
                modImplementation("lib:ftb-library-fabric:2101.1.20")
                modImplementation("lib:ftb-quests-fabric:2101.1.15")
                modImplementation("lib:ftb-teams-fabric:2101.1.4")
                modImplementation("dev.architectury:architectury-fabric:13.0.8")
                modRuntimeOnly("teamreborn:energy:4.1.0")
            } else {
                modImplementation("lib:ftb-library-neoforge:2101.1.20")
                modImplementation("lib:ftb-quests-neoforge:2101.1.15")
                modImplementation("lib:ftb-teams-neoforge:2101.1.4")
                modImplementation("lib:architectury:13.0.8-neoforge")
            }
        }

        "1.21.11" -> {
            if (platform == "fabric") {
                modImplementation("dev.ftb.mods:ftb-library-fabric:2111.1.1")
                modImplementation("dev.ftb.mods:ftb-quests-fabric:2111.1.5")
                modImplementation("dev.ftb.mods:ftb-teams-fabric:2111.1.1")
                modImplementation("dev.architectury:architectury-fabric:19.0.1")
                modRuntimeOnly("teamreborn:energy:4.1.0")
            } else {
                modImplementation("dev.ftb.mods:ftb-library-neoforge:2111.1.1")
                modImplementation("dev.ftb.mods:ftb-quests-neoforge:2111.1.5")
                modImplementation("dev.ftb.mods:ftb-teams-neoforge:2111.1.1")
                modImplementation("dev.architectury:architectury-neoforge:19.0.1")
            }
        }
    }
}

loom {
    val platform = stonecutter.current.project.substringAfterLast('-')
    val version = stonecutter.current.project.substringBeforeLast('-')

    if(version == "1.20.1" || (version == "1.21.1" && platform == "fabric")) {
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
