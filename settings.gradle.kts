import java.net.URI

rootProject.name = "CustomAnvil"

// for Disenchantment dependency
sourceControl {
    gitRepository(URI.create("https://github.com/H7KZ/Disenchantment.git")) {
        producesModule("cz.kominekjan:Disenchantment")
    }
}

// NMS subproject
include("nms:nms-common")
findProject(":nms:nms-common")?.name = "nms-common"
include("nms:v1_17R1")
findProject(":nms:v1_17R1")?.name = "v1_17R1"
include("nms:v1_18R1")
findProject(":nms:v1_18R1")?.name = "v1_18R1"
include("nms:v1_18R2")
findProject(":nms:v1_18R2")?.name = "v1_18R2"
include("nms:v1_19R1")
findProject(":nms:v1_19R1")?.name = "v1_19R1"
include("nms:v1_19R2")
findProject(":nms:v1_19R2")?.name = "v1_19R2"
include("nms:v1_19R3")
findProject(":nms:v1_19R3")?.name = "v1_19R3"
include("nms:v1_20R1")
findProject(":nms:v1_20R1")?.name = "v1_20R1"
include("nms:v1_20R2")
findProject(":nms:v1_20R2")?.name = "v1_20R2"
include("nms:v1_20R3")
findProject(":nms:v1_20R3")?.name = "v1_20R3"
include("nms:v1_20R4")
findProject(":nms:v1_20R4")?.name = "v1_20R4"
include("nms:v1_21R1")
findProject(":nms:v1_21R1")?.name = "v1_21R1"
include("nms:v1_21R2")
findProject(":nms:v1_21R2")?.name = "v1_21R2"
include("nms:v1_21R3")
findProject(":nms:v1_21R3")?.name = "v1_21R3"
