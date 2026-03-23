rootProject.name = "CustomAnvil"

// NMS subproject
include("nms:nms-common")
findProject(":nms:nms-common")?.name = "nms-common"
include("nms:nms-paper")
findProject(":nms:nms-paper")?.name = "nms-paper"


val reobfNMS = providers.gradleProperty("subprojects.reobfnms")
    .get().split(",")

for (nmsPart in reobfNMS) {
    include("nms:$nmsPart")
    findProject(":nms:$nmsPart")?.name = nmsPart
}

// compatibility subprojects
include(":impl:LegacyEcoEnchant")
findProject(":impl:LegacyEcoEnchant")?.name = "LegacyEcoEnchant"
include("impl:ExcellentEnchant5_4")
findProject(":impl:ExcellentEnchant5_4")?.name = "ExcellentEnchant5_4"