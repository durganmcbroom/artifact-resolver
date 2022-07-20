
rootProject.name = "artifact-resolver"
include("simple")
include("simple:maven")
findProject(":simple:maven")?.name = "maven"
