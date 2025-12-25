rootProject.name = "CustomAnvil"

// NMS subproject
include("nms:nms-common")
findProject(":nms:nms-common")?.name = "nms-common"
include("nms:nms-paper")
findProject(":nms:nms-paper")?.name = "nms-paper"
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
include("nms:v1_21R4")
findProject(":nms:v1_21R4")?.name = "v1_21R4"
include("nms:v1_21R5")
findProject(":nms:v1_21R5")?.name = "v1_21R5"
include("nms:v1_21R6")
findProject(":nms:v1_21R6")?.name = "v1_21R6"
include("nms:v1_21R7")
findProject(":nms:v1_21R7")?.name = "v1_21R7"

include(":impl:LegacyEcoEnchant")
findProject(":impl:LegacyEcoEnchant")?.name = "LegacyEcoEnchant"
include("impl:ExcellentEnchant5_3")
findProject(":impl:ExcellentEnchant5_3")?.name = "ExcellentEnchant5_3"