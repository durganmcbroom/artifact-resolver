package com.durganmcbroom.artifact.resolver.simple.maven.layout

import arrow.core.Either
import com.durganmcbroom.artifact.resolver.CheckedResource

public interface SimpleMavenRepositoryFacet {
    public val type: String

    public fun resourceOf(groupId: String, artifactId: String, version: String, classifier: String?, type: String) : Either<ResourceRetrievalException, CheckedResource>
}