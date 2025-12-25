rootProject.name = "CustomAnvil"

// NMS subproject
include("nms:nms-common")
findProject(":nms:nms-common")?.name = "nms-common"


val reobfNMS = providers.gradleProperty("subprojects.reobfnms")
    .get().split(",")

for (nmsPart in reobfNMS) {
    include("nms:$nmsPart")
    findProject(":nms:$nmsPart")?.name = nmsPart
}

// compatibility subprojects
include(":impl:LegacyEcoEnchant")
findProject(":impl:LegacyEcoEnchant")?.name = "LegacyEcoEnchant"
include("impl:ExcellentEnchant5_3")
findProject(":impl:ExcellentEnchant5_3")?.name = "ExcellentEnchant5_3"