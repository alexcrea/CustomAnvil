rootProject.name = "CustomAnvil"

// NMS subproject
include("nms:nms-common")
findProject(":nms:nms-common")?.name = "nms-common"
include("nms:v1_21R1")
findProject(":nms:v1_21R1")?.name = "v1_21R1"
include("nms:v1_21R2")
findProject(":nms:v1_21R2")?.name = "v1_21R2"
include("nms:v1_21R3")
findProject(":nms:v1_21R3")?.name = "v1_21R3"
include("nms:v1_21R4")
findProject(":nms:v1_21R4")?.name = "v1_21R4"