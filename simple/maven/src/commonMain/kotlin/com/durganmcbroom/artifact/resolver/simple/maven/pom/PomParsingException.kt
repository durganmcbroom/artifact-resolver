package com.durganmcbroom.artifact.resolver.simple.maven.pom

import com.durganmcbroom.artifact.resolver.MetadataRequestException

public sealed class PomParsingException(message: String) : MetadataRequestException(message) {
    public class PomNotFound(resource: String, lookedIn: List<String>, stage: PomProcessStage<*, *>) :
        PomParsingException(
            """Failed to find POM: '$resource' during parsing of stage: '${stage.name}'. The following places have been searched: |${
                lookedIn.joinToString(separator = "\n") { " - $it" }
            }""".trimMargin()
        )

    public class InvalidRepository(layout: String, foundIn: String, name: String) : PomParsingException(
        "Failed to parse repository: '$name' of layout '$layout'. This can be found in the POM of '$foundIn'"
    )

    public object DependencyManagementInjectionFailure : PomParsingException("Failed to fully resolve dependencies version for dependency in management.")

    public class InvalidPom(location: String, public val exception: Throwable) : PomParsingException("The given pom is invalid! It is located at: '$location'. The underlying exception was: '${exception.message}'")
}