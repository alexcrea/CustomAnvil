rootProject.name = "CustomAnvil"

include("nms:nms-common")
findProject(":nms:nms-common")?.name = "nms-common"
include("nms:v1_18R1")
findProject(":nms:v1_18R1")?.name = "v1_18R1"
