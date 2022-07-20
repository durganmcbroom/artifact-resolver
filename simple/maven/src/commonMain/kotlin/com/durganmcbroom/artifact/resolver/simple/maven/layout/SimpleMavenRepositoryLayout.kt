package com.durganmcbroom.artifact.resolver.simple.maven.layout

import com.durganmcbroom.artifact.resolver.CheckedResource


public interface SimpleMavenRepositoryLayout {
    public val type: String

    public fun artifactOf(groupId: String, artifactId: String, version: String, classifier: String?, type: String) : CheckedResource?
}