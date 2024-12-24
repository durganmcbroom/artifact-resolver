package com.durganmcbroom.artifact.resolver.simple.maven.layout

import com.durganmcbroom.resources.Resource

public interface SimpleMavenRepositoryFacet {
    public val type: String

    public suspend fun resourceOf(groupId: String, artifactId: String, version: String, classifier: String?, type: String) : Resource
}