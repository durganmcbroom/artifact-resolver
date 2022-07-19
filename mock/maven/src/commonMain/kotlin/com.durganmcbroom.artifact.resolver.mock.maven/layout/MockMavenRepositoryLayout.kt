package com.durganmcbroom.artifact.resolver.mock.maven.layout

import com.durganmcbroom.artifact.resolver.CheckedResource


public interface MockMavenRepositoryLayout {
    public val type: String
//    public val settings: MavenRepositorySettings

    public fun artifactOf(groupId: String, artifactId: String, version: String, classifier: String?, type: String) : CheckedResource?
}