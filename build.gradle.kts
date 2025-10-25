plugins {
    id("gg.meza.stonecraft")
}

repositories {
    flatDir {
        dirs(rootDir.resolve("libs"))
    }
}

dependencies {
    val platform = stonecutter.current.project.substringAfterLast('-')
    val version = stonecutter.current.project.substringBeforeLast('-')

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