package com.durganmcbroom.artifact.resolver.simple.maven.layout

import com.durganmcbroom.resources.Resource

public interface SimpleMavenRepositoryLayout {
    public val name: String
    public val location: String

    public suspend fun resourceOf(groupId: String, artifactId: String, version: String, classifier: String?, type: String) : Resource
}