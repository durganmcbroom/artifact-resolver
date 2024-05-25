package com.durganmcbroom.artifact.resolver.simple.maven.pom

import com.durganmcbroom.artifact.resolver.MetadataRequestException

public open class PomException(message: String, override val cause: Throwable? = null) : MetadataRequestException(message) {
    public class PomNotFound(resource: String, lookedIn: List<String>, stage: PomProcessStage<*, *>) :
        PomException(
            """Failed to find POM: '$resource' during parsing of stage: '${stage.name}'. The following places have been searched: |${
                lookedIn.joinToString(separator = "\n") { " - $it" }
            }""".trimMargin()
        )

    public class InvalidRepository(layout: String, foundIn: String, name: String) : PomException(
        "Failed to parse repository: '$name' of layout '$layout'. This can be found in the POM of '$foundIn'"
    )

    public object DependencyManagementInjectionFailure :
        PomException("Failed to fully resolve dependencies version for dependency in management.") {
        private fun readResolve(): Any = DependencyManagementInjectionFailure
    }

    public class ParseException(location: String, override val cause: Throwable) :
        PomException("The given pom is invalid! It is located at: '$location'.")

    public class AssembleException(
        override val cause: Throwable,
        public val location: String
    ) : PomException("Failed to assemble the pom located at: '$location' due to the previous error(s). ")
}