
rootProject.name = "artifact-resolver"
include("mock")
include("mock:untitled")
findProject(":mock:untitled")?.name = "untitled"
include("mock:maven")
findProject(":mock:maven")?.name = "maven"
