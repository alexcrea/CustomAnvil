rootProject.name = "CustomAnvil"

// NMS subproject
include("nms:nms-common")
findProject(":nms:nms-common")?.name = "nms-common"

