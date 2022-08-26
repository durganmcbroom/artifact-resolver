package com.durganmcbroom.artifact.resolver.simple.maven.layout

import arrow.core.Either
import com.durganmcbroom.artifact.resolver.CheckedResource

public interface SimpleMavenRepositoryLayout {
    public val name: String

    public fun resourceOf(groupId: String, artifactId: String, version: String, classifier: String?, type: String) : Either<ResourceRetrievalException, CheckedResource>
}